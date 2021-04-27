package me.superckl.upm.network;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.Getter;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.network.member.NetworkMember;
import me.superckl.upm.network.member.WrappedNetworkMember;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.packet.UpdateEnergyPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.PacketDistributor;

public class NetworkCache {

	@Getter
	private final List<WrappedNetworkMember> members;
	private final Map<MemberType, Long> typeStorages = new EnumMap<>(MemberType.class);
	private final Map<MemberType, Long> typeStored = new EnumMap<>(MemberType.class);
	//private Map<MemberType, Long> typeStorages0 = new EnumMap<>(MemberType.class);
	private Map<MemberType, Long> typeStored0 = new EnumMap<>(MemberType.class);
	private long totalStorage;
	private long totalStored;
	private long totalStored0;
	//private long totalStorage0;

	private boolean hasRefreshed;

	public NetworkCache(final List<WrappedNetworkMember> members) {
		this.members = members;
	}

	public long getTotalStorage() {
		return this.totalStorage;
	}

	public long getTotalStored() {
		return this.totalStored;
	}

	public long getStorage(final MemberType type) {
		return this.typeStorages.get(type);
	}

	public long getStored(final MemberType type) {
		return this.typeStored.get(type);
	}

	public long deltaTotalStored() {
		if(!this.hasRefreshed)
			return 0;
		return this.totalStored - this.totalStored0;
	}

	public long deltaStored(final MemberType type) {
		if(!this.hasRefreshed)
			return 0;
		return this.typeStored.get(type)-this.typeStored0.get(type);
	}

	public void refresh(final Supplier<Chunk> upmChunk, final BlockPos upmPos) {
		if(this.hasRefreshed)
			this.backup();
		final Map<MemberType, Long> storageChanges = new EnumMap<>(MemberType.class);
		final Map<MemberType, Long> storedChanges = new EnumMap<>(MemberType.class);

		this.totalStorage = 0;
		this.totalStored = 0;
		for(final MemberType type:MemberType.values()) {
			final List<NetworkMember> unwrapped = this.members.stream().filter(member -> member.getType() == type).map(WrappedNetworkMember::getMember).collect(Collectors.toList());
			final long typeStorage = unwrapped.stream().mapToLong(NetworkMember::getMaxStorage).sum();
			final long typeStored = unwrapped.stream().mapToLong(NetworkMember::getCurrentEnergy).sum();
			final Long prevStorage = this.typeStorages.put(type, typeStorage);
			if(prevStorage == null || prevStorage != typeStorage)
				storageChanges.put(type, typeStorage);
			final Long prevStored = this.typeStored.put(type, typeStored);
			if(prevStored == null || prevStored != typeStored)
				storedChanges.put(type, typeStored);
			this.totalStorage += typeStorage;
			this.totalStored += typeStored;
		}
		if(upmChunk != null)
			UPMPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(upmChunk), new UpdateEnergyPacket(upmPos, storageChanges, storedChanges));
		if(!this.hasRefreshed) {
			this.backup();
			this.hasRefreshed = true;
		}
	}

	public void clientUpdate(final Map<MemberType, Long> storage, final Map<MemberType, Long> stored) {
		if(this.hasRefreshed)
			this.backup();
		this.typeStorages.putAll(storage);
		this.typeStored.putAll(stored);
		this.totalStorage = this.typeStorages.values().stream().mapToLong(Long::longValue).sum();
		this.totalStored = this.typeStored.values().stream().mapToLong(Long::longValue).sum();
		if(!this.hasRefreshed) {
			this.backup();
			this.hasRefreshed = true;
		}
	}

	private void backup() {
		//this.typeStorages0 = new EnumMap<>(this.typeStorages);
		this.typeStored0 = new EnumMap<>(this.typeStored);
		//this.totalStorage0 = this.totalStorage;

		this.totalStored0 = this.totalStored;
	}

}
