package me.superckl.upm.util;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class SerializationUtil {

	public static final Function<Direction, StringNBT> DIRECTION_SERIALIZER = dir -> StringNBT.valueOf(dir.getSerializedName());
	public static final Function<INBT, Direction> DIRECTION_DESERIALIZER = inbt -> Direction.byName(inbt.getAsString());

	public static final Function<Optional<Direction>, StringNBT> OPT_DIRECTION_SERIALIZER;
	public static final Function<INBT, Optional<Direction>> OPT_DIRECTION_DESERIALIZER;

	static {
		final Pair<Function<Optional<Direction>, StringNBT>, Function<INBT, Optional<Direction>>>  dirPair =
				SerializationUtil.optionalWrap(SerializationUtil.DIRECTION_SERIALIZER, SerializationUtil.DIRECTION_DESERIALIZER, StringNBT.valueOf("null"));
		OPT_DIRECTION_SERIALIZER = dirPair.getKey();
		OPT_DIRECTION_DESERIALIZER = dirPair.getValue();
	}

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

	public static <T, I extends INBT> Pair<Function<Optional<T>, I>, Function<INBT, Optional<T>>> optionalWrap(final Function<T, ? extends I> serializer, final Function<INBT, ? extends T> deserializer, final I missingValue){
		final Function<Optional<T>, I> optSerializer = opt -> {
			if(opt.isPresent())
				return serializer.apply(opt.get());
			return missingValue;
		};
		final Function<INBT, Optional<T>> optDeserializer = inbt -> {
			if(inbt.equals(missingValue))
				return Optional.empty();
			return Optional.of(deserializer.apply(inbt));
		};
		return Pair.of(optSerializer, optDeserializer);
	}

}
