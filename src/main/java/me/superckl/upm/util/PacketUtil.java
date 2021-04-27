package me.superckl.upm.util;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

public class PacketUtil {

	public static <K, V> void writeMap(final Map<K, V> map, final BiConsumer<PacketBuffer, K> keySerializer,
			final BiConsumer<PacketBuffer, V> valueSerializer, final PacketBuffer buffer) {
		buffer.writeVarInt(map.size());
		map.forEach((key, value) -> {
			keySerializer.accept(buffer, key);
			valueSerializer.accept(buffer, value);
		});
	}

	public static < K, V, M extends Map<K, V>> M readMap(final Supplier<? extends M> mapFactory, final Function<PacketBuffer, K> keyDeserializer,
			final Function<PacketBuffer, V> valueDeserializer, final PacketBuffer buffer) {
		final M map = mapFactory.get();
		final int num = buffer.readVarInt();
		for(int i = 0; i < num; i++)
			map.put(keyDeserializer.apply(buffer), valueDeserializer.apply(buffer));
		return map;
	}

}
