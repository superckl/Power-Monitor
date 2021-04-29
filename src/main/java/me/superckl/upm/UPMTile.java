package me.superckl.upm;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import lombok.Getter;
import me.superckl.upm.network.EnergyNetwork;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.packet.UPMScanStatePacket;
import me.superckl.upm.screen.UPMScreen;
import me.superckl.upm.screen.UPMScreenModeType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class UPMTile extends TileEntity implements ITickableTileEntity{

	public static final Set<UPMTile> LOADED_TILES = Collections.newSetFromMap(new IdentityHashMap<>());
	public static BiFunction<UPMTile, CompoundNBT, Supplier<EnergyNetwork>> ENERGY_NETWORK_DESERIALIZER = (tile, nbt) -> () -> {
		final EnergyNetwork network = new EnergyNetwork(tile);
		network.deserializeNBT(nbt);
		return network;
	};

	private final int scanDelay = 60;

	private int scanTicks = 0;
	private EnergyNetwork network = null;
	/**
	 * EnergyNetworks require accessing (possibly many) tile entities to be deserialized, which
	 * is not possible on world load when tile entities are deserialized. Thus, deserialization of
	 * the tile entity stores the energy network's deserialization in a supplier to be called when needed
	 * after world load.
	 */
	private Supplier<EnergyNetwork> networkSupplier;
	private boolean scanRequested;

	@Getter
	private final Map<TileEntityType<?>, MemberType> typeOverrides = new IdentityHashMap<>();

	public UPMTile() {
		super(ModRegisters.UPM_TILE_TYPE.get());
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if(!this.level.isClientSide)
			UPMTile.LOADED_TILES.add(this);
	}

	@SuppressWarnings("resource")
	@Override
	public void setRemoved() {
		UPMTile.LOADED_TILES.remove(this);
		if(this.level.isClientSide && ClientHelper.getMinecraft().screen instanceof UPMScreen)
			((UPMScreen)ClientHelper.getMinecraft().screen).onUPMRemoved(this);
		super.setRemoved();
	}

	@Override
	public void tick() {
		/*
		 * Check for a pending EnergyNetwork deserialization.
		 * We can't deserialize networks on initial world load because
		 * it requires accessing other TileEntities
		 */
		this.handlePendingNetwork();
		if(this.level.isClientSide)
			return;
		if(this.scanTicks > 0) {
			this.scanTicks--;
			if(this.scanTicks == 0)
				UPMPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.worldPosition)),
						new UPMScanStatePacket(this.worldPosition, true));
		}else if(this.scanTicks <= 0 && this.scanRequested)
			this.scanNetwork();
				
	}

	public EnergyNetwork getNetwork() {
		this.handlePendingNetwork();
		return this.network;
	}

	@SuppressWarnings("resource")
	public void handlePendingNetwork() {
		if(this.networkSupplier != null) {
			this.network = this.networkSupplier.get();
			this.networkSupplier = null;
			if(this.level.isClientSide && ClientHelper.getMinecraft().screen instanceof UPMScreen)
				((UPMScreen)ClientHelper.getMinecraft().screen).onNetworkChanged(this);
		}
	}

	public boolean requestScan() {
		if(this.level.isClientSide)
			return false;
		if(this.scanTicks <= 0) {
			this.scanNetwork();
			return true;
		}
		this.scanRequested = true;
		return false;
	}

	private void scanNetwork() {
		if(this.level.isClientSide)
			return;
		if(this.network == null)
			this.network = new EnergyNetwork(this);
		if(this.network.scan()) {
			this.setChanged();
			this.syncToClientLight(null);
		}
		this.resetScanDelay();
		this.scanRequested = false;
	}

	public void resetScanDelay() {
		this.scanTicks = this.scanDelay;
		UPMPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.worldPosition)),
				new UPMScanStatePacket(this.worldPosition, false));
	}

	public void clientScanState(final boolean state) {
		if(!this.level.isClientSide)
			return;
		if(state)
			this.scanTicks = 0;
		else
			this.scanTicks = this.scanDelay;
	}

	public boolean canScan() {
		return this.scanTicks <= 0;
	}

	public void setTypeOverrides(final Map<TileEntityType<?>, MemberType> overrides) {
		this.typeOverrides.putAll(overrides);
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
	public static final String TYPE_OVERRIDE_KEY = "overrides";
	public static final String SCAN_STATE_KEY = "scan";

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		if(this.level.isClientSide)
			return null;
		final CompoundNBT nbt = new CompoundNBT();
		if(this.network != null)
			nbt.put(UPMTile.NETWORK_KEY, this.network.serializeNBT());
		nbt.putBoolean(UPMTile.SCAN_STATE_KEY, this.canScan());
		return new SUpdateTileEntityPacket(this.worldPosition, -1, nbt);
	}

	@Override
	public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt) {
		if(!this.level.isClientSide)
			return;
		if(pkt.getTag().contains(UPMTile.NETWORK_KEY, Constants.NBT.TAG_COMPOUND)) {
			final CompoundNBT networkNBT = pkt.getTag().getCompound(UPMTile.NETWORK_KEY);
			this.networkSupplier = UPMTile.ENERGY_NETWORK_DESERIALIZER.apply(this, networkNBT);
		}else
			this.network = null;
		this.clientScanState(pkt.getTag().getBoolean(UPMTile.SCAN_STATE_KEY));
	}

	@Override
	public CompoundNBT save(final CompoundNBT nbt) {
		final CompoundNBT data = new CompoundNBT();
		if(this.network != null)
			data.put(UPMTile.NETWORK_KEY, this.network.serializeNBT());
		if(!this.typeOverrides.isEmpty()) {
			final CompoundNBT typeOverrides = new CompoundNBT();
			this.typeOverrides.forEach((loc, type) -> typeOverrides.putString(loc.getRegistryName().toString(), type.name()));
			data.put(UPMTile.TYPE_OVERRIDE_KEY, typeOverrides);
		}
		nbt.put(UPM.MOD_ID, data);
		return super.save(nbt);
	}

	@Override
	public void load(final BlockState state, final CompoundNBT nbt) {
		final CompoundNBT data = nbt.getCompound(UPM.MOD_ID);
		if(data.contains(UPMTile.NETWORK_KEY, Constants.NBT.TAG_COMPOUND)) {
			final CompoundNBT networkNBT = data.getCompound(UPMTile.NETWORK_KEY);
			this.networkSupplier = UPMTile.ENERGY_NETWORK_DESERIALIZER.apply(this, networkNBT);
		}
		if(data.contains(UPMTile.TYPE_OVERRIDE_KEY, Constants.NBT.TAG_COMPOUND)) {
			final CompoundNBT typeOverrides = data.getCompound(UPMTile.TYPE_OVERRIDE_KEY);
			this.typeOverrides.clear();
			typeOverrides.getAllKeys().forEach(loc -> this.typeOverrides.put(ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(loc)), MemberType.valueOf(typeOverrides.getString(loc))));
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
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}

	}

}
