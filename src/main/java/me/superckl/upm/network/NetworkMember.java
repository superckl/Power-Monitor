package me.superckl.upm.network;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import me.superckl.upm.LogHelper;
import me.superckl.upm.ModRegisters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public abstract class NetworkMember{

	private static final Set<ResourceLocation> WARNED = Sets.newHashSet();

	public abstract int getMaxStorage();

	public abstract int getCurrentEnergy();

	public abstract MemberType type();

	public Direction[] childDirections() {
		return Direction.values();
	}

	public NetworkMember resolve(final Map<Direction, NetworkMember> sidedMembers, final TileEntity entity) {
		if(!NetworkMember.WARNED.contains(entity.getType().getRegistryName())) {
			final Collection<NetworkMember> members = sidedMembers.values();
			for(final NetworkMember member:members)
				if(!this.isSameStorage(member)) {
					LogHelper.warn("Error scanning network. TE "+entity.getType().getRegistryName()+" at "+entity.getBlockPos()+" provides different storage on sides than unsided storage!"
							+ "Defaulting to unsided storage.");
					NetworkMember.WARNED.add(entity.getType().getRegistryName());
					break;
				}
		}
		return this;
	}

	public boolean connects(final NetworkMember adjacent, final Direction side) {
		return this.canExtract() && adjacent.canInsert() || this.canInsert() && adjacent.canExtract();
	}

	public abstract boolean canExtract();

	public abstract boolean canInsert();

	public abstract boolean isSameStorage(NetworkMember member);

	public abstract boolean valid();

	public static Optional<? extends NetworkMember> from(@Nullable final TileEntity te, final Direction side) {
		if(te == null)
			return Optional.empty();
		for(final NetworkMemberResolver<?> resolver:ModRegisters.RESOLVER_REGISTRY.get().getValues()) {
			final Optional<? extends NetworkMember> opt = resolver.getNetworkMember(te, side);
			if(opt.isPresent())
				return opt;
		}
		return Optional.empty();
	}

}
