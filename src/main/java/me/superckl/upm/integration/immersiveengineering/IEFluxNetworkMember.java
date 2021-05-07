package me.superckl.upm.integration.immersiveengineering;

import java.util.EnumSet;
import java.util.Optional;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.network.member.NetworkMember;
import me.superckl.upm.network.member.NetworkMemberResolver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class IEFluxNetworkMember extends NetworkMember{

	private final IEFluxTileWrapper wrapper;
	private final Direction side;

	public IEFluxNetworkMember(final IEFluxTileWrapper wrapper, final Direction side, final MemberType type) {
		super(type);
		this.wrapper = wrapper;
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
		return this.wrapper.isReceiver() ? this.wrapper.receiver().receiveEnergy(this.side, energy, false):0;
	}

	@Override
	public int removeEnergy(final int energy) {
		return this.wrapper.isProvider() ? this.wrapper.provider().extractEnergy(this.side, energy, false):0;
	}

	@Override
	public boolean isSameStorage(final NetworkMember member) {
		return member instanceof IEFluxNetworkMember && this.wrapper.equals(((IEFluxNetworkMember)member).wrapper);
	}

	@Override
	public Multimap<BlockPos, Direction> connectionsFrom(final TileEntity te) {
		if(!(te instanceof IFluxConnection))
			return ImmutableMultimap.of();
		final LocalWireNetwork network = GlobalWireNetwork.getNetwork(te.getLevel()).getNullableLocalNet(te.getBlockPos());
		if(network != null) {
			final Multimap<BlockPos, Direction> map = MultimapBuilder.hashKeys(1).enumSetValues(Direction.class).build();
			network.getConnectors().forEach(pos -> map.putAll(pos, EnumSet.allOf(Direction.class)));
			return map;
		}
		return super.connectionsFrom(te);
	}

	@Override
	public boolean valid() {
		return this.wrapper.isValid();
	}

	public static class Resolver extends NetworkMemberResolver<IEFluxNetworkMember>{

		@Override
		public Optional<IEFluxNetworkMember> getNetworkMember(final TileEntity tile, final Direction side) {
			if(IEFluxTileWrapper.matches(tile))
				return Optional.of(new IEFluxNetworkMember(IEFluxTileWrapper.from(tile), side, this.typeFromTag(tile.getType())));
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
