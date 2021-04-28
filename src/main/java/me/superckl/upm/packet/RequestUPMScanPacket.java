package me.superckl.upm.packet;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.UPMTile;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.util.SerializationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

@RequiredArgsConstructor
public class RequestUPMScanPacket {

	private final BlockPos tilePosition;
	private final Map<TileEntityType<?>, MemberType> typeOverrides;

	public void encode(final PacketBuffer buffer) {
		buffer.writeBlockPos(this.tilePosition);
		SerializationUtil.writeMap(this.typeOverrides, PacketBuffer::writeRegistryId, PacketBuffer::writeEnum, buffer);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER)
			supplier.get().enqueueWork(() -> {
				final PlayerEntity player = supplier.get().getSender();
				if(player != null) {
					final TileEntity te = player.level.getBlockEntity(this.tilePosition);
					if(te instanceof UPMTile) {
						((UPMTile) te).setTypeOverrides(this.typeOverrides);
						((UPMTile)te).requestScan();
					}
				}
			});
		supplier.get().setPacketHandled(true);
	}

	public static RequestUPMScanPacket decode(final PacketBuffer buffer) {
		final BlockPos pos = buffer.readBlockPos();
		final Map<TileEntityType<?>, MemberType> typeOverrides = SerializationUtil.readMap(IdentityHashMap::new, pBuffer -> pBuffer.readRegistryIdSafe(TileEntityType.class), pBuffer -> pBuffer.readEnum(MemberType.class), buffer);
		return new RequestUPMScanPacket(pos, typeOverrides);
	}

}
