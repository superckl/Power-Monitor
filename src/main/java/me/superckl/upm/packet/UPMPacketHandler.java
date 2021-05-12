package me.superckl.upm.packet;

import me.superckl.upm.api.UPMAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class UPMPacketHandler {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(UPMAPI.MOD_ID, "main"),
			() -> UPMPacketHandler.PROTOCOL_VERSION, UPMPacketHandler.PROTOCOL_VERSION::equals, UPMPacketHandler.PROTOCOL_VERSION::equals);

}
