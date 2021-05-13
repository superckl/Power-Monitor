package me.superckl.upm.util;

import java.util.EnumMap;
import java.util.Map;

import lombok.Getter;
import me.superckl.upm.api.MemberType;

@Getter
public class StorageCache {

	private final Map<MemberType, Long> typeStorages = new EnumMap<>(MemberType.class);
	private final Map<MemberType, Long> typeStored = new EnumMap<>(MemberType.class);

	public Long getStorage(final MemberType type) {
		return this.typeStorages.getOrDefault(type, 0L);
	}

	public Long getStored(final MemberType type) {
		return this.typeStored.getOrDefault(type, 0L);
	}

	public long getTotalStorage() {
		return this.typeStorages.values().stream().mapToLong(Long::longValue).sum();
	}

	public long getTotalStored() {
		return this.typeStored.values().stream().mapToLong(Long::longValue).sum();
	}

}
