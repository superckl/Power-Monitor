package me.superckl.upm.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
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
		Map<TileEntity, NetworkMember> toCheck = NetworkUtil.getConnectedNeighbors(upm, upm.getBlockPos(), Direction.values(), tracker);
		while(!toCheck.isEmpty()) {
			final Map<TileEntity, NetworkMember> toCheckTemp = new IdentityHashMap<>();
			toCheck.forEach((te, member) -> {
				boolean found = false;
				for(final WrappedNetworkMember existingMember:members)
					if(existingMember.getMember().isSameStorage(member)) {
						existingMember.addPosition(te.getBlockPos(), te.getType());
						found = true;
					}
				if(!found) {
					final WrappedNetworkMember wrapped = new WrappedNetworkMember(member, Sets.newHashSet(te.getBlockPos()), Lists.newArrayList(te.getType()));
					tracker.typeOf(te).ifPresent(type -> wrapped.setType(type));
					members.add(wrapped);
				}
				toCheckTemp.putAll(NetworkUtil.getConnectedNeighbors(te, te.getBlockPos(), member.childDirections(), tracker));

			});
			toCheck = new IdentityHashMap<>(toCheckTemp);
		}
		return members;
	}

	public static Map<TileEntity, NetworkMember> getConnectedNeighbors(final TileEntity originTE, final BlockPos pos, final Direction[] dirs, final TraversalTracker tracker){
		final Map<Direction, NetworkMember> originMembers = new EnumMap<>(Direction.class);
		for(final Direction dir:dirs)
			NetworkMember.from(originTE, dir).ifPresent(member -> {
				originMembers.put(dir, member);
			});
		final Map<TileEntity, NetworkMember> members = Maps.newIdentityHashMap();
		for(final Direction dir:originMembers.keySet()) {
			final BlockPos neighborPos = pos.relative(dir);
			if(!tracker.isInvalid(neighborPos)) {
				//visited.add(neighborPos);
				final TileEntity te = originTE.getLevel().getBlockEntity(neighborPos);
				NetworkMember.from(te, dir.getOpposite()).filter(member ->
				originMembers.get(dir).connects(member, dir, tracker.typeOf(originTE), tracker.typeOf(te)))
				.ifPresent(member -> {
					final NetworkMember unsided = NetworkMember.from(te, null).orElseThrow(() ->
					new IllegalStateException("Error scanning network. TE "+originTE.getType().getRegistryName()+" at "+pos+" does not provide unsided storage!"));
					members.put(te, unsided.resolve(Util.make(new EnumMap<>(Direction.class), map -> map.put(dir, member)), te));
					tracker.invalidate(neighborPos);
				});
			}
		}
		//Ignore UPMs
		members.keySet().removeIf(te -> te instanceof UPMTile);
		return members;
	}

	public static Pair<NetworkMember, List<TileEntityType<?>>> getAndVerify(final Set<BlockPos> positions, final World level) {
		NetworkMember member = null;
		final List<TileEntityType<?>> types = new ArrayList<>();
		TileEntity memberTE = null;
		for(final BlockPos pos:positions){
			final TileEntity te = level.getBlockEntity(pos);
			if(te == null)
				throw new IllegalStateException("Error deserializing EnergyNetwork. TileEntity at "+pos+" is null!");
			final NetworkMember intMember = NetworkMember.from(te, null).orElseThrow(() ->
			new IllegalStateException("Error deserializing EnergyNetwork. Unsided NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not exist!"));
			if(member == null) {
				member = intMember;
				memberTE = te;
			}else if(!member.isSameStorage(intMember))
				throw new IllegalStateException("Error deserializing EnergyNetwork. NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not match NetworkMember at "+memberTE.getBlockPos()+" for TE "+memberTE.getType().getRegistryName());
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
