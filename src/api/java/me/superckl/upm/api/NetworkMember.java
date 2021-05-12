package me.superckl.upm.api;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class NetworkMember{

	protected final WeakReference<TileEntity> tileEntity;
	protected final MemberType type;

	public NetworkMember(final TileEntity tile, final MemberType type) {
		this.tileEntity = new WeakReference<>(tile);
		this.type = type;
	}

	/**
	 * @return The max energy storage (in FE) of this member.
	 */
	public abstract long getMaxStorage();

	/**
	 * @return The energy stored (in FE) in this member.
	 */
	public abstract long getCurrentEnergy();

	/**
	 * @return A set of all positions that may possibly have connections.
	 * This generally includes the position of this member.
	 */
	public Set<BlockPos> getConnections(){
		return Sets.newHashSet(this.tileEntity.get().getBlockPos());
	}

	/**
	 * @return All possible directions that this member can directly connect to
	 */
	public Direction[] connectingDirections() {
		return this.type.connects() ? Direction.values():new Direction[0];
	}

	/**
	 * Determines if this network members "connects" to the passed network member
	 * @param other The other network member that this member might connect to
	 * @param side The direction of the connection (from this member), which might not exist
	 * @param overrideType The overriden type for this member, which might not exist
	 * @param adjacentOverrideType The overriden type for the other member, which might not exist
	 * @return If this member connects to the other member
	 */
	public boolean connects(final NetworkMember other, final Optional<Direction> side, final Optional<MemberType> overrideType, final Optional<MemberType> adjacentOverrideType) {
		return overrideType.orElseGet(this::getType).connects();
	}

	/**
	 * @return If UPM should perform injection checking to see if multiple members share the same storage.
	 * Should be false if you properly override {@link #isSameStorage(NetworkMember)}.
	 */
	public boolean requiresInjectionCheck() {
		return true;
	}

	/**
	 * @return The tile entity associated with this network member
	 */
	public TileEntity getTileEntity() {
		return this.tileEntity.get();
	}

	/**
	 * @return The type of this network member
	 */
	public MemberType getType() {
		return this.type;
	}

	/**
	 * Attempts to add (insert) energy to this member
	 * @param energy The amount of energy to add
	 * @return The energy that was actually added
	 */
	public abstract int addEnergy(int energy);

	/**
	 * Attempts to remove (extract) energy from this member
	 * @param energy The amount of energy to remove
	 * @return The energy that was actually removed
	 */
	public abstract int removeEnergy(int energy);

	/**
	 * Determines if this member shares the same storage as the passed member. This is very important
	 * to override properly, since it allows UPM to avoid injection checking if
	 * {@link #requiresInjectionCheck()} returns false.
	 * @param member The other network member to check
	 * @return If this network member shares the same storage as the passed member
	 */
	public abstract boolean isSameStorage(NetworkMember member);

	/**
	 * @return If this network member is still valid and can be used to monitor storage
	 */
	public boolean valid() {
		return this.tileEntity.get() != null;
	}

	public static Optional<? extends NetworkMember> from(@Nullable final TileEntity te, final Direction side) {
		if(te == null)
			return Optional.empty();
		final Iterator<NetworkMemberResolver<?>> it = UPMAPI.RESOLVER_REGISTRY.get().getValues().stream().sorted(NetworkMemberResolver.REVERSE_COMPARATOR_FACTORY.apply(te)).iterator();
		while(it.hasNext()) {
			final Optional<? extends NetworkMember> opt = it.next().getNetworkMember(te, side);
			if(opt.isPresent())
				return opt;
		}
		return Optional.empty();
	}

}
