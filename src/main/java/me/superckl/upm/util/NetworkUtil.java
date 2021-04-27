package me.superckl.upm.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
		//Set of visited positions so we don't double check positions. We assert that each member provides
		//the same storage on it's sides as unsided, so the position is a unique identifier
		final Set<BlockPos> visited = Sets.newHashSet();
		final Map<TileEntityType<?>, MemberType> typeOverrides = upm.getTypeOverrides();

		//List of tile entities to check. Originally populated with all connected neighbors of the UPM
		Map<TileEntity, NetworkMember> toCheck = NetworkUtil.getConnectedNeighbors(upm, upm.getBlockPos(), Direction.values(), visited);
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
					if(typeOverrides.containsKey(te.getType()))
						wrapped.setType(typeOverrides.get(te.getType()));
					members.add(wrapped);
				}
				toCheckTemp.putAll(NetworkUtil.getConnectedNeighbors(te, te.getBlockPos(), member.childDirections(), visited));

			});
			toCheck = new IdentityHashMap<>(toCheckTemp);
		}
		return members;
	}

	public static Map<TileEntity, NetworkMember> getConnectedNeighbors(final TileEntity originTE, final BlockPos pos, final Direction[] dirs, final Set<BlockPos> visited){
		final Map<Direction, NetworkMember> originMembers = new EnumMap<>(Direction.class);
		for(final Direction dir:dirs)
			NetworkMember.from(originTE, dir).ifPresent(member -> {
				originMembers.put(dir, member);
			});
		final Map<TileEntity, NetworkMember> members = Maps.newIdentityHashMap();
		for(final Direction dir:originMembers.keySet()) {
			final BlockPos neighborPos = pos.relative(dir);
			if(!visited.contains(neighborPos)) {
				visited.add(neighborPos);
				final TileEntity te = originTE.getLevel().getBlockEntity(neighborPos);
				NetworkMember.from(te, dir.getOpposite()).filter(member -> originMembers.get(dir).connects(member, dir)).ifPresent(member -> {
					final NetworkMember unsided = NetworkMember.from(te, null).orElseThrow(() -> new IllegalStateException("Error scanning network. TE "+originTE.getType().getRegistryName()+" at "+pos+" does not provide unsided storage!"));
					members.put(te, unsided.resolve(Util.make(new EnumMap<>(Direction.class), map -> map.put(dir, member)), te));
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

}
