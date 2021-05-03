package me.superckl.upm.network;

import java.util.EnumSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.util.SerializationUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UPMRedstoneConfiguration implements INBTSerializable<CompoundNBT>, Cloneable{

	public static final String MIN_KEY = "min";
	public static final String MAX_KEY = "max";
	public static final String TYPES_KEY = "types";

	@Setter
	@Getter
	private int min = 5, max = 95;
	@Getter
	private Set<MemberType> types = EnumSet.noneOf(MemberType.class);

	@Override
	public UPMRedstoneConfiguration clone() {
		return new UPMRedstoneConfiguration(this.min, this.max, EnumSet.copyOf(this.types));
	}

	public boolean shouldOutput(final EnergyNetwork network, final boolean currentlyOutputting) {
		if(this.types.isEmpty())
			return false;
		long stored = 0;
		long storage = 0;
		for(final MemberType type:this.types) {
			stored += network.getStored(type);
			storage += network.getStorage(type);
		}
		if(storage == 0)
			return false;
		final int percentage = (int) Math.round(100*(double)stored/storage);
		if(percentage <= this.min)
			return true;
		if(percentage >= this.max)
			return false;
		return currentlyOutputting;
	}

	public void copyFrom(final UPMRedstoneConfiguration config) {
		this.max = config.max;
		this.min = config.min;
		this.types = EnumSet.copyOf(config.types);

	}

	public void writeToNetwork(final PacketBuffer buffer) {
		buffer.writeVarInt(this.min);
		buffer.writeVarInt(this.max);
		SerializationUtil.writeSet(this.types, PacketBuffer::writeEnum, buffer);
	}

	public static UPMRedstoneConfiguration readFromNetwork(final PacketBuffer buffer) {
		final int min = buffer.readVarInt();
		final int max = buffer.readVarInt();
		final Set<MemberType> types = SerializationUtil.readSet(buffer, () -> EnumSet.noneOf(MemberType.class), pBuffer -> pBuffer.readEnum(MemberType.class));
		return new UPMRedstoneConfiguration(min, max, types);
	}

	@Override
	public CompoundNBT serializeNBT() {
		final CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(UPMRedstoneConfiguration.MIN_KEY, this.min);
		nbt.putInt(UPMRedstoneConfiguration.MAX_KEY, this.max);
		nbt.put(UPMRedstoneConfiguration.TYPES_KEY, SerializationUtil.writeSet(this.types, type -> StringNBT.valueOf(type.name())));
		return nbt;
	}

	@Override
	public void deserializeNBT(final CompoundNBT nbt) {
		this.min = nbt.getInt(UPMRedstoneConfiguration.MIN_KEY);
		this.max = nbt.getInt(UPMRedstoneConfiguration.MAX_KEY);
		this.types.clear();
		this.types.addAll(SerializationUtil.readSet(nbt.getList(UPMRedstoneConfiguration.TYPES_KEY, Constants.NBT.TAG_STRING), () -> EnumSet.noneOf(MemberType.class),
				inbt -> MemberType.valueOf(inbt.getAsString())));
	}

}
