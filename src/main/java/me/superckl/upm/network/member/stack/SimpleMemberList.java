package me.superckl.upm.network.member.stack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Iterables;

import lombok.Getter;
import me.superckl.upm.network.member.WrappedNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SimpleMemberList extends NetworkItemStackHelper{

	@Getter
	private final Block block;
	@Getter
	private final Set<WrappedNetworkMember> members = Collections.newSetFromMap(new IdentityHashMap<>());

	protected SimpleMemberList(final Block block, final WrappedNetworkMember member) {
		super(member.getType());
		this.block = block;
		this.members.add(member);
	}

	@Override
	public boolean add(final WrappedNetworkMember member, final World world) {
		final Optional<SimpleMemberList> simple = SimpleMemberList.from(member, world).filter(list -> list.block == this.block);
		if(simple.isPresent()) {
			this.members.addAll(simple.get().members);
			return true;
		}
		return false;
	}

	@Override
	public ItemStack toStack() {
		return new ItemStack(this.block.asItem(), this.members.stream().mapToInt(member -> member.getPositions().size()).sum());
	}

	public static Optional<SimpleMemberList> from(final WrappedNetworkMember member, final World world) {
		final Set<Block> blocks = Collections.newSetFromMap(new IdentityHashMap<>());
		member.getPositions().keySet().forEach(pos -> blocks.add(world.getBlockState(pos).getBlock()));
		if(blocks.size() == 1)
			return Optional.of(new SimpleMemberList(Iterables.getOnlyElement(blocks), member));
		return Optional.empty();
	}

}