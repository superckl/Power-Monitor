package me.superckl.upm.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.superckl.upm.ClientHelper;
import me.superckl.upm.UPM;
import me.superckl.upm.UPMTile;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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

	@Override
	public <T extends IGuiEventListener> T addWidget(final T widget) {
		return super.addWidget(widget);
	}

	public Slot getHoveredSlot() {
		return this.hoveredSlot;
	}

	public void changeMode(final UPMScreenModeType type) {
		final UPMScreenMode mode = type.buildMode();
		ClientHelper.getMinecraft().setScreen(new UPMScreen(new UPMClientSideContainer(this.menu.getOwner()),
				new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "name"))), mode));
	}

	public void onNetworkChanged(final UPMTile tile) {
		if(this.menu.getOwner() == tile && this.mode.networkChanged(tile.getScreenType()))
			this.minecraft.setScreen(UPMScreen.from(tile));
	}

	public void onScanStateChanged(final boolean state) {
		this.mode.upmScanStateChanged(state);
	}

	@Override
	public void render(final MatrixStack stack, final int mouseX, final int mouseY, final float partial) {
		super.render(stack, mouseX, mouseY, partial);
		this.mode.renderWidgets(stack, mouseX, mouseY, partial);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	public FontRenderer getFont() {
		return this.font;
	}

	@Override
	protected void renderTooltip(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.mode.renderTooltip(stack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(final MatrixStack stack, final float partial, final int mouseX, final int mouseY) {
		this.mode.renderBackground(stack, mouseX, mouseY);
		for(final Slot slot:this.menu.slots) {
			if(slot.getItem().isEmpty())
				continue;
			final int color = this.mode.getSlotBackgroundColor(slot);
			if(color != -1) {
				final int x = this.getGuiLeft()+slot.x;
				final int y = this.getGuiTop()+slot.y;
				this.fillGradient(stack, x, y, x+16, y+16, color, color);
			}
		}
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

	@Override
	public boolean mouseScrolled(final double mouseX, final double mouseY, final double scroll) {
		return this.mode.mouseScrolled(mouseX, mouseY, scroll) || super.mouseScrolled(mouseX, mouseY, scroll);
	}

	@Override
	public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
		return this.mode.mouseClicked(mouseX, mouseY, mouseButton) || super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseDragged(final double newX, final double newY, final int button, final double deltaX, final double deltaY) {
		return this.mode.mouseDragged(newX, newY, button, deltaX, deltaY) || this.nestedGUIMouseDragged(newX, newY, button, deltaX, deltaY) || super.mouseDragged(newX, newY, button, deltaX, deltaY);
	}

	/**
	 * Container screen is dumb and doesn't call super.mouseDragged, so we have to manually notify any widgets
	 */
	private boolean nestedGUIMouseDragged(final double newX, final double newY, final int button, final double deltaX, final double deltaY) {
		return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(newX, newY, button, deltaX, deltaY) : false;
	}

	@Override
	public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
		return this.mode.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
	}

	public void onUPMRemoved(final UPMTile tile) {
		if(this.menu.getOwner() == tile)
			ClientHelper.getMinecraft().setScreen(null);
	}

	public static UPMScreen from(final UPMTile tile) {
		final UPMScreenMode mode = tile.getScreenType().buildMode();
		final UPMClientSideContainer container = new UPMClientSideContainer(tile);
		return new UPMScreen(container, new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "name"))), mode);
	}

	public Button newScanButton(final int x, final int y, final int width, final int height, final boolean rescan, final IPressable onPress) {
		return new Button(x, y, width, height, new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, rescan ? "rescan":"scan"))),
				onPress, (button, stack, mouseX, mouseY) -> {
					if(!this.menu.getOwner().canScan())
						this.renderTooltip(stack, new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "scan_delay"))).withStyle(TextFormatting.RED), mouseX, mouseY);
				});
	}

}
