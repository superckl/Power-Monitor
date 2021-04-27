package me.superckl.upm.network.member.stack;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import me.superckl.upm.network.member.WrappedNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MultiblockMember extends NetworkItemStackHelper{

	private final WrappedNetworkMember member;
	private final Reference2IntMap<Block> blocks;

	private MultiblockMember(final WrappedNetworkMember member, final Reference2IntMap<Block> blocks) {
		super(member.getType());
		this.member = member;
		this.blocks = blocks;
	}

	@Override
	public boolean add(final WrappedNetworkMember member, final World world) {
		return false;
	}

	//TODO this should support multiple itemstacks and cycling
	@Override
	public ItemStack toStack() {
		final Block choice = this.blocks.keySet().iterator().next();
		return new ItemStack(choice.asItem(), this.blocks.getInt(choice));
	}

	@Override
	public boolean accepts(final WrappedNetworkMember member) {
		return false;
	}

	@Override
	public Collection<WrappedNetworkMember> getMembers() {
		return ImmutableList.of(this.member);
	}

	public static MultiblockMember from(final WrappedNetworkMember member, final World world) {
		final Reference2IntMap<Block> blocks = new Reference2IntArrayMap<>();
		member.getPositions().forEach(pos -> {
			blocks.mergeInt(world.getBlockState(pos).getBlock(), 1, (val1, val2) -> val1+val2);
		});
		return new MultiblockMember(member, blocks);
	}

}
