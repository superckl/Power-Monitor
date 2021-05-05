package me.superckl.upm.network.member;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.upm.LogHelper;
import me.superckl.upm.ModRegisters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

@RequiredArgsConstructor
public abstract class NetworkMember{

	@Getter
	protected final MemberType type;

	private static final Set<ResourceLocation> WARNED = Sets.newHashSet();

	public abstract long getMaxStorage();

	public abstract long getCurrentEnergy();

	public Multimap<BlockPos, Direction> connectionsFrom(final TileEntity te){
		final Multimap<BlockPos, Direction> map = MultimapBuilder.hashKeys(1).enumSetValues(Direction.class).build();
		//default to just looking in all directions from this point
		map.putAll(te.getBlockPos(), EnumSet.allOf(Direction.class));
		return map;
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

	public boolean connects(final NetworkMember adjacent, final Direction side, final Optional<MemberType> overrideType, final Optional<MemberType> adjacentOverrideType) {
		return overrideType.orElseGet(this::getType).connects();
	}

	public boolean requiresInjectionCheck() {
		return true;
	}

	public abstract int addEnergy(int energy);
	public abstract int removeEnergy(int energy);

	public abstract boolean isSameStorage(NetworkMember member);

	public abstract boolean valid();

	public static Optional<? extends NetworkMember> from(@Nullable final TileEntity te, final Direction side) {
		if(te == null)
			return Optional.empty();
		final Iterator<NetworkMemberResolver<?>> it = ModRegisters.RESOLVER_REGISTRY.get().getValues().stream().sorted(NetworkMemberResolver.REVERSE_COMPARATOR_FACTORY.apply(te)).iterator();
		while(it.hasNext()) {
			final Optional<? extends NetworkMember> opt = it.next().getNetworkMember(te, side);
			if(opt.isPresent())
				return opt;
		}
		return Optional.empty();
	}

}
