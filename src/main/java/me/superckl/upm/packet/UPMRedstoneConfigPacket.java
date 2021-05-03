package me.superckl.upm.packet;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.ClientHelper;
import me.superckl.upm.UPMTile;
import me.superckl.upm.network.UPMRedstoneConfiguration;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

@RequiredArgsConstructor
public class UPMRedstoneConfigPacket {

	private final BlockPos tilePosition;
	private final UPMRedstoneConfiguration config;

	public void encode(final PacketBuffer buffer) {
		buffer.writeBlockPos(this.tilePosition);
		this.config.writeToNetwork(buffer);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> {
			final TileEntity te = this.getLevel(supplier.get()).getBlockEntity(this.tilePosition);
			if(te instanceof UPMTile)
				((UPMTile) te).setRedstoneConfig(this.config);
		});
		supplier.get().setPacketHandled(true);
	}

	private World getLevel(final NetworkEvent.Context context) {

		if(context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
			return ClientHelper.getLevel();
		return context.getSender().level;
	}

	public static UPMRedstoneConfigPacket decode(final PacketBuffer buffer) {
		return new UPMRedstoneConfigPacket(buffer.readBlockPos(), UPMRedstoneConfiguration.readFromNetwork(buffer));
	}

}
