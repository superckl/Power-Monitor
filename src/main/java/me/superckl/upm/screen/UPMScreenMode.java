package me.superckl.upm.screen;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Setter;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;

public abstract class UPMScreenMode {

	@Setter
	protected UPMScreen screen;

	public void init() {}
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {}
	public void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {}
	public void initSlots(final UPMClientSideContainer clientContainer) {}
	public void slotClicked(final Slot slot, final int mouseX, final int mouseY, final ClickType type) {}
	public int getSlotBackgroundColor(final Slot slot) {return -1;}
	public void upmScanStateChanged(final boolean state) {}

	public void renderTooltip(final MatrixStack stack, final int mouseX, final int mouseY) {
		if(this.screen.getHoveredSlot() != null)
			this.screen.renderComponentTooltip(stack, this.tooltipForSlot(this.screen.getHoveredSlot()), mouseX, mouseY);
	}

	public List<ITextComponent> tooltipForSlot(final Slot slot){
		if(slot.getItem().isEmpty())
			return Collections.emptyList();
		return Lists.newArrayList(slot.getItem().getHoverName());
	}

	public abstract int getWidth();
	public abstract int getHeight();

	public abstract UPMScreenModeType getType();

	public boolean shouldReopen(final UPMScreenModeType type) {
		return type != this.getType();
	}

}
