package me.superckl.upm.packet;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.ClientHelper;
import me.superckl.upm.UPMTile;
import me.superckl.upm.screen.UPMScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

@RequiredArgsConstructor
public class UPMScanStatePacket {

	private final BlockPos tilePosition;
	private final boolean state;

	public void encode(final PacketBuffer buffer) {
		buffer.writeBlockPos(this.tilePosition);
		buffer.writeBoolean(this.state);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
			supplier.get().enqueueWork(() -> {
				final TileEntity te = ClientHelper.getLevel().getBlockEntity(this.tilePosition);
				if(te instanceof UPMTile) {
					((UPMTile)te).clientScanState(this.state);
					final Screen currentScreen = ClientHelper.getScreen();
					if(currentScreen instanceof UPMScreen)
						((UPMScreen)currentScreen).onScanStateChanged(this.state);
				}

			});
		supplier.get().setPacketHandled(true);
	}

	public static UPMScanStatePacket decode(final PacketBuffer buffer) {
		return new UPMScanStatePacket(buffer.readBlockPos(), buffer.readBoolean());
	}

}
