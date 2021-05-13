package me.superckl.upm.network.member.stack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import lombok.Getter;
import me.superckl.upm.network.member.wrapper.WrappedNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class SimpleMemberList extends NetworkItemStackHelper{

	@Getter
	private final Block block;
	@Getter
	private final Set<WrappedNetworkMember> members = Collections.newSetFromMap(new IdentityHashMap<>());

	private List<ItemStack> stacks;

	protected SimpleMemberList(final Block block, final WrappedNetworkMember member) {
		super(member.getType());
		this.block = block;
		this.members.add(member);
	}

	@Override
	public boolean add(final WrappedNetworkMember member) {
		final Optional<SimpleMemberList> simple = SimpleMemberList.from(member).filter(list -> list.block == this.block);
		if(simple.isPresent()) {
			this.members.addAll(simple.get().members);
			this.stacks = null;
			return true;
		}
		return false;
	}

	@Override
	public List<ItemStack> toStacks() {
		if(this.stacks == null)
			this.stacks = ImmutableList.of(new ItemStack(this.block.asItem(), this.members.stream().mapToInt(member -> member.toBlocks().size()).sum()));
		return this.stacks;
	}

	public static Optional<SimpleMemberList> from(final WrappedNetworkMember member) {
		final Set<Block> blocks = Collections.newSetFromMap(new IdentityHashMap<>());
		member.toBlocks().forEach(blocks::add);
		if(blocks.size() == 1)
			return Optional.of(new SimpleMemberList(Iterables.getOnlyElement(blocks), member));
		return Optional.empty();
	}

}