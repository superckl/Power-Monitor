package me.superckl.upm.network.member;

import me.superckl.upm.UPMTile;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockChangeListener {

	@SubscribeEvent
	public void onBlockChange(final BlockEvent.NeighborNotifyEvent e) {
		if(e.getWorld().isClientSide())
			return;
		UPMTile.LOADED_TILES.stream().filter(tile -> tile.getNetwork() != null).map(UPMTile::getNetwork).forEach(network -> {
			if(network.getMembers().stream().anyMatch(wrapped -> wrapped.getPositions().contains(e.getPos()))) {
				network.scan();
				network.getOwner().resetScanDelay();
			}
		});
	}

}
