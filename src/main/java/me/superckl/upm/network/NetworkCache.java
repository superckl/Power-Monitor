package me.superckl.upm.network;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.NetworkMember;
import me.superckl.upm.network.member.WrappedNetworkMember;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.packet.UpdateEnergyPacket;
import me.superckl.upm.util.MovingAveragedDifferenceCache;
import me.superckl.upm.util.StorageCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.PacketDistributor;

public class NetworkCache {

	public static final int CACHE_POINTS = 10;

	@Getter
	private final List<WrappedNetworkMember> members;
	@Delegate
	private final StorageCache storageCache = new StorageCache();
	private final Map<MemberType, MovingAveragedDifferenceCache> typeStored0 = new EnumMap<>(MemberType.class);
	private final MovingAveragedDifferenceCache totalStored0 = new MovingAveragedDifferenceCache(NetworkCache.CACHE_POINTS);

	public NetworkCache(final List<WrappedNetworkMember> members) {
		this.members = members;
	}

	public long deltaTotalStored() {
		return this.totalStored0.get();
	}

	public long deltaStored(final MemberType type) {
		return this.typeStored0.computeIfAbsent(type, x -> new MovingAveragedDifferenceCache(NetworkCache.CACHE_POINTS)).get();
	}

	public void refresh(final Supplier<Chunk> upmChunk, final BlockPos upmPos) {
		final Map<MemberType, Long> storageChanges = new EnumMap<>(MemberType.class);
		final Map<MemberType, Long> storedChanges = new EnumMap<>(MemberType.class);

		for(final MemberType type:MemberType.values()) {
			final List<NetworkMember> unwrapped = this.members.stream()
					.filter(member -> member.getType() == type).map(WrappedNetworkMember::getMember).collect(Collectors.toList());
			final long typeStorage = unwrapped.stream().mapToLong(NetworkMember::getMaxStorage).sum();
			final long typeStored = unwrapped.stream().mapToLong(NetworkMember::getCurrentEnergy).sum();
			final Long prevStorage = this.storageCache.getTypeStorages().put(type, typeStorage);
			if(prevStorage == null || prevStorage != typeStorage)
				storageChanges.put(type, typeStorage);
			final Long prevStored = this.storageCache.getTypeStored().put(type, typeStored);
			if(prevStored == null || prevStored != typeStored)
				storedChanges.put(type, typeStored);
		}
		if(upmChunk != null)
			UPMPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(upmChunk), new UpdateEnergyPacket(upmPos, storageChanges, storedChanges));
		this.cache();
	}

	public void clientUpdate(final Map<MemberType, Long> storage, final Map<MemberType, Long> stored) {
		this.storageCache.getTypeStorages().putAll(storage);
		this.storageCache.getTypeStored().putAll(stored);
		this.cache();
	}

	private void cache() {
		for(final MemberType type:MemberType.values()) {
			final MovingAveragedDifferenceCache cache = this.typeStored0.computeIfAbsent(type, x -> new MovingAveragedDifferenceCache(NetworkCache.CACHE_POINTS));
			cache.cache(this.storageCache.getTypeStored().getOrDefault(type, 0L));
		}
		this.totalStored0.cache(this.storageCache.getTotalStored());
	}

}
