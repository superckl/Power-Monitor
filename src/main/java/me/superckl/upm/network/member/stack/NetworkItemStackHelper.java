package me.superckl.upm.network.member.stack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.network.member.WrappedNetworkMember;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@RequiredArgsConstructor
public abstract class NetworkItemStackHelper {

	protected final MemberType type;
	protected MemberType overrideType;

	public boolean accepts(final WrappedNetworkMember member) {
		return member.getType() == this.type;
	}

	public void setType(final MemberType type) {
		this.overrideType = type;
		this.getMembers().forEach(member -> member.setType(type));
	}

	public MemberType getType() {
		return this.overrideType == null ? this.type:this.overrideType;
	}

	public boolean hasTypeOverride() {
		return this.overrideType != this.type;
	}

	public abstract boolean add(WrappedNetworkMember member, World world);

	public abstract List<ItemStack> toStacks();

	public abstract Collection<WrappedNetworkMember> getMembers();

	public static Optional<? extends NetworkItemStackHelper> from(final WrappedNetworkMember member, final World world) {
		final Optional<SimpleMemberList> simple =  SimpleMemberList.from(member, world);
		if(simple.isPresent())
			return simple;
		return MultiblockMember.from(member, world);
	}

}