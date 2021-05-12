package me.superckl.upm.integration.immersiveengineering;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class IEFluxTileWrapper{

	private final Optional<WeakReference<IImmersiveConnectable>> asConnection;
	private final Optional<WeakReference<IFluxProvider>> asProvider;
	private final Optional<WeakReference<IFluxReceiver>> asReceiver;

	private IEFluxTileWrapper(final IImmersiveConnectable connection, final IFluxProvider provider, final IFluxReceiver receiver) {
		this.asConnection = Optional.ofNullable(connection).map(WeakReference::new);
		this.asProvider = Optional.ofNullable(provider).map(WeakReference::new);
		this.asReceiver = Optional.ofNullable(receiver).map(WeakReference::new);

	}

	public boolean isConnection() {
		return this.asConnection != null;
	}

	public boolean isReceiver() {
		return this.asReceiver != null;
	}

	public boolean isProvider() {
		return this.asProvider != null;
	}

	public IImmersiveConnectable connection() {
		return this.asConnection.get().get();
	}

	public IFluxProvider provider() {
		return this.asProvider.get().get();
	}

	public IFluxReceiver receiver() {
		return this.asReceiver.get().get();
	}

	@Override
	public boolean equals(final Object obj) {
		if(!(obj instanceof IEFluxTileWrapper))
			return false;
		if(this == obj)
			return true;
		final IEFluxTileWrapper wrapper = (IEFluxTileWrapper) obj;
		return this.asConnection.equals(wrapper.asConnection) &&
				this.asProvider.equals(wrapper.asProvider) &&
				this.asReceiver.equals(wrapper.asReceiver);
	}

	public int storage(final Direction side) {
		if(this.isProvider())
			return this.provider().getMaxEnergyStored(side);
		return this.receiver().getMaxEnergyStored(side);
	}

	public int stored(final Direction side) {
		if(this.isProvider())
			return this.provider().getEnergyStored(side);
		return this.receiver().getEnergyStored(side);
	}

	public Collection<Connection> connections(final LocalWireNetwork network){
		if(this.isConnection())
			return this.connection().getConnectionPoints().stream().map(network::getConnections).flatMap(Collection::stream).collect(Collectors.toSet());
		return Collections.emptySet();
	}

	public int insertEnergy(final Direction dir, final int amount) {
		if(this.isReceiver())
			return this.receiver().receiveEnergy(dir, amount, false);
		return 0;
	}

	public int extractEnergy(final Direction dir, final int amount) {
		if(this.isProvider())
			return this.provider().extractEnergy(dir, amount, false);
		return 0;
	}

	public boolean connectsEnergy(final Direction from) {
		if(this.isProvider())
			return this.provider().canConnectEnergy(from);
		if(this.isReceiver())
			return this.receiver().canConnectEnergy(from);
		return false;
	}

	public boolean isValid() {
		return this.asConnection.map(ref -> ref.get() != null).orElse(true) &&
				this.asProvider.map(ref -> ref.get() != null).orElse(true) &&
				this.asReceiver.map(ref -> ref.get() != null).orElse(true);
	}

	public static IEFluxTileWrapper from(final TileEntity entity) {
		final IImmersiveConnectable connection = entity instanceof IImmersiveConnectable ? (IImmersiveConnectable) entity:null;
		final IFluxProvider provider = entity instanceof IFluxProvider ? (IFluxProvider) entity:null;
		final IFluxReceiver receiver = entity instanceof IFluxReceiver ? (IFluxReceiver) entity:null;
		return new IEFluxTileWrapper(connection, provider, receiver);
	}

	public static boolean matches(final TileEntity entity) {
		return entity instanceof IFluxProvider || entity instanceof IFluxReceiver || entity instanceof IImmersiveConnectable;
	}

}
