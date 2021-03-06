package me.superckl.upm.network.member.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import me.superckl.upm.network.member.wrapper.WrappedNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class MultiblockMember extends NetworkItemStackHelper{

	private final List<WrappedNetworkMember> members;
	private final Reference2IntMap<Block> blocks;

	private List<ItemStack> stacks;

	private MultiblockMember(final WrappedNetworkMember member, final Reference2IntMap<Block> blocks) {
		super(member.getType());
		this.members = Lists.newArrayList(member);
		this.blocks = blocks;
	}

	@Override
	public boolean add(final WrappedNetworkMember member) {
		final Collection<Block> blocks = member.toBlocks();
		if(blocks.stream().allMatch(this.blocks::containsKey)) {
			blocks.forEach(block -> this.blocks.mergeInt(block, 1, Integer::sum));
			this.members.add(member);
			this.stacks = null;
			return true;
		}
		return false;
	}

	@Override
	public List<ItemStack> toStacks() {
		if(this.stacks == null) {
			final List<ItemStack> stacks = new ArrayList<>();
			Reference2IntMaps.fastForEach(this.blocks, entry -> {
				stacks.add(new ItemStack(entry.getKey().asItem(), entry.getIntValue()));
			});
			this.stacks = stacks;
		}
		return this.stacks;
	}

	@Override
	public Collection<WrappedNetworkMember> getMembers() {
		return ImmutableList.copyOf(this.members);
	}

	public static Optional<MultiblockMember> from(final WrappedNetworkMember member) {
		final Reference2IntMap<Block> blocks = new Reference2IntArrayMap<>();
		member.toBlocks().forEach(block -> {
			blocks.mergeInt(block, 1, Integer::sum);
		});
		return Optional.of(new MultiblockMember(member, blocks));
	}

}
