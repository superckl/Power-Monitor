package me.superckl.upm.network.member;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.superckl.upm.util.NetworkUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@RequiredArgsConstructor
@Getter
public class WrappedNetworkMember{

	public static final String POSITIONS_KEY = "positions";
	public static final String OVERRIDE_KEY = "override";

	private final NetworkMember member;
	private final Set<BlockPos> positions;
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

	public void addPosition(final BlockPos pos, final TileEntityType<?> type) {
		this.positions.add(pos);
		if(!this.tileTypes.contains(type))
			this.tileTypes.add(type);
	}

	public MemberType getType() {
		if(this.type == null)
			return this.member.getType();
		return this.type;
	}

	public boolean hasTypeOverride() {
		return this.type != null;
	}

	public CompoundNBT serialize() {
		final CompoundNBT nbt = new CompoundNBT();
		nbt.putLongArray(WrappedNetworkMember.POSITIONS_KEY, this.positions.stream().mapToLong(BlockPos::asLong).toArray());
		if(this.hasTypeOverride())
			nbt.putString(WrappedNetworkMember.OVERRIDE_KEY, this.getType().name());
		return nbt;
	}

	public static WrappedNetworkMember deserialize(final CompoundNBT nbt, final World level) {
		final Set<BlockPos> positions = LongStream.of(nbt.getLongArray(WrappedNetworkMember.POSITIONS_KEY)).mapToObj(BlockPos::of).collect(Collectors.toCollection(HashSet::new));
		final Pair<NetworkMember, List<TileEntityType<?>>> pair =  NetworkUtil.getAndVerify(positions, level);
		final WrappedNetworkMember wrapped = new WrappedNetworkMember(pair.getLeft(), positions, pair.getRight());
		if(nbt.contains(WrappedNetworkMember.OVERRIDE_KEY))
			wrapped.setType(MemberType.valueOf(nbt.getString(WrappedNetworkMember.OVERRIDE_KEY)));
		return wrapped;
	}

}
