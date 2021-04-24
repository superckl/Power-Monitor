package me.superckl.upm.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Setter;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;

public abstract class UPMScreenMode {

	@Setter
	protected UPMScreen screen;

	public void init() {}
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {}
	public void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {}
	public void initSlots(final UPMClientSideContainer clientContainer) {}
	public void slotClicked(final Slot slot, final int mouseX, final int mouseY, final ClickType type) {}

	public abstract int getWidth();
	public abstract int getHeight();

	public abstract UPMScreenModeType getType();

	public boolean shouldReopen(final UPMScreenModeType type) {
		return type != this.getType();
	}

}
