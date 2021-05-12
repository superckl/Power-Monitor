package me.superckl.upm.api;

import java.util.function.Supplier;

import net.minecraftforge.registries.IForgeRegistry;

public class UPMAPI {

	public static Supplier<IForgeRegistry<NetworkMemberResolver<?>>> RESOLVER_REGISTRY;
	public static final String MOD_ID = "upm";

}
