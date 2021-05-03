package me.superckl.upm;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;

import me.superckl.upm.network.NetworkListeners;
import me.superckl.upm.packet.OpenUPMScreenPacket;
import me.superckl.upm.packet.RequestUPMScanPacket;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.packet.UPMRedstoneConfigPacket;
import me.superckl.upm.packet.UPMScanStatePacket;
import me.superckl.upm.packet.UpdateEnergyPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;

@Mod(UPM.MOD_ID)
public class UPM {

	public static final String MOD_ID = "upm";

	public UPM() {
		LogHelper.setLogger(LogManager.getFormatterLogger(UPM.MOD_ID));

		final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ModRegisters.BLOCK_REGISTER.register(bus);
		ModRegisters.ITEM_REGISTER.register(bus);
		ModRegisters.TILE_REGISTER.register(bus);
		ModRegisters.RESOLVER_REGISTER.register(bus);

		bus.addListener(this::commonSetup);
	}

	private void commonSetup(final FMLCommonSetupEvent e) {
		e.enqueueWork(() -> MinecraftForge.EVENT_BUS.register(new NetworkListeners()));
		int pIndex = 0;
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, OpenUPMScreenPacket.class, OpenUPMScreenPacket::encode,
				OpenUPMScreenPacket::decode, OpenUPMScreenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, RequestUPMScanPacket.class, RequestUPMScanPacket::encode,
				RequestUPMScanPacket::decode, RequestUPMScanPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, UpdateEnergyPacket.class, UpdateEnergyPacket::encode,
				UpdateEnergyPacket::decode, UpdateEnergyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, UPMScanStatePacket.class, UPMScanStatePacket::encode,
				UPMScanStatePacket::decode, UPMScanStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, UPMRedstoneConfigPacket.class, UPMRedstoneConfigPacket::encode,
				UPMRedstoneConfigPacket::decode, UPMRedstoneConfigPacket::handle);
	}

}
