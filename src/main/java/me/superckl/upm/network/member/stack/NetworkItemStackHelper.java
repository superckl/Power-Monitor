package me.superckl.upm.network.member.stack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.network.member.wrapper.WrappedNetworkMember;
import net.minecraft.item.ItemStack;

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

	public abstract boolean add(WrappedNetworkMember member);

	public abstract List<ItemStack> toStacks();

	public abstract Collection<WrappedNetworkMember> getMembers();

	public static Optional<? extends NetworkItemStackHelper> from(final WrappedNetworkMember member) {
		final Optional<SimpleMemberList> simple =  SimpleMemberList.from(member);
		if(simple.isPresent())
			return simple;
		return MultiblockMember.from(member);
	}

}