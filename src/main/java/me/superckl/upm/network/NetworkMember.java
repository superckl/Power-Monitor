package me.superckl.upm.network;

import java.util.Optional;

import me.superckl.upm.ModRegisters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public abstract class NetworkMember{

	public abstract int getMaxStorage();

	public abstract int getCurrentEnergy();

	public abstract MemberType type();

	public Direction[] childDirections() {
		return Direction.values();
	}

	public abstract boolean isSameStorage(NetworkMember member);

	public abstract boolean valid();

	public static Optional<? extends NetworkMember> from(final TileEntity te) {
		for(final NetworkMemberResolver<?> resolver:ModRegisters.RESOLVER_REGISTRY.get().getValues()) {
			final Optional<? extends NetworkMember> opt = resolver.getNetworkMember(te);
			if(opt.isPresent())
				return opt;
		}
		return Optional.empty();
	}

}
