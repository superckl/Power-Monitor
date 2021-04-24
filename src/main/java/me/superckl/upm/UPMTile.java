package me.superckl.upm;

import java.util.function.Supplier;

import lombok.Getter;
import me.superckl.upm.screen.UPMScreen;
import me.superckl.upm.screen.UPMScreenModeType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.PacketDistributor;

public class UPMTile extends TileEntity implements ITickableTileEntity{

	private int scanDelay = 0;
	@Getter
	private EnergyNetwork network;
	private Supplier<EnergyNetwork> networkSupplier;

	public UPMTile() {
		super(ModRegisters.UPM_TILE_TYPE.get());
	}

	@Override
	public void tick() {
		/*
		 * Check for a pending EnergyNetwork deserialization.
		 * We can't deserialize networks on initial world load because
		 * it requires accessing other TileEntities
		 */
		if(this.networkSupplier != null) {
			this.network = this.networkSupplier.get();
			this.networkSupplier = null;
		}
		if(this.level.isClientSide)
			return;
		if(this.scanDelay > 0)
			this.scanDelay--;
	}

	public void scanNetwork() {
		if(this.level.isClientSide)
			return;
		if(this.scanDelay <= 0) {
			if(this.network == null)
				this.network = new EnergyNetwork(this);
			if(this.network.scan()) {
				this.setChanged();
				this.syncToClientLight(null);
			}
			this.scanDelay = 100;
		}
	}

	public UPMScreenModeType getScreenType() {
		return this.network == null ? UPMScreenModeType.NO_NETWORK : UPMScreenModeType.NETWORK;
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(final BlockState state, final CompoundNBT tag) {
		this.load(state, tag);
	}

	public void syncToClientLight(final ServerPlayerEntity player) {
		if(player == null)
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NO_RERENDER);
		else
			PacketDistributor.PLAYER.with(() -> player).send(this.getUpdatePacket());
	}

	public static final String NETWORK_KEY = "network";
	public static final String DELAY_KEY = "delay";

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		if(this.level.isClientSide)
			return null;
		final CompoundNBT nbt = new CompoundNBT();
		if(this.network != null)
			nbt.put(UPMTile.NETWORK_KEY, this.network.serializeNBT());
		return new SUpdateTileEntityPacket(this.worldPosition, -1, nbt);
	}

	@SuppressWarnings("resource")
	@Override
	public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt) {
		if(!this.level.isClientSide)
			return;
		if(pkt.getTag().contains(UPMTile.NETWORK_KEY, Constants.NBT.TAG_COMPOUND)) {
			this.network = new EnergyNetwork(this);
			this.network.deserializeNBT(pkt.getTag().getCompound(UPMTile.NETWORK_KEY));
		}else
			this.network = null;
		if(ClientHelper.getMinecraft().screen instanceof UPMScreen)
			((UPMScreen)ClientHelper.getMinecraft().screen).onNetworkChanged(this);
	}

	@Override
	public CompoundNBT save(final CompoundNBT nbt) {
		final CompoundNBT data = new CompoundNBT();
		data.putInt(UPMTile.DELAY_KEY, this.scanDelay);
		if(this.network != null)
			data.put(UPMTile.NETWORK_KEY, this.network.serializeNBT());
		nbt.put(UPM.MOD_ID, data);
		return super.save(nbt);
	}

	@Override
	public void load(final BlockState state, final CompoundNBT nbt) {
		final CompoundNBT data = nbt.getCompound(UPM.MOD_ID);
		this.scanDelay = data.getInt(UPMTile.DELAY_KEY);
		if(data.contains(UPMTile.NETWORK_KEY, Constants.NBT.TAG_COMPOUND)) {
			final CompoundNBT networkNBT = data.getCompound(UPMTile.NETWORK_KEY);
			final Supplier<EnergyNetwork> supplier = () -> {
				final EnergyNetwork network = new EnergyNetwork(this);
				network.deserializeNBT(networkNBT);
				return network;
			};
			if(this.level == null)
				this.networkSupplier = supplier;
			else
				this.network = supplier.get();
		}
		super.load(state, nbt);
	}

	private final LazyOptional<NoEnergyStorage> storage = LazyOptional.of(NoEnergyStorage::new);

	@Override
	public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side) {
		if(cap == CapabilityEnergy.ENERGY)
			return this.storage.cast();
		return super.getCapability(cap, side);
	}

	@Override
	protected void invalidateCaps() {
		this.storage.invalidate();
		super.invalidateCaps();
	}

	private static class NoEnergyStorage implements IEnergyStorage{

		@Override
		public int receiveEnergy(final int maxReceive, final boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(final int maxExtract, final boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return false;
		}

	}

}
