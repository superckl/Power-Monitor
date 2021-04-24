package me.superckl.upm;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.network.NetworkMember;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

@RequiredArgsConstructor
public class EnergyNetwork implements INBTSerializable<CompoundNBT>{

	private final UPMTile upm;
	private Multimap<NetworkMember, BlockPos> members;

	public boolean scan() {
		//Map of found members and their positions. Multiple blocks may provide the same storage, hence Multimap
		final Multimap<NetworkMember, BlockPos> members = MultimapBuilder.hashKeys().hashSetValues().build();
		//Set of visited positions so we don't double check positions. We assert that each member provides
		//the same storage on it's sides as unsided, so the position is a unique identifier
		final Set<BlockPos> visited = Sets.newHashSet();

		//List of tile entities to check. Originally populated with all connected neighbors of the UPM
		Map<TileEntity, NetworkMember> toCheck = this.getConnectedNeighbors(this.upm, this.upm.getBlockPos(), Direction.values(), visited);
		while(!toCheck.isEmpty()) {
			final Map<TileEntity, NetworkMember> toCheckTemp = new IdentityHashMap<>();
			toCheck.forEach((te, member) -> {
				boolean found = false;
				for(final NetworkMember existingMember:members.keySet())
					if(existingMember.isSameStorage(member)) {
						members.put(existingMember, te.getBlockPos());
						found = true;
					}
				if(!found)
					members.put(member, te.getBlockPos());
				toCheckTemp.putAll(this.getConnectedNeighbors(te, te.getBlockPos(), member.childDirections(), visited));
			});
			toCheck = new IdentityHashMap<>(toCheckTemp);
		}
		final boolean changed = !members.equals(this.members);
		this.updateMembers(members);
		return changed;
	}

	private void updateMembers(final Multimap<NetworkMember, BlockPos> members) {
		this.members = members;
	}

	public NonNullList<ItemStack> asItems(){
		final NonNullList<ItemStack> items = NonNullList.create();
		if(this.members != null)
			this.members.values().forEach(pos -> items.add(new ItemStack(this.upm.getLevel().getBlockState(pos).getBlock().asItem())));
		return items;
	}

	@Override
	public CompoundNBT serializeNBT() {
		final CompoundNBT nbt = new CompoundNBT();
		if(this.members != null) {
			final ListNBT list = new ListNBT();
			this.members.keys().forEach(key -> {
				list.add(new LongArrayNBT(this.members.get(key).stream().mapToLong(BlockPos::asLong).toArray()));
			});
			nbt.put("positions", list);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(final CompoundNBT nbt) {
		final Multimap<NetworkMember, BlockPos> members = MultimapBuilder.hashKeys().hashSetValues().build();
		if(nbt.contains("positions", Constants.NBT.TAG_LIST)) {
			final ListNBT list = nbt.getList("positions", Constants.NBT.TAG_LONG_ARRAY);
			list.forEach(inbt -> {
				final Set<BlockPos> positions = LongStream.of(((LongArrayNBT)inbt).getAsLongArray()).mapToObj(BlockPos::of).collect(Collectors.toSet());
				members.putAll(this.getAndVerify(positions), positions);
			});
		}
		this.updateMembers(members);
	}

	private NetworkMember getAndVerify(final Set<BlockPos> positions) {
		NetworkMember member = null;
		TileEntity memberTE = null;
		for(final BlockPos pos:positions){
			final TileEntity te = this.upm.getLevel().getBlockEntity(pos);
			if(te == null)
				throw new IllegalStateException("Error deserializing EnergyNetwork. TileEntity at "+pos+" is null!");
			final NetworkMember intMember = NetworkMember.from(te, null).orElseThrow(() ->
			new IllegalStateException("Error deserializing EnergyNetwork. Unsided NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not exist!"));
			if(member == null) {
				member = intMember;
				memberTE = te;
			}else if(!member.isSameStorage(intMember))
				throw new IllegalStateException("Error deserializing EnergyNetwork. NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not match NetworkMember at "+memberTE.getBlockPos()+" for TE "+memberTE.getType().getRegistryName());
		}
		return member;
	}

	private Map<TileEntity, NetworkMember> getConnectedNeighbors(final TileEntity originTE, final BlockPos pos, final Direction[] dirs, final Set<BlockPos> visited){
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
				final TileEntity te = this.upm.getLevel().getBlockEntity(neighborPos);
				NetworkMember.from(te, dir.getOpposite()).filter(member -> originMembers.get(dir).connects(member, dir)).ifPresent(member -> {
					LogHelper.info("TE "+te.getType().getRegistryName()+" connects to "+originTE.getType().getRegistryType());
					LogHelper.info(member.canExtract()+":"+member.canInsert());
					final NetworkMember unsided = NetworkMember.from(te, null).orElseThrow(() -> new IllegalStateException("Error scanning network. TE "+originTE.getType().getRegistryName()+" at "+pos+" does not provide unsided storage!"));
					members.put(te, unsided.resolve(Util.make(new EnumMap<>(Direction.class), map -> map.put(dir, member)), originTE));
				});
			}
		}
		//Ignore UPMs
		members.keySet().removeIf(te -> te instanceof UPMTile);
		return members;
	}

}
