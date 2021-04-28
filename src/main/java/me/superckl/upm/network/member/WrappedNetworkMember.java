package me.superckl.upm.network.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.superckl.upm.util.NetworkUtil;
import me.superckl.upm.util.SerializationUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

@RequiredArgsConstructor
@Getter
public class WrappedNetworkMember{

	public static final String POSITIONS_KEY = "positions";
	public static final String OVERRIDE_KEY = "override";
	public static final String SIDES_KEY = "sides";

	private final NetworkMember member;
	private final Map<BlockPos, Direction> positions;
	private final List<TileEntityType<?>> tileTypes;
	@Setter
	private MemberType type;

	@Override
	public boolean equals(final Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof WrappedNetworkMember))
			return false;
		final WrappedNetworkMember other = (WrappedNetworkMember) obj;
		return this.member.isSameStorage(other.member) && this.positions.equals(other.positions) && this.type == other.type;
	}

	public void addPosition(final BlockPos pos, final TileEntityType<?> type, final Direction side) {
		if(!this.positions.containsKey(pos)) {
			this.positions.put(pos, side);
			if(!this.tileTypes.contains(type))
				this.tileTypes.add(type);
		}
	}

	public MemberType getType() {
		if(this.type == null)
			return this.member.getType();
		return this.type;
	}

	public boolean hasTypeOverride() {
		return this.type != null;
	}

	public void merge(final WrappedNetworkMember other) {
		this.positions.putAll(other.positions);
		other.tileTypes.forEach(type -> {
			if(!this.tileTypes.contains(type))
				this.tileTypes.add(type);
		});
	}

	public CompoundNBT serialize() {
		final CompoundNBT nbt = new CompoundNBT();
		nbt.put(WrappedNetworkMember.POSITIONS_KEY, SerializationUtil.writeMap(this.positions, NBTUtil::writeBlockPos, dir -> StringNBT.valueOf(dir.getName())));
		if(this.hasTypeOverride())
			nbt.putString(WrappedNetworkMember.OVERRIDE_KEY, this.getType().name());
		return nbt;
	}

	public static WrappedNetworkMember deserialize(final CompoundNBT nbt, final World level) {
		final Map<BlockPos, Direction> positions = SerializationUtil.readMap(nbt.getCompound(WrappedNetworkMember.POSITIONS_KEY),
				HashMap::new, inbt -> NBTUtil.readBlockPos((CompoundNBT) inbt), Constants.NBT.TAG_COMPOUND,
				inbt -> Direction.byName(inbt.getAsString()), Constants.NBT.TAG_STRING);
		final Pair<NetworkMember, List<TileEntityType<?>>> pair =  NetworkUtil.getMembers(positions, level);
		final WrappedNetworkMember wrapped = new WrappedNetworkMember(pair.getLeft(), positions, pair.getRight());
		if(nbt.contains(WrappedNetworkMember.OVERRIDE_KEY))
			wrapped.setType(MemberType.valueOf(nbt.getString(WrappedNetworkMember.OVERRIDE_KEY)));
		return wrapped;
	}

}
