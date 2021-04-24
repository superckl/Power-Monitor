package me.superckl.upm;

import org.apache.logging.log4j.LogManager;

import me.superckl.upm.packet.OpenUPMScreenPacket;
import me.superckl.upm.packet.RequestUPMScanPacket;
import me.superckl.upm.packet.UPMPacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
		int pIndex = 0;
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, OpenUPMScreenPacket.class, OpenUPMScreenPacket::encode,
				OpenUPMScreenPacket::decode, OpenUPMScreenPacket::handle);
		UPMPacketHandler.INSTANCE.registerMessage(pIndex++, RequestUPMScanPacket.class, RequestUPMScanPacket::encode,
				RequestUPMScanPacket::decode, RequestUPMScanPacket::handle);
	}

}
