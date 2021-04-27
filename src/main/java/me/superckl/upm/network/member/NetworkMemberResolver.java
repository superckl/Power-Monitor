package me.superckl.upm.network.member;

import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NetworkMemberResolver<T extends NetworkMember> extends ForgeRegistryEntry<NetworkMemberResolver<?>>{

	public abstract Optional<T> getNetworkMember(TileEntity tile, Direction side);

	public MemberType typeFromTag(final TileEntityType<?> teType) {
		if(teType.getTags().contains(MemberType.STORAGE.tag()))
			return MemberType.STORAGE;
		if(teType.getTags().contains(MemberType.CABLE.tag()))
			return MemberType.CABLE;
		if(teType.getTags().contains(MemberType.MACHINE.tag()))
			return MemberType.MACHINE;
		return MemberType.UNKNOWN;
	}

}
