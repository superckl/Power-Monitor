package me.superckl.upm.network.member.wrapper;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.NetworkMember;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants;

@RequiredArgsConstructor
public abstract class WrappedNetworkMember {

	public static final String OVERRIDE_KEY = "override";

	@Getter
	protected final NetworkMember member;
	@Getter
	protected final List<TileEntityType<?>> tileTypes;
	@Setter
	protected MemberType type;

	public MemberType getType() {
		if(this.type == null)
			return this.member.getType();
		return this.type;
	}

	public boolean hasTypeOverride() {
		return this.type != null;
	}

	public abstract Collection<Block> toBlocks();

	public CompoundNBT serialize(final boolean toClient) {
		final CompoundNBT nbt = new CompoundNBT();
		if(this.hasTypeOverride())
			nbt.putString(WrappedNetworkMember.OVERRIDE_KEY, this.getType().name());
		return nbt;
	}

	public void deserializeTypeOverride(final CompoundNBT nbt){
		if(nbt.contains(WrappedNetworkMember.OVERRIDE_KEY, Constants.NBT.TAG_STRING))
			this.setType(MemberType.valueOf(nbt.getString(WrappedNetworkMember.OVERRIDE_KEY)));
	}

}
