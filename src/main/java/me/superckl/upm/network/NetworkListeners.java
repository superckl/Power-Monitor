package me.superckl.upm.network;

import me.superckl.upm.UPMTile;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class NetworkListeners {

	@SubscribeEvent
	public void onBlockChange(final BlockEvent.NeighborNotifyEvent e) {
		if(e.getWorld().isClientSide())
			return;
		//TODO listen for updates next to the network and that the added block has a valid capability
		UPMTile.LOADED_TILES.stream().filter(tile -> tile.getNetwork() != null).forEach(tile -> {
			final EnergyNetwork network = tile.getNetwork();
			if(network.getMembers().stream().anyMatch(wrapped -> wrapped.getPositions().containsKey(e.getPos()))) {
				network.scan();
				network.getOwner().resetScanDelay();
				tile.syncToClientLight(null);
			}
		});
	}

	@SubscribeEvent
	public void onWorldTick(final WorldTickEvent e) {
		if(e.side == LogicalSide.SERVER && e.phase == Phase.END)
			UPMTile.LOADED_TILES.forEach(tile -> {
				if(tile.getLevel() == e.world && tile.getNetwork() != null)
					tile.getNetwork().tick();
			});
	}

}
