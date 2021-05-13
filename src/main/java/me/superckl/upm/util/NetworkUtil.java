package me.superckl.upm.util;

import java.util.ArrayList;
import java.util.Collections;
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
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.RequiredArgsConstructor;
import me.superckl.upm.UPMTile;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.NetworkMember;
import me.superckl.upm.network.member.wrapper.PositionBasedWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class NetworkUtil {

	/**
	 * Peforms a comprehensive scan of "connected" network members by querying tile entities and network members
	 * @param upm The power monitor to start the search at
	 * @return A list of wrapped network members representing the determined energy network
	 */
	@SuppressWarnings("resource")
	public static List<PositionBasedWrapper> scan(final UPMTile upm) {
		if(upm.getLevel().isClientSide)
			return Collections.emptyList();
		//Map of found members and their positions. Multiple blocks may provide the same storage, hence Multimap
		final List<PositionBasedWrapper> members = new ArrayList<>();

		final TraversalTracker tracker = new TraversalTracker(upm.getTypeOverrides());

		//List of tile entities to check. Originally populated with all connected neighbors of the UPM
		Map<TileEntity, Pair<NetworkMember, Optional<Direction>>> toCheck = NetworkUtil.getConnectedNeighbors(upm.getLevel(), upm.getBlockPos(), Direction.values(), tracker);
		while(!toCheck.isEmpty()) {
			//temporary map to store tile entities we found to check
			final Map<TileEntity, Pair<NetworkMember, Optional<Direction>>> toCheckTemp = new IdentityHashMap<>();
			toCheck.forEach((te, member) -> {
				boolean found = false;
				//Check if this member has already been found (shares storage with another member)
				for(final PositionBasedWrapper existingMember:members)
					if(existingMember.getMember().isSameStorage(member.getLeft())) {
						//Simply add this position to the wrapped member if it already exists
						existingMember.addPosition(PositionUtil.getGlobalPos(te), te.getType(), member.getRight());
						found = true;
					}
				if(!found) {
					//Create a new wrapped member if it doesn't already exist
					final PositionBasedWrapper wrapped = new PositionBasedWrapper(member.getLeft(),
							Util.make(new HashMap<>(), map -> map.put(PositionUtil.getGlobalPos(te), member.getRight())),
							Lists.newArrayList(te.getType()));
					tracker.typeOf(te).ifPresent(type -> wrapped.setType(type));
					members.add(wrapped);
				}
				//Add all the connected neighbors of this member as tile entities to check
				toCheckTemp.putAll(NetworkUtil.getConnectedNeighbors(te.getLevel(), te.getBlockPos(), member.getKey().connectingDirections(), tracker));

			});
			//Assign the temporary map to the actual map once it's empty
			toCheck = new IdentityHashMap<>(toCheckTemp);
		}

		//Okay we've asked any present integration about what's the same storage
		//Now we do a brute force detection by injecting/removing energy and checking
		//if any of the members change at the same time
		return NetworkUtil.injectionConsolidate(members);
	}

	/**
	 * Finds all connected network members to block at the passed position.
	 * @param level The level the search is occurring in
	 * @param pos The position to look for neighboring members at
	 * @param dirs The directions to search for neighboring members
	 * @param tracker The traversal tracker for the current network search
	 * @return All connected tile entities and their associated network members and the direction (if adjacent). It is possible that a connected member
	 * is not adjacent (e.g., IE wires), and thus does not have an associated direction
	 */
	public static Map<TileEntity, Pair<NetworkMember, Optional<Direction>>> getConnectedNeighbors(final World level, final BlockPos pos, final Direction[] dirs, final TraversalTracker tracker){
		final TileEntity originTE = level.getBlockEntity(pos);
		//If there is no tile entity at this position, we cannot determine network members
		if(originTE == null)
			return Collections.emptyMap();
		//A map keeping track of the network members in each direction for this tile entity
		final Map<Direction, NetworkMember> originMembers = new EnumMap<>(Direction.class);
		//A map of "extra" connected positions that are exposed by the network member
		final Multimap<Direction, BlockPos> positions = MultimapBuilder.enumKeys(Direction.class).hashSetValues().build();
		for(final Direction dir:dirs)
			NetworkMember.from(originTE, dir).ifPresent(member -> {
				originMembers.put(dir, member);
				positions.putAll(dir, member.getConnections());
			});
		//Grab any positions that don't have an associated direction
		final Optional<Pair<NetworkMember, Set<BlockPos>>> noDirs = NetworkMember.from(originTE, null).map(member ->
		Pair.of(member, member.getConnections()));
		//Remove those that already have a direction
		noDirs.ifPresent(pair -> pair.getValue().removeAll(positions.values()));
		//Map of tile entities to their network member and direction, if it exists
		final Map<TileEntity, Pair<NetworkMember, Optional<Direction>>> members = Maps.newIdentityHashMap();
		//For all of the potential connected members, check they are connected
		for(final Direction dir:originMembers.keySet()) {
			final BlockPos neighborPos = pos.relative(dir);
			NetworkUtil.checkConnectedMember(originTE, dir, members, neighborPos, originMembers.get(dir), tracker);
		}
		positions.forEach((dir, connectedPos) -> {
			NetworkUtil.checkConnectedMember(originTE, dir, members, connectedPos, originMembers.get(dir), tracker);
		});
		noDirs.ifPresent(pair -> pair.getValue().forEach(connectedPos -> {
			NetworkUtil.checkConnectedMember(originTE, null, members, connectedPos, pair.getKey(), tracker);
		}));
		//Ignore UPMs
		members.keySet().removeIf(te -> te instanceof UPMTile);
		return members;
	}

	/**
	 * Checks that the network member at pos is actually connected to the originating network member, and adds it to members if so
	 * @param originTE the originating tile entity
	 * @param dir The direction from the originating tile entity
	 * @param members A map of all discovered members to which the connected member will be added
	 * @param pos The position of the member to check
	 * @param originMember The originating network member
	 * @param tracker The traversal tracker for the current network search
	 */
	private static void checkConnectedMember(final TileEntity originTE, final Direction dir, final Map<TileEntity, Pair<NetworkMember, Optional<Direction>>> members,
			final BlockPos pos, final NetworkMember originMember, final TraversalTracker tracker) {
		//If we've already searched this position, skip it
		if(!tracker.isInvalid(pos)) {
			//If this position isn't loaded, there's not much we can do except skip it
			//and mark it checked
			if(!originTE.getLevel().isLoaded(pos)) {
				tracker.invalidate(pos);
				return;
			}
			final TileEntity te = originTE.getLevel().getBlockEntity(pos);
			final Optional<Direction> dirOpt = Optional.ofNullable(dir);
			//get the network member, and is present, make sure it connects to the originating member
			NetworkMember.from(te, dirOpt.map(Direction::getOpposite).orElse(null)).filter(member ->
			originMember.connects(member, dirOpt, tracker.typeOf(originTE), tracker.typeOf(te)))
			.ifPresent(member -> {
				//Add it to members and mark this position as checked
				members.put(te, Pair.of(member, dirOpt.map(Direction::getOpposite)));
				tracker.invalidate(pos);
			});
		}
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
	public static List<PositionBasedWrapper> injectionConsolidate(final List<PositionBasedWrapper> members) {
		final List<PositionBasedWrapper> consolidatedMembers = new ArrayList<>();
		//Continue looping while there are still unconsolidated members
		while(!members.isEmpty()) {
			//Get the member at the top of the list to inject into
			final PositionBasedWrapper wrapped = members.remove(0);
			//Check if this member is some form of integration that has handled consolidating members
			if(wrapped.getMember().requiresInjectionCheck()) {
				//Freeze the storage for the remaining members to check for changes
				final LongList frozenStorage = new LongArrayList(consolidatedMembers.size());
				members.forEach(member -> frozenStorage.add(member.getMember().getCurrentEnergy()));
				//Attempt to insert or extract 1 FE
				//Try extraction first so we don't magically create energy if we don't have to
				//We'll try to restore the change, but sometimes providers are selectively read-only
				boolean wasInserted = false;
				final long stored = wrapped.getMember().getCurrentEnergy();
				int removed = wrapped.getMember().removeEnergy(1);
				if(removed == 0) {
					removed = -wrapped.getMember().addEnergy(1);
					wasInserted = true;
				}
				final long actual = stored-wrapped.getMember().getCurrentEnergy();
				if(actual != removed) {
					removed = (int) actual;
					OneTimeWarnings.injectionCheckActual(wrapped.getMember().getTileEntity());
				}
				if(removed != 0) {
					//If the member changed, loop over the remaining members to check for changes
					final Iterator<PositionBasedWrapper> it = members.iterator();
					int i = 0;
					while(it.hasNext()) {
						final PositionBasedWrapper test = it.next();
						if(test.getMember().getCurrentEnergy() != frozenStorage.getLong(i++)) {
							//Looks like this changed and is thus "the same as" member, merge the members
							//and remove test
							it.remove();
							wrapped.merge(test);
						}
					}
					//Undo the insertion or extraction
					if(wasInserted)
						wrapped.getMember().removeEnergy(-removed);
					else
						wrapped.getMember().addEnergy(removed);
				}
			}
			//We're done checking this member, so add it to the consolidated list
			consolidatedMembers.add(wrapped);
		}
		return consolidatedMembers;
	}

	/**
	 * Converts a map of block positions and directions into a network member for purposes of deserializing networks.
	 * It is not verified that all the members are the "same" in some regard. The first network members is used.
	 * @param positions The map of positions and direction to get network members from
	 * @param level The level of the energy network
	 * @return The network member and it's associated tile entities
	 * @throws IllegalStateException
	 */
	public static Pair<NetworkMember, List<TileEntityType<?>>> getMembers(final Map<GlobalPos, Optional<Direction>> positions) throws IllegalStateException{
		NetworkMember member = null;
		final List<TileEntityType<?>> types = new ArrayList<>();
		for(final GlobalPos pos:positions.keySet()){
			final TileEntity te = PositionUtil.getTileEntity(pos);
			if(te == null)
				throw new IllegalStateException("Error deserializing EnergyNetwork. TileEntity at "+pos+" is null!");
			final NetworkMember intMember = NetworkMember.from(te, positions.get(pos).orElse(null)).orElseThrow(() ->
			new IllegalStateException("Error deserializing EnergyNetwork. NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not exist!"));
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

		private final Map<TileEntityType<?>, MemberType> typeOverrides;

		public boolean invalidate(final BlockPos pos) {
			return this.invalid.add(pos);
		}

		public boolean isInvalid(final BlockPos pos) {
			return this.invalid.contains(pos);
		}

		public Optional<MemberType> typeOf(@Nonnull final TileEntity te){
			return Optional.ofNullable(this.typeOverrides.get(te.getType()));
		}

	}

}