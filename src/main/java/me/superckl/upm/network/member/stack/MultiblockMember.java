package me.superckl.upm.network.member.stack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import me.superckl.upm.network.member.WrappedNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MultiblockMember extends NetworkItemStackHelper{

	private final List<WrappedNetworkMember> members;
	private final Reference2IntMap<Block> blocks;

	private MultiblockMember(final WrappedNetworkMember member, final Reference2IntMap<Block> blocks) {
		super(member.getType());
		this.members = Lists.newArrayList(member);
		this.blocks = blocks;
	}

	@Override
	public boolean add(final WrappedNetworkMember member, final World world) {
		final List<Block> blocks = member.getPositions().keySet().stream().map(pos -> world.getBlockState(pos).getBlock()).collect(Collectors.toList());
		if(blocks.stream().allMatch(this.blocks::containsKey)) {
			blocks.forEach(block -> this.blocks.mergeInt(block, 1, Integer::sum));
			this.members.add(member);
			return true;
		}
		return false;
	}

	//TODO this should support multiple itemstacks and cycling
	@Override
	public ItemStack toStack() {
		final Block choice = this.blocks.keySet().iterator().next();
		return new ItemStack(choice.asItem(), this.blocks.getInt(choice));
	}

	@Override
	public Collection<WrappedNetworkMember> getMembers() {
		return ImmutableList.copyOf(this.members);
	}

	public static Optional<MultiblockMember> from(final WrappedNetworkMember member, final World world) {
		final Reference2IntMap<Block> blocks = new Reference2IntArrayMap<>();
		member.getPositions().forEach((pos, dir) -> {
			blocks.mergeInt(world.getBlockState(pos).getBlock(), 1, Integer::sum);
		});
		return Optional.of(new MultiblockMember(member, blocks));
	}

}
