package me.superckl.upm.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.RequiredArgsConstructor;
import me.superckl.upm.LogHelper;
import me.superckl.upm.UPMTile;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.network.member.NetworkMember;
import me.superckl.upm.network.member.WrappedNetworkMember;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkUtil {

	public static List<WrappedNetworkMember> scan(final UPMTile upm) {
		//Map of found members and their positions. Multiple blocks may provide the same storage, hence Multimap
		final List<WrappedNetworkMember> members = new ArrayList<>();

		final TraversalTracker tracker = new TraversalTracker(upm.getTypeOverrides());

		//List of tile entities to check. Originally populated with all connected neighbors of the UPM
		Map<TileEntity, Pair<NetworkMember, Direction>> toCheck = NetworkUtil.getConnectedNeighbors(upm, upm.getBlockPos(), Direction.values(), tracker);
		while(!toCheck.isEmpty()) {
			final Map<TileEntity, Pair<NetworkMember, Direction>> toCheckTemp = new IdentityHashMap<>();
			toCheck.forEach((te, member) -> {
				boolean found = false;
				for(final WrappedNetworkMember existingMember:members)
					if(existingMember.getMember().isSameStorage(member.getLeft())) {
						existingMember.addPosition(te.getBlockPos(), te.getType(), member.getRight());
						found = true;
					}
				if(!found) {
					final WrappedNetworkMember wrapped = new WrappedNetworkMember(member.getLeft(),
							Util.make(new HashMap<>(), map -> map.put(te.getBlockPos(), member.getRight())),
							Lists.newArrayList(te.getType()));
					tracker.typeOf(te).ifPresent(type -> wrapped.setType(type));
					members.add(wrapped);
				}
				toCheckTemp.putAll(NetworkUtil.getConnectedNeighbors(te, te.getBlockPos(), member.getLeft().childDirections(), tracker));

			});
			toCheck = new IdentityHashMap<>(toCheckTemp);
		}

		//Okay we've asked any present integration about what's the same storage
		//Now we do a brute force detection by injecting/removing energy and checking
		//if any of the members change at the same time
		return NetworkUtil.injectionConsolidate(members);
	}

	public static Map<TileEntity, Pair<NetworkMember, Direction>> getConnectedNeighbors(final TileEntity originTE, final BlockPos pos, final Direction[] dirs, final TraversalTracker tracker){
		final Map<Direction, NetworkMember> originMembers = new EnumMap<>(Direction.class);
		for(final Direction dir:dirs)
			NetworkMember.from(originTE, dir).ifPresent(member -> {
				originMembers.put(dir, member);
			});
		final Map<TileEntity, Pair<NetworkMember, Direction>> members = Maps.newIdentityHashMap();
		for(final Direction dir:originMembers.keySet()) {
			final BlockPos neighborPos = pos.relative(dir);
			if(!tracker.isInvalid(neighborPos)) {
				//visited.add(neighborPos);
				final TileEntity te = originTE.getLevel().getBlockEntity(neighborPos);
				NetworkMember.from(te, dir.getOpposite()).filter(member ->
				originMembers.get(dir).connects(member, dir, tracker.typeOf(originTE), tracker.typeOf(te)))
				.ifPresent(member -> {
					members.put(te, Pair.of(member, dir.getOpposite()));
					tracker.invalidate(neighborPos);
				});
			}
		}
		//Ignore UPMs
		members.keySet().removeIf(te -> te instanceof UPMTile);
		return members;
	}

	/**
	 * Runs a brute-force "is same storage" check on all network members passed in by
	 * attempting to insert (or extract if insert fails) 1 FE from one member at a time.
	 * The other members are scanned for changes in their storage that would indicate they
	 * are linked to that member;
	 * @param members The list of members to consolidate. This list must be modifiable and will
	 * be empty when the method returns
	 * @return The consolidated list of members
	 */
	public static List<WrappedNetworkMember> injectionConsolidate(final List<WrappedNetworkMember> members) {
		final List<WrappedNetworkMember> consolidatedMembers = new ArrayList<>();
		//Continue looping while there are still unconsolidated members
		while(!members.isEmpty()) {
			//Get the member at the top of the list to inject into
			final WrappedNetworkMember wrapped = members.remove(0);
			//Check if this member is some form of integration that has handled consolidating members
			if(wrapped.getMember().requiresInjectionCheck()) {
				//Freeze the storage for the remaining members to check for changes
				final LongList frozenStorage = new LongArrayList(consolidatedMembers.size());
				members.forEach(member -> frozenStorage.add(member.getMember().getCurrentEnergy()));
				//Attempt to insert or extract 1 FE
				//Try extraction first so we don't magically create energy if we don't have to
				//We'll try to restore the change, but sometimes providers are selectively read-only
				boolean wasInserted = false;
				int removed = wrapped.getMember().removeEnergy(1);
				if(removed == 0) {
					removed = wrapped.getMember().addEnergy(1);
					wasInserted = true;
				}
				if(removed != 0) {
					//If the member changed, loop over the remaining members to check for changes
					final Iterator<WrappedNetworkMember> it = members.iterator();
					int i = 0;
					while(it.hasNext()) {
						final WrappedNetworkMember test = it.next();
						LogHelper.info(test.getMember().getCurrentEnergy());
						if(test.getMember().getCurrentEnergy() != frozenStorage.getLong(i++)) {
							//Looks like this changed and is thus "the same as" member, merge the members
							//and remove test
							it.remove();
							wrapped.merge(test);
						}
					}
					//Undo the insertion or extraction
					if(wasInserted)
						wrapped.getMember().removeEnergy(removed);
					else
						wrapped.getMember().addEnergy(removed);
				}
			}
			//We're done checking this member, so add it to the consolidated list
			consolidatedMembers.add(wrapped);
		}
		return consolidatedMembers;
	}

	public static Pair<NetworkMember, List<TileEntityType<?>>> getMembers(final Map<BlockPos, Direction> positions, final World level) throws IllegalStateException{
		NetworkMember member = null;
		final List<TileEntityType<?>> types = new ArrayList<>();
		for(final BlockPos pos:positions.keySet()){
			final TileEntity te = level.getBlockEntity(pos);
			if(te == null)
				throw new IllegalStateException("Error deserializing EnergyNetwork. TileEntity at "+pos+" is null!");
			final NetworkMember intMember = NetworkMember.from(te, positions.get(pos)).orElseThrow(() ->
			new IllegalStateException("Error deserializing EnergyNetwork. Unsided NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not exist!"));
			if(member == null)
				member = intMember;
			if(!types.contains(te.getType()))
				types.add(te.getType());
		}
		return Pair.of(member, types);
	}
	@RequiredArgsConstructor
	public static class TraversalTracker{

		private final Set<BlockPos> invalid = new HashSet<>();
		private final Set<BlockPos> potential = new HashSet<>();

		private final Map<TileEntityType<?>, MemberType> typeOverrides;

		public Optional<Object> check(final BlockPos pos) {
			if(this.invalid.contains(pos))
				return Optional.of(VisitedType.INVALID);
			if(this.potential.contains(pos))
				return Optional.of(VisitedType.POTENTIAL);
			return Optional.empty();
		}

		public boolean invalidate(final BlockPos pos) {
			return this.invalid.add(pos);
		}

		public boolean addPotential(final BlockPos pos) {
			return this.potential.add(pos);
		}

		public boolean removePotential(final BlockPos pos) {
			return this.potential.remove(pos);
		}

		public boolean isInvalid(final BlockPos pos) {
			return this.invalid.contains(pos);
		}

		public Optional<MemberType> typeOf(@Nonnull final TileEntity te){
			return Optional.ofNullable(this.typeOverrides.get(te.getType()));
		}

	}

	public enum VisitedType{

		INVALID, POTENTIAL;

	}

}
