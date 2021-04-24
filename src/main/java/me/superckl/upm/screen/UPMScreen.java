package me.superckl.upm.screen;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.superckl.upm.UPM;
import me.superckl.upm.UPMTile;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

public class UPMScreen extends ContainerScreen<UPMClientSideContainer>{

	private final UPMScreenMode mode;

	public UPMScreen(final UPMClientSideContainer container, final ITextComponent title, final UPMScreenMode mode) {
		super(container, null, title);
		this.mode = mode;
		this.mode.setScreen(this);
		this.mode.initSlots(container);
	}

	@Override
	protected void init() {
		this.buttons.clear();
		this.children.clear();
		this.imageWidth = this.mode.getWidth();
		this.imageHeight = this.mode.getHeight();
		super.init();
		this.mode.init();
	}

	@Override
	public <T extends Widget> T addButton(final T button) {
		return super.addButton(button);
	}

	public void onNetworkChanged(final UPMTile tile) {
		if(this.menu.getUPMPosition().equals(tile.getBlockPos()) && this.mode.shouldReopen(tile.getScreenType()))
			this.minecraft.setScreen(UPMScreen.from(tile));
	}

	@Override
	public void render(final MatrixStack stack, final int mouseX, final int mouseY, final float partial) {
		super.render(stack, mouseX, mouseY, partial);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	public FontRenderer getFont() {
		return this.font;
	}

	@Override
	public void renderWrappedToolTip(final MatrixStack matrixStack, List<? extends ITextProperties> tooltips, final int mouseX,
			final int mouseY, final FontRenderer font) {
		tooltips = this.mode.modifyTooltip(this.hoveredSlot, tooltips);
		super.renderWrappedToolTip(matrixStack, tooltips, mouseX, mouseY, font);
	}

	@Override
	protected void renderBg(final MatrixStack stack, final float partial, final int mouseX, final int mouseY) {
		this.mode.renderBackground(stack, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.font.draw(stack, this.title, this.imageWidth/2-this.font.width(this.title)/2, this.titleLabelY, 4210752);
		this.mode.renderLabels(stack, mouseX, mouseY);
	}

	@Override
	protected void slotClicked(final Slot slot, final int mouseX, final int mouseY, final ClickType type) {
		this.mode.slotClicked(slot, mouseX, mouseY, type);
	}

	public static UPMScreen from(final UPMTile tile) {
		final UPMScreenMode mode = tile.getScreenType().buildMode();
		final UPMClientSideContainer container = new UPMClientSideContainer(tile.getNetwork(), tile.getBlockPos());
		return new UPMScreen(container, new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "name"))), mode);
	}

}
