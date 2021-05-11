package me.superckl.upm.integration.immersiveengineering;

import java.lang.ref.WeakReference;
import java.util.Optional;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class IEFluxTileWrapper{

	private final Optional<WeakReference<IFluxConnection>> asConnection;
	private final Optional<WeakReference<IFluxProvider>> asProvider;
	private final Optional<WeakReference<IFluxReceiver>> asReceiver;

	private IEFluxTileWrapper(final IFluxConnection connection, final IFluxProvider provider, final IFluxReceiver receiver) {
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

	public IFluxConnection connection() {
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

	public boolean isValid() {
		return this.asConnection.map(ref -> ref.get() != null).orElse(true) &&
				this.asProvider.map(ref -> ref.get() != null).orElse(true) &&
				this.asReceiver.map(ref -> ref.get() != null).orElse(true);
	}

	public static IEFluxTileWrapper from(final TileEntity entity) {
		final IFluxConnection connection = entity instanceof IFluxConnection ? (IFluxConnection) entity:null;
		final IFluxProvider provider = entity instanceof IFluxProvider ? (IFluxProvider) entity:null;
		final IFluxReceiver receiver = entity instanceof IFluxReceiver ? (IFluxReceiver) entity:null;
		return new IEFluxTileWrapper(connection, provider, receiver);
	}

	public static boolean matches(final TileEntity entity) {
		//TODO need to consider a tile that's just a connector as well
		return entity instanceof IFluxProvider || entity instanceof IFluxReceiver;
	}

}
