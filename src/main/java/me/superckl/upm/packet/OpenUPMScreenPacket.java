package me.superckl.upm.packet;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.ClientHelper;
import me.superckl.upm.UPMTile;
import me.superckl.upm.screen.UPMScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

@RequiredArgsConstructor
public class OpenUPMScreenPacket {

	private final BlockPos tilePosition;

	public void encode(final PacketBuffer buffer) {
		buffer.writeBlockPos(this.tilePosition);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
			supplier.get().enqueueWork(() -> {
				final TileEntity te = ClientHelper.getLevel().getBlockEntity(this.tilePosition);
				if(te instanceof UPMTile)
					ClientHelper.getMinecraft().setScreen(UPMScreen.from((UPMTile) te));

			});
		supplier.get().setPacketHandled(true);
	}

	public static OpenUPMScreenPacket decode(final PacketBuffer buffer) {
		return new OpenUPMScreenPacket(buffer.readBlockPos());
	}

}
