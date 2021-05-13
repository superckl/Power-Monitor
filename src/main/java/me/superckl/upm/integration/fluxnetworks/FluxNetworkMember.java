package me.superckl.upm.integration.fluxnetworks;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.NetworkMember;
import me.superckl.upm.api.NetworkMemberResolver;
import me.superckl.upm.api.PositionUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.energy.CapabilityEnergy;
import sonar.fluxnetworks.api.network.FluxLogicType;
import sonar.fluxnetworks.api.network.IFluxNetwork;

public class FluxNetworkMember extends NetworkMember{

	private final FNTileWrapper wrapper;
	private final Direction side;

	public FluxNetworkMember(final TileEntity tile, final Direction side, final MemberType type) {
		super(tile, type);
		this.wrapper = FNTileWrapper.from(tile);
		this.side = side;
	}

	@Override
	public long getMaxStorage() {
		return this.wrapper.storage();
	}

	@Override
	public long getCurrentEnergy() {
		return this.wrapper.stored();
	}

	@Override
	public int addEnergy(final int energy) {
		return this.wrapper.insertEnergy(this.side, energy);
	}

	@Override
	public int removeEnergy(final int energy) {
		return this.wrapper.extractEnergy(this.side, energy);
	}

	@Override
	public boolean isSameStorage(final NetworkMember member) {
		return this.equals(member);
	}

	@Override
	public boolean requiresInjectionCheck() {
		return false;
	}

	@Override
	public boolean valid() {
		return super.valid() && this.wrapper.valid();
	}

	@Override
	public Set<GlobalPos> getConnections() {
		final IFluxNetwork network = this.wrapper.device().getNetwork();
		if(network != null)
			return network.getConnections(FluxLogicType.ANY).stream().filter(device -> device instanceof TileEntity &&
					device.isActive() && device.isChunkLoaded()).map(device -> PositionUtil.getGlobalPos((TileEntity) device))
					.collect(Collectors.toSet());
		return super.getConnections();
	}

	@Override
	public boolean connects(final NetworkMember other, final Optional<Direction> side, final Optional<MemberType> overrideType,
			final Optional<MemberType> adjacentOverrideType) {
		if(super.connects(other, side, overrideType, adjacentOverrideType)) {
			final IFluxNetwork network = this.wrapper.device().getNetwork();
			if(network != null && network.getConnectionByPos(PositionUtil.getGlobalPos(other.getTileEntity())).isPresent())
				return true;
			return side.map(dir -> this.tileEntity.get().getCapability(CapabilityEnergy.ENERGY, dir).isPresent()).orElse(false);
		}
		return false;
	}

	public static class Resolver extends NetworkMemberResolver<FluxNetworkMember>{

		@Override
		public Optional<FluxNetworkMember> getNetworkMember(final TileEntity tile, final Direction side) {
			if(FNTileWrapper.matches(tile))
				return Optional.of(new FluxNetworkMember(tile, side, this.typeFromTag(tile.getType())));
			return Optional.empty();
		}

		@Override
		public int getPriority(final TileEntity tile) {
			if(FNTileWrapper.matches(tile))
				return 1;
			return -1;
		}

	}

}
