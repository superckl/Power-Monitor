package me.superckl.upm.network.member.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Getter;
import me.superckl.upm.api.NetworkMember;
import me.superckl.upm.util.NetworkUtil;
import me.superckl.upm.util.PositionUtil;
import me.superckl.upm.util.SerializationUtil;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.common.util.Constants;

public class PositionBasedWrapper extends WrappedNetworkMember{

	public static final String POSITIONS_KEY = "positions";

	public static final String SIDES_KEY = "sides";

	@Getter
	private final Map<GlobalPos, Optional<Direction>> positions;

	public PositionBasedWrapper(final NetworkMember member, final Map<GlobalPos, Optional<Direction>> positions, final List<TileEntityType<?>> tileTypes) {
		super(member, tileTypes);
		this.positions = positions;
	}

	@Override
	public boolean equals(final Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof PositionBasedWrapper))
			return false;
		final PositionBasedWrapper other = (PositionBasedWrapper) obj;
		return this.member.isSameStorage(other.member) && this.positions.equals(other.positions) && this.type == other.type;
	}

	public void addPosition(final GlobalPos pos, final TileEntityType<?> type, final Optional<Direction> side) {
		if(!this.positions.containsKey(pos)) {
			this.positions.put(pos, side);
			if(!this.tileTypes.contains(type))
				this.tileTypes.add(type);
		}
	}

	public void merge(final PositionBasedWrapper other) {
		this.positions.putAll(other.positions);
		other.tileTypes.forEach(type -> {
			if(!this.tileTypes.contains(type))
				this.tileTypes.add(type);
		});
	}

	@Override
	public CompoundNBT serialize(final boolean toClient) {
		if(toClient)
			return new PhantomWrapper(this.getType(), this.tileTypes, this.toBlocks()).serialize(toClient);
		final CompoundNBT nbt = super.serialize(toClient);
		nbt.put(PositionBasedWrapper.POSITIONS_KEY, SerializationUtil.writeMap(this.positions,
				pos -> GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, pos).getOrThrow(false, val -> {}), SerializationUtil.OPT_DIRECTION_SERIALIZER));
		return nbt;
	}

	public static PositionBasedWrapper deserialize(final CompoundNBT nbt) throws IllegalStateException{
		final Map<GlobalPos, Optional<Direction>> positions = SerializationUtil.readMap(nbt.getCompound(PositionBasedWrapper.POSITIONS_KEY),
				HashMap::new, inbt -> GlobalPos.CODEC.parse(NBTDynamicOps.INSTANCE, inbt).getOrThrow(false, val -> {}), Constants.NBT.TAG_COMPOUND,
				SerializationUtil.OPT_DIRECTION_DESERIALIZER, Constants.NBT.TAG_STRING);
		final Pair<NetworkMember, List<TileEntityType<?>>> pair =  NetworkUtil.getMembers(positions);
		final PositionBasedWrapper wrapped = new PositionBasedWrapper(pair.getLeft(), positions, pair.getRight());
		wrapped.deserializeTypeOverride(nbt);
		return wrapped;
	}

	@Override
	public List<Block> toBlocks() {
		return this.positions.keySet().stream().map(PositionUtil::getBlock).collect(Collectors.toList());
	}

}
