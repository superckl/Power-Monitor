package me.superckl.upm.network;

import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NetworkMemberResolver<T extends NetworkMember> extends ForgeRegistryEntry<NetworkMemberResolver<T>>{

	public abstract Optional<T> getNetworkMember(TileEntity tile);

}
