package me.superckl.upm.screen;

import lombok.Getter;
import me.superckl.upm.UPMTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;

public class UPMClientSideContainer extends Container{

	@Getter
	private final UPMTile owner;

	public UPMClientSideContainer(final UPMTile owner) {
		super(null, 0);
		this.owner = owner;
	}

	@Override
	public Slot addSlot(final Slot slot) {
		return super.addSlot(slot);
	}

	@Override
	public boolean stillValid(final PlayerEntity p_75145_1_) {
		return true;
	}

}
