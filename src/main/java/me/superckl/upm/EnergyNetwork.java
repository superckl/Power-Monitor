package me.superckl.upm;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.google.common.collect.Lists;
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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

@RequiredArgsConstructor
public class EnergyNetwork implements INBTSerializable<CompoundNBT>{

	private final UPMTile upm;
	private Multimap<NetworkMember, BlockPos> members;

	public boolean scan() {
		final Multimap<NetworkMember, BlockPos> members = MultimapBuilder.hashKeys().hashSetValues().build();
		final List<TileEntity> toCheck = Lists.newArrayList(this.upm);
		final Set<BlockPos> visited = Sets.newHashSet();
		while(!toCheck.isEmpty()) {
			final List<TileEntity> neighbors = this.neighbors(toCheck.get(0).getBlockPos(), Direction.values(), visited);
			neighbors.removeIf(te -> te instanceof UPMTile);
			neighbors.forEach(te ->
			NetworkMember.from(te).ifPresent(member -> {
				members.put(member, te.getBlockPos());
				final Direction[] childDirs = member.childDirections();
				if(childDirs.length != 0)
					toCheck.addAll(this.neighbors(te.getBlockPos(), childDirs, visited));
			})
					);
			toCheck.remove(0);
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
			final NetworkMember intMember = NetworkMember.from(te).orElseThrow(() ->
			new IllegalStateException("Error deserializing EnergyNetwork. NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not exist!"));
			if(member == null) {
				member = intMember;
				memberTE = te;
			}else if(!member.isSameStorage(intMember))
				throw new IllegalStateException("Error deserializing EnergyNetwork. NetworkMember at "+pos+" for TE "+te.getType().getRegistryName()+" does not match NetworkMember at "+memberTE.getBlockPos()+" for TE "+memberTE.getType().getRegistryName());
		}
		return member;
	}

	private List<TileEntity> neighbors(final BlockPos pos, final Direction[] dirs, final Set<BlockPos> visited){
		final List<TileEntity> tiles = Lists.newArrayList();
		for(final Direction dir:dirs) {
			final BlockPos neighborPos = pos.relative(dir);
			if(!visited.contains(neighborPos)) {
				visited.add(neighborPos);
				final TileEntity te = this.upm.getLevel().getBlockEntity(neighborPos);
				if(te != null)
					tiles.add(te);
			}
		}
		return tiles;
	}

}
