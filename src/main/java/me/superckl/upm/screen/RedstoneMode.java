package me.superckl.upm.screen;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import cofh.lib.util.helpers.MathHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.superckl.upm.ClientHelper;
import me.superckl.upm.ModRegisters;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.UPMAPI;
import me.superckl.upm.network.UPMRedstoneConfiguration;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.packet.UPMRedstoneConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

public class RedstoneMode extends UPMScreenMode{

	public static final ResourceLocation BACKGROUND = new ResourceLocation(UPMAPI.MOD_ID, "textures/gui/redstone.png");
	public static final int WIDTH = 195;
	public static final int HEIGHT = 183;

	public static final String REDSTONE_MODE_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPMAPI.MOD_ID, "redstone_mode"));

	private final ItemStack upmIcon = new ItemStack(ModRegisters.UPM_ITEM::get);
	private final ItemStack redstoneIcon = new ItemStack(Items.REDSTONE);
	private final ItemRenderer itemRenderer = ClientHelper.getItemRenderer();

	private final IFormattableTextComponent redstoneConfigText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "tab.redstone"))).withStyle(TextFormatting.BOLD);
	private final TranslationTextComponent configTypesText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_types")));
	private final IFormattableTextComponent noneText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_none"))).withStyle(TextFormatting.ITALIC);
	private final TranslationTextComponent thresholdsText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_thresholds")));
	private final TranslationTextComponent upperText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_thresholds.upper")));
	private final TranslationTextComponent lowerText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_thresholds.lower")));
	private final IFormattableTextComponent onText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_on"))).withStyle(TextFormatting.DARK_GREEN, TextFormatting.BOLD);
	private final IFormattableTextComponent offText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "redstone_off"))).withStyle(TextFormatting.DARK_RED, TextFormatting.BOLD);

	private IFormattableTextComponent typesText = this.noneText;

	private UPMRedstoneConfiguration configuration;
	private MinMaxRedstoneSlider maxSlider;
	private MinMaxRedstoneSlider minSlider;
	private MemberTypeList typeList;
	private ExtendedButton saveButton;

	@Override
	public void init() {
		this.configuration = this.getUPM().getRedstoneConfig().clone();
		this.maxSlider = new MinMaxRedstoneSlider(this.screen.getGuiLeft()+80, this.screen.getGuiTop()+105, 105, this.configuration.getMax()/100D, true);
		this.screen.addWidget(this.maxSlider);
		this.minSlider = this.maxSlider.makeOther(this.screen.getGuiLeft()+80, this.screen.getGuiTop()+128, this.configuration.getMin()/100D);
		this.screen.addWidget(this.minSlider);
		this.typeList = new MemberTypeList(this.screen.getMinecraft(), this.screen.getGuiLeft()+110, this.screen.getGuiTop()+45, 75, 45);
		this.screen.addWidget(this.typeList);
		this.saveButton = new ExtendedButton(this.screen.getGuiLeft()+134, this.screen.getGuiTop()+155, 50, 20, new StringTextComponent("Save"), this::saveButtonPressed);
		this.redstoneConfigChanged();
		this.screen.addButton(this.saveButton);
	}

	@Override
	public int getWidth() {
		return RedstoneMode.WIDTH;
	}

	@Override
	public int getHeight() {
		return RedstoneMode.HEIGHT;
	}

	@Override
	public UPMScreenModeType getType() {
		return UPMScreenModeType.REDSTONE;
	}

	@Override
	public boolean networkChanged(final UPMScreenModeType type) {
		return type == UPMScreenModeType.NO_NETWORK;
	}

	private void redstoneConfigChanged() {
		this.saveButton.active = !this.configuration.equals(this.getUPM().getRedstoneConfig());
		final EnumSet<MemberType> enabledTypes = EnumSet.noneOf(MemberType.class);
		for(final MemberTypeEntry entry:this.typeList.children())
			if(entry.active)
				enabledTypes.add(entry.type);
		this.typesText = enabledTypes.isEmpty() ? this.noneText:
			new StringTextComponent(TextComponentUtils.formatAndSortList(enabledTypes,
					type -> new StringTextComponent(type.name())).getString()).withStyle(TextFormatting.ITALIC);
	}

	private void saveButtonPressed(final Button button) {
		this.getUPM().setRedstoneConfig(this.configuration);
		this.redstoneConfigChanged();
		UPMPacketHandler.INSTANCE.sendToServer(new UPMRedstoneConfigPacket(this.getUPM().getBlockPos(), this.configuration));
	}

	@Override
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.screen.getMinecraft().getTextureManager().bind(RedstoneMode.BACKGROUND);

		this.screen.blit(stack, this.screen.getGuiLeft()-23, this.screen.getGuiTop(), 195, 15, 29, 28);
		this.screen.blit(stack, this.screen.getGuiLeft(), this.screen.getGuiTop(), 0, 0, this.getWidth(), this.getHeight());
		this.screen.blit(stack, this.screen.getGuiLeft()-25, this.screen.getGuiTop()+28, 195, 71, 29, 28);

		this.itemRenderer.renderAndDecorateItem(this.upmIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+6);
		this.itemRenderer.renderGuiItemDecorations(this.screen.getFont(), this.upmIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+6);

		this.itemRenderer.renderAndDecorateItem(this.redstoneIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+34);
		this.itemRenderer.renderGuiItemDecorations(this.screen.getFont(), this.redstoneIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+34);
	}

	@Override
	public void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		final FontRenderer font = this.screen.getFont();
		final int black = TextFormatting.BLACK.getColor();

		font.draw(stack, this.redstoneConfigText, 8, 25, black);
		font.draw(stack, this.configTypesText, 8, 45, black);
		font.drawWordWrap(this.typesText, 14, 45+font.lineHeight+5, 90, black);

		font.draw(stack, this.thresholdsText, 8, 94, black);
		font.draw(stack, this.upperText, 14, 109, black);
		font.draw(stack, this.lowerText, 14, 132, black);
		final boolean on = this.getUPM().isRedstoneOutput();
		font.draw(stack, new TranslationTextComponent(RedstoneMode.REDSTONE_MODE_ID, on ? this.onText:this.offText), 8, 162, black);
	}

	@Override
	public void renderWidgets(final MatrixStack stack, final int mouseX, final int mouseY, final float partial) {
		this.maxSlider.render(stack, mouseX, mouseY, partial);
		this.minSlider.render(stack, mouseX, mouseY, partial);
		this.typeList.render(stack, mouseX, mouseY, partial);
	}

	@Override
	public void renderTooltip(final MatrixStack stack, final int mouseX, final int mouseY) {
		final Optional<UPMScreenModeType> hoveredTab = this.getHoveredTab(mouseX, mouseY);
		if(hoveredTab.isPresent())
			this.screen.renderTooltip(stack, hoveredTab.get().getTabHover(), mouseX, mouseY);
		else
			super.renderTooltip(stack, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
		if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_1)
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		final Optional<UPMScreenModeType> hoveredTab = this.getHoveredTab(mouseX, mouseY).filter(tab -> tab != this.getType());
		if(hoveredTab.isPresent()) {
			this.screen.changeMode(hoveredTab.get());
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public Optional<UPMScreenModeType> getHoveredTab(double mouseX, double mouseY){
		mouseX -= this.screen.getGuiLeft();
		mouseY -= this.screen.getGuiTop();
		if(mouseY >= 0 && mouseY < 28 && mouseX < 0 && mouseX >= -25)
			return Optional.of(UPMScreenModeType.NETWORK);
		if(mouseY >= 28 && mouseY < 56 && mouseX < 2 && mouseX >= -24)
			return Optional.of(UPMScreenModeType.REDSTONE);
		return Optional.empty();
	}

	public static ITextComponent redstoneMessageFromValue(final double value) {
		return new StringTextComponent(Integer.toString((int) Math.round(100*value))+"%");
	}

	public class MemberTypeList extends AbstractList<MemberTypeEntry>{

		public MemberTypeList(final Minecraft mc, final int x, final int y, final int width, final int height) {
			super(mc, width, height, y, y+height, mc.font.lineHeight+4);
			this.setRenderBackground(false);
			this.setRenderTopAndBottom(false);
			this.setLeftPos(x);
			for(final MemberType type:MemberType.values()) {
				final MemberTypeEntry entry = new MemberTypeEntry(type);
				if(RedstoneMode.this.configuration.getTypes().contains(type))
					entry.setActive(true);
				this.addEntry(entry);
			}
		}

		@Override
		public int getRowWidth() {
			return this.width;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.x1-6;
		}

		@Override
		public void render(final MatrixStack stack, final int mouseX, final int mouseY, final float partial) {
			final double scale = this.minecraft.getWindow().getGuiScale();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor((int)(this.x0  * scale), (int)(this.minecraft.getWindow().getHeight() - this.y1 * scale),
					(int)(this.width * scale), (int)(this.height * scale));
			super.render(stack, mouseX, mouseY, partial);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}

		@Override
		protected void renderBackground(final MatrixStack stack) {
			AbstractGui.fill(stack, this.x0, this.y0, this.x1, this.y1, Color.BLACK.getRGB());
		}

		@Override
		public boolean keyReleased(final int key, final int input_1, final int input_2) {
			final MemberTypeEntry entry = this.getSelected();
			if(entry != null && entry.keyReleased(key, input_1, input_2))
				return true;
			return super.keyReleased(key, input_1, input_2);
		}

	}

	@RequiredArgsConstructor
	public class MemberTypeEntry extends AbstractList.AbstractListEntry<MemberTypeEntry>{

		@Getter
		private final MemberType type;
		@Setter
		private boolean active;

		@Override
		public void render(final MatrixStack stack, final int entryIdx, final int top, final int left, final int entryWidth,
				final int entryHeight, final int mouseX, final int mouseY, final boolean mouseOver, final float partialTicks) {
			ClientHelper.getFontRenderer().draw(stack, this.type.name(), left, top+1, this.active ? TextFormatting.GREEN.getColor():TextFormatting.GRAY.getColor());
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
			if(button == GLFW.GLFW_MOUSE_BUTTON_1) {
				this.changeActive();
				this.list.setSelected(this);
			}
			return true;
		}

		@Override
		public boolean keyReleased(final int key, final int input_1, final int input_2) {
			if(key == GLFW.GLFW_KEY_ENTER) {
				this.changeActive();
				return true;
			}
			return super.keyReleased(key, input_1, input_2);
		}

		private void changeActive() {
			this.active = !this.active;
			if(this.active)
				RedstoneMode.this.configuration.getTypes().add(this.type);
			else
				RedstoneMode.this.configuration.getTypes().remove(this.type);
			RedstoneMode.this.redstoneConfigChanged();
		}

	}

	public class MinMaxRedstoneSlider extends AbstractSlider{

		private final boolean max;
		private MinMaxRedstoneSlider other;

		public MinMaxRedstoneSlider(final int x, final int y, final int width,
				final double initValue, final boolean max) {
			super(x, y, width, 20, RedstoneMode.redstoneMessageFromValue(initValue), initValue);
			this.max = max;
		}

		public MinMaxRedstoneSlider makeOther(final int x, final int y, final double initValue) {
			this.other = new MinMaxRedstoneSlider(x, y, this.width, initValue, !this.max);
			this.other.other = this;
			this.ensureBounds();
			return this.other;
		}

		@Override
		protected void updateMessage() {
			this.setMessage(RedstoneMode.redstoneMessageFromValue(this.value));
		}

		@Override
		public boolean mouseScrolled(final double mouseX, final double mouseY, final double scroll) {
			final double oldVal = this.value;
			this.value = MathHelper.clamp(this.value+scroll/100D, 0, 1);
			if(this.value != oldVal)
				this.applyValue();
			return true;
		}

		@Override
		protected void applyValue() {
			this.ensureBounds();
			final int val = (int) Math.round(100*this.value);
			if(this.max)
				RedstoneMode.this.configuration.setMax(val);
			else
				RedstoneMode.this.configuration.setMin(val);
			RedstoneMode.this.redstoneConfigChanged();
		}

		protected void ensureBounds() {
			if(this.max)
				this.value = Math.max(this.value, this.other.value);
			else
				this.value = Math.min(this.value, this.other.value);
		}

	}

}
