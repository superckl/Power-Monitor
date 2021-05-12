package me.superckl.upm.integration.immersiveengineering;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.NetworkMember;
import me.superckl.upm.api.NetworkMemberResolver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class IEFluxNetworkMember extends NetworkMember{

	private final IEFluxTileWrapper wrapper;
	private final Direction side;

	public IEFluxNetworkMember(final TileEntity tile, final Direction side, final MemberType type) {
		super(tile, type);
		this.wrapper = IEFluxTileWrapper.from(tile);
		this.side = side;
	}

	@Override
	public long getMaxStorage() {
		return this.wrapper.storage(this.side);
	}

	@Override
	public long getCurrentEnergy() {
		return this.wrapper.stored(this.side);
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
		return member instanceof IEFluxNetworkMember && this.wrapper.equals(((IEFluxNetworkMember)member).wrapper);
	}

	@Override
	public Direction[] connectingDirections() {
		final Set<Direction> dirs = EnumSet.noneOf(Direction.class);
		for(final Direction dir:super.connectingDirections())
			if(this.wrapper.connectsEnergy(dir))
				dirs.add(dir);
		return dirs.toArray(new Direction[dirs.size()]);
	}

	@Override
	public Set<BlockPos> getConnections() {
		final LocalWireNetwork network = GlobalWireNetwork.getNetwork(this.tileEntity.get().getLevel()).getNullableLocalNet(this.tileEntity.get().getBlockPos());
		if(network != null) {
			final Set<BlockPos> positions = Sets.newHashSet(this.tileEntity.get().getBlockPos());
			this.wrapper.connections(network).forEach(conn -> {
				positions.add(conn.getEndA().getPosition());
				positions.add(conn.getEndB().getPosition());
			});
			return positions;
		}
		return super.getConnections();
	}

	@Override
	public boolean connects(final NetworkMember other, final Optional<Direction> side, final Optional<MemberType> overrideType,
			final Optional<MemberType> adjacentOverrideType) {
		if(super.connects(other, side, overrideType, adjacentOverrideType)) {
			if(side.isPresent() && this.wrapper.connectsEnergy(side.get()))
				return true;
			final LocalWireNetwork network = GlobalWireNetwork.getNetwork(this.tileEntity.get().getLevel()).getNullableLocalNet(this.tileEntity.get().getBlockPos());
			if(network != null) {
				final BlockPos otherPos = other.getTileEntity().getBlockPos();
				return network.getConnections(this.tileEntity.get().getBlockPos()).stream().anyMatch(conn ->
				conn.getEndA().getPosition().equals(otherPos) || conn.getEndB().getPosition().equals(otherPos));
			}
		}
		return false;
	}

	@Override
	public boolean valid() {
		return super.valid() && this.wrapper.isValid();
	}

	public static class Resolver extends NetworkMemberResolver<IEFluxNetworkMember>{

		@Override
		public Optional<IEFluxNetworkMember> getNetworkMember(final TileEntity tile, final Direction side) {
			if(IEFluxTileWrapper.matches(tile))
				return Optional.of(new IEFluxNetworkMember(tile, side, this.typeFromTag(tile.getType())));
			return Optional.empty();
		}

		@Override
		public int getPriority(final TileEntity tile) {
			if(IEFluxTileWrapper.matches(tile))
				return 1;
			return 0;
		}

	}

}
