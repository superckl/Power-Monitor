package me.superckl.upm.network.member.stack;

import java.util.Collection;

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

	public abstract ItemStack toStack();

	public abstract Collection<WrappedNetworkMember> getMembers();

	public static NetworkItemStackHelper from(final WrappedNetworkMember member, final World world) {
		if(member.getPositions().size() == 1)
			return SimpleMemberList.from(member, world);
		return MultiblockMember.from(member, world);
	}

}