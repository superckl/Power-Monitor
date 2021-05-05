package me.superckl.upm.integration.immersiveengineering;

import java.util.EnumSet;
import java.util.Optional;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import me.superckl.upm.network.member.ForgeEnergyNetworkMember;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.network.member.NetworkMemberResolver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class IENetworkMember extends ForgeEnergyNetworkMember{

	public IENetworkMember(final IEnergyStorage storage, final MemberType type) {
		super(storage, type);
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

	public static class Resolver extends NetworkMemberResolver<IENetworkMember>{

		@Override
		public Optional<IENetworkMember> getNetworkMember(final TileEntity tile, final Direction side) {
			if(tile instanceof IFluxConnection) {
				final LazyOptional<IEnergyStorage> storage = tile.getCapability(CapabilityEnergy.ENERGY, side);
				return storage.map(iEnergy -> new IENetworkMember(iEnergy, this.typeFromTag(tile.getType())));
			}
			return Optional.empty();
		}

		@Override
		public int getPriority(final TileEntity tile) {
			return tile instanceof IFluxConnection ? 1:-1;
		}

	}

}
