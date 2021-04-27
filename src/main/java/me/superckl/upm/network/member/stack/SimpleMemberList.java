package me.superckl.upm.network.member.stack;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import lombok.Getter;
import me.superckl.upm.network.member.WrappedNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SimpleMemberList extends NetworkItemStackHelper{

	private final Block block;
	@Getter
	private final List<WrappedNetworkMember> members;

	protected SimpleMemberList(final Block block, final WrappedNetworkMember member) {
		super(member.getType());
		this.block = block;
		this.members = Lists.newArrayList(member);
	}

	@Override
	public boolean add(final WrappedNetworkMember member, final World world) {
		if(member.getPositions().size() == 1 && world.getBlockState(Iterables.getOnlyElement(member.getPositions())).getBlock() == this.block) {
			this.members.add(member);
			return true;
		}
		return false;
	}

	@Override
	public boolean accepts(final WrappedNetworkMember member) {
		return member.getPositions().size() == 1 && super.accepts(member);
	}

	@Override
	public ItemStack toStack() {
		return new ItemStack(this.block.asItem(), this.members.size());
	}

	public static SimpleMemberList from(final WrappedNetworkMember member, final World world) {
		final Block block = world.getBlockState(Iterables.getOnlyElement(member.getPositions())).getBlock();
		return new SimpleMemberList(block, member);
	}

}