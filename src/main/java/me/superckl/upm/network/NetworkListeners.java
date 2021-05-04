package me.superckl.upm.network;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import me.superckl.upm.UPMTile;
import me.superckl.upm.network.member.NetworkMember;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class NetworkListeners {

	/**
	 * Event listener to update networks when blocks in or next to the network are updated
	 */
	@SubscribeEvent
	public void onBlockChange(final BlockEvent.NeighborNotifyEvent e) {
		if(e.getWorld().isClientSide())
			return;
		//Initialize the set with the changed block's positions to check if it
		//was in a network
		final Set<BlockPos> updated = Sets.newHashSet(e.getPos());
		//If the updated block has sided network members, check for networks on those sides as well
		final TileEntity teUpdated = e.getWorld().getBlockEntity(e.getPos());
		if(teUpdated != null)
			e.getNotifiedSides().forEach(dir -> NetworkMember.from(teUpdated, dir)
					.ifPresent(member -> updated.add(e.getPos().relative(dir))));
		UPMTile.LOADED_TILES.stream().filter(tile -> tile.getLevel() == e.getWorld() && tile.getNetwork() != null).forEach(tile -> {
			//For each loaded network, test if it contains any of the updated positions and rescan if so
			final EnergyNetwork network = tile.getNetwork();
			if(updated.contains(tile.getBlockPos()) || network.getMembers().stream()
					.anyMatch(wrapped -> !Collections.disjoint(wrapped.getPositions().keySet(), updated)))
				network.scheduleScan();
		});
	}

	/**
	 * Event listener to tick loaded networks once world ticking is done
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onWorldTick(final WorldTickEvent e) {
		if(e.side == LogicalSide.SERVER && e.phase == Phase.END)
			UPMTile.LOADED_TILES.forEach(tile -> {
				if(tile.getLevel() == e.world && tile.getNetwork() != null)
					tile.getNetwork().tick();
			});
	}

}
