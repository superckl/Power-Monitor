package me.superckl.upm.packet;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.ClientHelper;
import me.superckl.upm.UPMTile;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.util.PacketUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

@RequiredArgsConstructor
public class UpdateEnergyPacket {

	private final BlockPos tilePosition;
	private final Map<MemberType, Long> storage;
	private final Map<MemberType, Long> stored;

	public void encode(final PacketBuffer buffer) {
		buffer.writeBlockPos(this.tilePosition);
		PacketUtil.writeMap(this.storage, PacketBuffer::writeEnum, PacketBuffer::writeVarLong, buffer);
		PacketUtil.writeMap(this.stored, PacketBuffer::writeEnum, PacketBuffer::writeVarLong, buffer);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
			supplier.get().enqueueWork(() -> {
				final TileEntity te = ClientHelper.getLevel().getBlockEntity(this.tilePosition);
				if(te instanceof UPMTile) {
					final UPMTile upm = (UPMTile) te;
					if(upm.getNetwork() != null)
						upm.getNetwork().setEnergy(this.storage, this.stored);
				}

			});
		supplier.get().setPacketHandled(true);
	}

	public static UpdateEnergyPacket decode(final PacketBuffer buffer) {
		final BlockPos pos = buffer.readBlockPos();
		final Map<MemberType, Long> storage = PacketUtil.readMap(() -> new EnumMap<>(MemberType.class), pBuffer -> pBuffer.readEnum(MemberType.class), PacketBuffer::readVarLong, buffer);
		final Map<MemberType, Long> stored = PacketUtil.readMap(() -> new EnumMap<>(MemberType.class), pBuffer -> pBuffer.readEnum(MemberType.class), PacketBuffer::readVarLong, buffer);
		return new UpdateEnergyPacket(pos, storage, stored);
	}

}
