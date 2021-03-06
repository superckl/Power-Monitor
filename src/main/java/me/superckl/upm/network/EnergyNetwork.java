package me.superckl.upm.network;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.LogHelper;
import me.superckl.upm.UPMTile;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.network.member.wrapper.PhantomWrapper;
import me.superckl.upm.network.member.wrapper.PositionBasedWrapper;
import me.superckl.upm.network.member.wrapper.WrappedNetworkMember;
import me.superckl.upm.util.NetworkUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

@RequiredArgsConstructor
public class EnergyNetwork{

	public static final String MEMBERS_KEY = "members";
	public static final String STORAGE_KEY = "storage";
	public static final String STORED_KEY = "stored";

	protected final UPMTile upm;
	private NetworkCache networkCache;
	private boolean scanScheduled;

	//This method is private to ensure it is only called when the network ticks
	//at end of world ticking
	private boolean scan() {
		return this.updateMembers(NetworkUtil.scan(this.upm));
	}

	protected boolean updateMembers(final List<? extends WrappedNetworkMember> wrapped) {
		boolean changed;
		if(this.networkCache == null)
			changed = true;
		else {
			final List<? extends WrappedNetworkMember> cachedMembers = this.networkCache.getMembers();
			changed = cachedMembers == null || wrapped.size() != cachedMembers.size();
			if(!changed) {
				//The lists are the same size, so check if they contain the same elements
				cachedMembers.removeAll(wrapped);
				changed = !cachedMembers.isEmpty();
			}
		}
		this.networkCache = new NetworkCache(wrapped);
		this.refreshEnergyStorage(false);
		return changed;
	}

	public void tick() {
		if(this.scanScheduled || !this.isValid()) {
			this.scan();
			this.scanScheduled = false;
			this.upm.resetScanDelay();
			this.upm.syncToClientLight(null);
		}
		this.refreshEnergyStorage(true);
	}

	public void scheduleScan() {
		this.scanScheduled = true;
	}

	private void refreshEnergyStorage(final boolean sync) {
		if(this.networkCache != null && !this.upm.getLevel().isClientSide)
			this.networkCache.refresh(sync ? () -> this.upm.getLevel().getChunkAt(this.upm.getBlockPos()):null, this.upm.getBlockPos());
	}

	public boolean isValid() {
		return this.getMembers().stream().allMatch(wrapped -> wrapped.getMember().valid());
	}

	public World getLevel() {
		return this.upm.getLevel();
	}

	public UPMTile getOwner() {
		return this.upm;
	}

	public long getTotalStorage() {
		if(this.networkCache == null)
			return 0;
		return this.networkCache.getTotalStorage();
	}

	public long getTotalStored() {
		if(this.networkCache == null)
			return 0;
		return this.networkCache.getTotalStored();
	}

	public long getStorage(final MemberType type) {
		if(this.networkCache == null)
			return 0;
		return this.networkCache.getStorage(type);
	}

	public long getStored(final MemberType type) {
		if(this.networkCache == null)
			return 0;
		return this.networkCache.getStored(type);
	}

	public long deltaTotalStored() {
		if(this.networkCache == null)
			return 0;
		return this.networkCache.deltaTotalStored();
	}

	public long deltaStored(final MemberType type) {
		if(this.networkCache == null)
			return 0;
		return this.networkCache.deltaStored(type);
	}

	@SuppressWarnings("resource")
	public void setEnergy(final Map<MemberType, Long> storage, final Map<MemberType, Long> stored) {
		if(!this.upm.getLevel().isClientSide)
			return;
		this.networkCache.clientUpdate(storage, stored);
	}

	public List<? extends WrappedNetworkMember> getMembers(){
		if(this.networkCache == null)
			return ImmutableList.of();
		return ImmutableList.copyOf(this.networkCache.getMembers());
	}

	public CompoundNBT serializeNBT(final boolean client) {
		final CompoundNBT nbt = new CompoundNBT();
		if(this.networkCache != null) {
			final ListNBT list = new ListNBT();
			this.networkCache.getMembers().forEach(member -> {
				list.add(member.serialize(client));
			});
			nbt.put(EnergyNetwork.MEMBERS_KEY, list);
			final CompoundNBT storage = new CompoundNBT();
			final CompoundNBT stored = new CompoundNBT();
			for(final MemberType type:MemberType.values()) {
				storage.putLong(type.name(), this.networkCache.getStorage(type));
				stored.putLong(type.name(), this.networkCache.getStored(type));
			}
			nbt.put(EnergyNetwork.STORAGE_KEY, storage);
			nbt.put(EnergyNetwork.STORED_KEY, stored);
		}
		return nbt;
	}

	public void deserializeNBT(final CompoundNBT nbt, final boolean client) {
		final List<WrappedNetworkMember> members = new ArrayList<>();
		final Map<MemberType, Long> storage = new EnumMap<>(MemberType.class);
		final Map<MemberType, Long> stored = new EnumMap<>(MemberType.class);
		if(nbt.contains(EnergyNetwork.MEMBERS_KEY, Constants.NBT.TAG_LIST)) {
			final ListNBT list = nbt.getList(EnergyNetwork.MEMBERS_KEY, Constants.NBT.TAG_COMPOUND);
			list.forEach(inbt -> {
				try {
					members.add(client ? PhantomWrapper.deserialize((CompoundNBT) inbt)
							:PositionBasedWrapper.deserialize((CompoundNBT) inbt));
				} catch (final IllegalStateException e) {
					LogHelper.error("Error deserializing network member!");
					e.printStackTrace();
				}
			});
			final CompoundNBT storageNBT = nbt.getCompound(EnergyNetwork.STORAGE_KEY);
			final CompoundNBT storedNBT = nbt.getCompound(EnergyNetwork.STORED_KEY);
			for(final MemberType type:MemberType.values()) {
				storage.put(type, storageNBT.getLong(type.name()));
				stored.put(type, storedNBT.getLong(type.name()));
			}
		}
		this.updateMembers(members);
		this.networkCache.clientUpdate(storage, stored);
	}

}
