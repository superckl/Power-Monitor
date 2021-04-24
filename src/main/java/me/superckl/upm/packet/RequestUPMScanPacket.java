package me.superckl.upm.packet;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.UPMTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

@RequiredArgsConstructor
public class RequestUPMScanPacket {

	private final BlockPos tilePosition;

	public void encode(final PacketBuffer buffer) {
		buffer.writeBlockPos(this.tilePosition);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER)
			supplier.get().enqueueWork(() -> {
				final PlayerEntity player = supplier.get().getSender();
				if(player != null) {
					final TileEntity te = player.level.getBlockEntity(this.tilePosition);
					if(te instanceof UPMTile)
						((UPMTile)te).scanNetwork();
				}
			});
		supplier.get().setPacketHandled(true);
	}

	public static RequestUPMScanPacket decode(final PacketBuffer buffer) {
		return new RequestUPMScanPacket(buffer.readBlockPos());
	}

}
