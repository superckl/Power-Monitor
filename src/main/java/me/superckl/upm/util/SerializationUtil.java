package me.superckl.upm.util;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;

public class SerializationUtil {

	public static <K, V> void writeMap(final Map<? extends K, ? extends V> map, final BiConsumer<PacketBuffer, K> keySerializer,
			final BiConsumer<PacketBuffer, V> valueSerializer, final PacketBuffer buffer) {
		buffer.writeVarInt(map.size());
		map.forEach((key, value) -> {
			keySerializer.accept(buffer, key);
			valueSerializer.accept(buffer, value);
		});
	}

	public static <K, V, M extends Map<K, V>> M readMap(final Supplier<? extends M> mapFactory, final Function<PacketBuffer, ? extends K> keyDeserializer,
			final Function<PacketBuffer, ? extends V> valueDeserializer, final PacketBuffer buffer) {
		final M map = mapFactory.get();
		final int num = buffer.readVarInt();
		for(int i = 0; i < num; i++)
			map.put(keyDeserializer.apply(buffer), valueDeserializer.apply(buffer));
		return map;
	}

	public static <K, V> CompoundNBT writeMap(final Map<? extends K, ? extends V> map, final Function<K, ? extends INBT> keySerializer,
			final Function<V, ? extends INBT> valueSerializer) {
		final CompoundNBT nbt = new CompoundNBT();
		final ListNBT keys = new ListNBT();
		final ListNBT values = new ListNBT();
		map.forEach((key, value) -> {
			keys.add(keySerializer.apply(key));
			values.add(valueSerializer.apply(value));
		});
		nbt.put("keys", keys);
		nbt.put("values", values);
		return nbt;
	}

	public static <K, V, M extends Map<K, V>> M readMap(final CompoundNBT nbt, final Supplier<? extends M> mapFactory,
			final Function<INBT, ? extends K> keyDeserializer, final int keyType,
			final Function<INBT, ? extends V> valueDeserializer, final int valueType) {
		final ListNBT keys = nbt.getList("keys", keyType);
		final ListNBT values = nbt.getList("values", valueType);
		final M map = mapFactory.get();
		for(int i = 0; i < keys.size(); i++)
			map.put(keyDeserializer.apply(keys.get(i)), valueDeserializer.apply(values.get(i)));
		return map;
	}

	public static <T> ListNBT writeSet(final Set<? extends T> set, final Function<T, ? extends INBT> elementSerializer) {
		final ListNBT list = new ListNBT();
		set.forEach(element -> list.add(elementSerializer.apply(element)));
		return list;
	}

	public static <T, S extends Set<T>> S readSet(final ListNBT nbt, final Supplier<? extends S> setFactory,
			final Function<INBT, ? extends T> elementDeserializer) {
		final S set = setFactory.get();
		nbt.forEach(inbt -> set.add(elementDeserializer.apply(inbt)));
		return set;
	}

	public static <T> void writeSet(final Set<? extends T> set, final BiConsumer<PacketBuffer, T> elementSerializer, final PacketBuffer buffer) {
		buffer.writeVarInt(set.size());
		set.forEach(element -> elementSerializer.accept(buffer, element));
	}

	public static <T, S extends Set<T>> S readSet(final PacketBuffer buffer, final Supplier<? extends S> setFactory,
			final Function<PacketBuffer, ? extends T> elementDeserializer) {
		final S set = setFactory.get();
		final int numElem = buffer.readVarInt();
		for(int i = 0; i < numElem; i++)
			set.add(elementDeserializer.apply(buffer));
		return set;
	}

}
