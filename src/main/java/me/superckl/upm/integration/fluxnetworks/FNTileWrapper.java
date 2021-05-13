package me.superckl.upm.integration.fluxnetworks;

import java.lang.ref.WeakReference;
import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import sonar.fluxnetworks.api.device.IFluxDevice;
import sonar.fluxnetworks.api.device.IFluxStorage;

public class FNTileWrapper {

	private final WeakReference<IFluxDevice> asDevice;
	private final Optional<WeakReference<IFluxStorage>> asStorage;

	public FNTileWrapper(final IFluxDevice asDevice, final IFluxStorage asStorage) {
		this.asDevice = new WeakReference<>(asDevice);
		this.asStorage = Optional.ofNullable(asStorage).map(WeakReference::new);
	}

	@Override
	public boolean equals(final Object obj) {
		if(!(obj instanceof FNTileWrapper))
			return false;
		if(this == obj)
			return true;
		final FNTileWrapper wrapper = (FNTileWrapper) obj;
		return this.asDevice.equals(wrapper.asDevice) &&
				this.asStorage.equals(wrapper.asStorage);
	}

	public IFluxDevice device() {
		return this.asDevice.get();
	}

	public long storage() {
		return this.asStorage.isPresent() ? this.asStorage.get().get().getMaxTransferLimit():0;
	}

	public long stored() {
		return this.asStorage.isPresent() ? this.asStorage.get().get().getTransferBuffer():0;
	}

	public int insertEnergy(final Direction dir, final int amount) {
		return 0;
	}

	public int extractEnergy(final Direction dir, final int amount) {
		return 0;
	}

	public boolean valid() {
		return this.asDevice.get() != null && this.asStorage.map(ref -> ref.get() != null).orElse(true);
	}

	public static boolean matches(final TileEntity te) {
		return te instanceof IFluxDevice;
	}

	public static FNTileWrapper from(final TileEntity te) {
		final IFluxDevice asDevice = (IFluxDevice) te;
		final IFluxStorage asStorage = te instanceof IFluxStorage ? (IFluxStorage) te:null;
		return new FNTileWrapper(asDevice, asStorage);
	}
}
