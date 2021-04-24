package me.superckl.upm.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.superckl.upm.UPM;
import me.superckl.upm.packet.RequestUPMScanPacket;
import me.superckl.upm.packet.UPMPacketHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class NoNetworkMode extends UPMScreenMode{

	public static final ResourceLocation BACKGROUND = new ResourceLocation(UPM.MOD_ID, "textures/gui/no_network.png");
	public static final int WIDTH = 144;
	public static final int HEIGHT = 79;
	public static final ITextComponent NO_NETWORK_TEXT = new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "no_network")));

	@Override
	public void init() {
		final int width = 100;
		final int height = 20;
		this.screen.addButton(new Button(this.screen.getGuiLeft()+(this.getWidth()-width)/2, 15+this.screen.getGuiTop()+(this.getHeight()-height)/2, width, height,
				new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "scan"))), this::onScanButtonPress));
	}

	@Override
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.screen.getMinecraft().getTextureManager().bind(NoNetworkMode.BACKGROUND);
		this.screen.blit(stack, this.screen.getGuiLeft(), this.screen.getGuiTop(), 0, 0, this.getWidth(), this.getHeight());
	}

	@Override
	public void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {
		final FontRenderer font = this.screen.getFont();
		font.draw(stack, NoNetworkMode.NO_NETWORK_TEXT, this.getWidth()/2-font.width(NoNetworkMode.NO_NETWORK_TEXT)/2, this.getHeight()/2-font.lineHeight, 4210752);
	}

	public void onScanButtonPress(final Button button) {
		UPMPacketHandler.INSTANCE.sendToServer(new RequestUPMScanPacket(this.screen.getMenu().getUPMPosition()));
	}

	@Override
	public int getWidth() {
		return NoNetworkMode.WIDTH;
	}

	@Override
	public int getHeight() {
		return NoNetworkMode.HEIGHT;
	}

	@Override
	public UPMScreenModeType getType() {
		return UPMScreenModeType.NO_NETWORK;
	}

}
