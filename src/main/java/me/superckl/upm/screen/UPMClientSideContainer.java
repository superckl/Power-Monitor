package me.superckl.upm.screen;

import lombok.Getter;
import me.superckl.upm.network.EnergyNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.BlockPos;

public class UPMClientSideContainer extends Container{

	@Getter
	private final EnergyNetwork network;
	@Getter
	private final BlockPos UPMPosition;

	public UPMClientSideContainer(final EnergyNetwork network, final BlockPos position) {
		super(null, 0);
		this.network = network;
		this.UPMPosition = position;
	}

	@Override
	public Slot addSlot(final Slot slot) {
		return super.addSlot(slot);
	}

	@Override
	public boolean stillValid(final PlayerEntity p_75145_1_) {
		// TODO Auto-generated method stub
		return false;
	}

}
