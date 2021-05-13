package me.superckl.upm.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;

import me.superckl.upm.ClientHelper;
import me.superckl.upm.ModRegisters;
import me.superckl.upm.api.MemberType;
import me.superckl.upm.api.UPMAPI;
import me.superckl.upm.network.EnergyNetwork;
import me.superckl.upm.network.member.WrappedNetworkMember;
import me.superckl.upm.network.member.stack.NetworkItemStackHelper;
import me.superckl.upm.packet.RequestUPMScanPacket;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.util.NumberUtil;
import me.superckl.upm.util.SlotChangeTimer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class NetworkMode extends UPMScreenMode{

	public static final ResourceLocation BACKGROUND = new ResourceLocation(UPMAPI.MOD_ID, "textures/gui/network.png");
	public static final int WIDTH = 195;
	public static final int HEIGHT = 183;

	private final TranslationTextComponent totalText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "total_energy")));
	private final TranslationTextComponent storageText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "storage_energy")));
	private final TranslationTextComponent cableText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "cable_energy")));
	private final TranslationTextComponent machineText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "machine_energy")));
	private final TranslationTextComponent generatorText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "generator_energy")));

	public static final String CONNECTED_BLOCKS_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPMAPI.MOD_ID, "connected_blocks"));

	private final IFormattableTextComponent configChangedText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "config_changed"))).withStyle(TextFormatting.DARK_RED);
	private static final IFormattableTextComponent rescanSaveText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "rescan_save"))).withStyle(TextFormatting.RED);
	private final IFormattableTextComponent multipleTypesText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "multiple_blocks"))).withStyle(TextFormatting.RED);

	private static final IFormattableTextComponent clickToCycleText = new TranslationTextComponent(Util.makeDescriptionId("gui",
			new ResourceLocation(UPMAPI.MOD_ID, "click_cycle"))).withStyle(TextFormatting.GRAY);

	private final Inventory slotInv = new Inventory(9*3);
	private Multimap<MemberType, NetworkItemStackHelper> typeToHelpers;
	private List<NetworkItemStackHelper> slotToHelpers;

	private Button scanButton;
	private float scrollBar;
	private boolean scrolling;
	private double scrollBarRelY;

	private int numBlocks;

	private final ItemStack upmIcon = new ItemStack(ModRegisters.UPM_ITEM::get);
	private final ItemStack redstoneIcon = new ItemStack(Items.REDSTONE);
	private final ItemRenderer itemRenderer = ClientHelper.getItemRenderer();

	@Override
	public void init() {
		final int width = 60;
		final int height = 20;
		this.scanButton = this.screen.newScanButton(this.screen.getGuiLeft()+this.getWidth()-width-7, this.screen.getGuiTop()+123-24, width, height,
				true, this::onScanButtonPress);
		this.scanButton.active = this.getUPM().canScan();
		this.screen.addButton(this.scanButton);
	}

	@Override
	public void upmScanStateChanged(final boolean state) {
		this.scanButton.active = state;
	}

	@Override
	public boolean networkChanged(final UPMScreenModeType type) {
		if(type != this.getType())
			return true;
		this.consolidateToItems(this.getNetwork());
		this.updateInventoryAndSlots();
		return false;
	}

	public void onScanButtonPress(final Button button) {
		final Map<TileEntityType<?>, MemberType> typeOverrides = new IdentityHashMap<>();
		this.screen.getMenu().slots.forEach(slot -> {
			if(slot instanceof NetworkBlockSlot) {
				final NetworkBlockSlot nSlot = (NetworkBlockSlot) slot;
				if(nSlot.isTypeChanged())
					nSlot.member.getMembers().forEach(wrapped -> {
						wrapped.getTileTypes().forEach(type -> typeOverrides.put(type, nSlot.getType()));
					});
			}
		});
		UPMPacketHandler.INSTANCE.sendToServer(new RequestUPMScanPacket(this.getUPM().getBlockPos(), typeOverrides));
	}

	@Override
	public void initSlots(final UPMClientSideContainer container) {
		this.consolidateToItems(this.getNetwork());
		if(!this.typeToHelpers.isEmpty()) {
			this.updateInventory();
			final int startX = 9;
			final int startY = 123;
			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 3; j++) {
					final int index = j*9+i;
					final NetworkItemStackHelper member = index < this.slotToHelpers.size() ? this.slotToHelpers.get(index):null;
					container.addSlot(new NetworkBlockSlot(this.slotInv, index, startX+i*18, startY+j*18, member));
				}
		}
	}

	public void updateInventoryAndSlots() {
		this.updateInventory();
		for (final Slot slot:this.screen.getMenu().slots)
			if(slot instanceof NetworkBlockSlot) {
				final NetworkItemStackHelper helper = slot.getSlotIndex() < this.slotToHelpers.size() ? this.slotToHelpers.get(slot.getSlotIndex()):null;
				((NetworkBlockSlot)slot).setMember(helper);
			}
	}

	public void updateInventory() {
		final List<NetworkItemStackHelper> helpers = new ArrayList<>(Math.min(this.typeToHelpers.size(), 9*3));
		if(!this.typeToHelpers.isEmpty()) {
			int toSkip = Math.max(0, Math.round((this.getTotalRows()-3)*this.scrollBar)*9);
			int invIndex = 0;
			for(final MemberType type:MemberType.values())
				if(invIndex < this.slotInv.getContainerSize())
					for(final NetworkItemStackHelper member:this.typeToHelpers.get(type)) {
						if(toSkip-- > 0)
							continue;
						if(invIndex++ >= this.slotInv.getContainerSize())
							break;
						helpers.add(member);
					}
		}
		this.slotToHelpers = helpers;
	}

	private void consolidateToItems(final EnergyNetwork network) {
		this.numBlocks = 0;
		final Multimap<MemberType, NetworkItemStackHelper> type2Helpers = MultimapBuilder.enumKeys(MemberType.class).arrayListValues().build();
		network.getMembers().forEach(member -> {
			final Collection<NetworkItemStackHelper> helpers = type2Helpers.get(member.getType());
			boolean added = false;
			for(final NetworkItemStackHelper helper:helpers)
				if(helper.accepts(member) && helper.add(member, network.getLevel()))
					added = true;
			if(!added)
				helpers.add(NetworkItemStackHelper.from(member, network.getLevel())
						.orElseThrow(() -> new IllegalStateException("Network member "+member+" could not be resolved to an item")));
		});
		type2Helpers.values().forEach(stack -> this.numBlocks += stack.toStacks().stream().mapToInt(ItemStack::getCount).sum());
		this.typeToHelpers = type2Helpers;
	}

	@Override
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.screen.getMinecraft().getTextureManager().bind(NetworkMode.BACKGROUND);

		this.screen.blit(stack, this.screen.getGuiLeft()-23, this.screen.getGuiTop()+28, 195, 15, 29, 28);
		this.screen.blit(stack, this.screen.getGuiLeft(), this.screen.getGuiTop(), 0, 0, this.getWidth(), this.getHeight());
		this.screen.blit(stack, this.screen.getGuiLeft()-25, this.screen.getGuiTop(), 195, 43, 29, 28);

		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+20, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+35, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+50, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+65, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+80, 0, 195, 80, 12);

		this.screen.blit(stack, this.screen.getGuiLeft()+175, this.screen.getGuiTop()+123+Math.round((52-15)*this.scrollBar), this.needsScrollBars() ? 195:207, 0, 12, 15);

		final EnergyNetwork network = this.getNetwork();

		long storage = network.getTotalStorage();
		double percentage = storage == 0 ? 0:(double)network.getTotalStored()/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+20, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.STORAGE);
		percentage = storage == 0 ? 0:(double)network.getStored(MemberType.STORAGE)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+35, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.CABLE);
		percentage = storage == 0 ? 0:(double)network.getStored(MemberType.CABLE)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+50, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.MACHINE);
		percentage = storage == 0 ? 0:(double)network.getStored(MemberType.MACHINE)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+65, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.GENERATOR);
		percentage = storage == 0 ? 0:(double)network.getStored(MemberType.GENERATOR)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+80, 0, 183, (int) Math.round(80*percentage), 12);

		this.itemRenderer.renderAndDecorateItem(this.upmIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+6);
		this.itemRenderer.renderGuiItemDecorations(this.screen.getFont(), this.upmIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+6);

		this.itemRenderer.renderAndDecorateItem(this.redstoneIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+34);
		this.itemRenderer.renderGuiItemDecorations(this.screen.getFont(), this.redstoneIcon, this.screen.getGuiLeft()-18, this.screen.getGuiTop()+34);
	}

	@Override
	public void renderTooltip(final MatrixStack stack, final int mouseX, final int mouseY) {
		final List<ITextComponent> tooltip = this.tooltipForBarHover(mouseX, mouseY);
		if(tooltip != null)
			this.screen.renderComponentTooltip(stack, tooltip, mouseX, mouseY);
		else if(this.isOverConfigChanged(mouseX, mouseY))
			this.screen.renderTooltip(stack, NetworkMode.rescanSaveText, mouseX, mouseY);
		else {
			final Optional<UPMScreenModeType> hoveredTab = this.getHoveredTab(mouseX, mouseY);
			if(hoveredTab.isPresent())
				this.screen.renderTooltip(stack, hoveredTab.get().getTabHover(), mouseX, mouseY);
			else
				super.renderTooltip(stack, mouseX, mouseY);
		}
	}

	@Override
	public void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		final FontRenderer font = this.screen.getFont();
		final IFormattableTextComponent connectedBlocks = new TranslationTextComponent(NetworkMode.CONNECTED_BLOCKS_ID, this.numBlocks);
		final int black = TextFormatting.BLACK.getColor();
		font.draw(stack, connectedBlocks, 8, 123-font.lineHeight-2, black);

		font.draw(stack, this.totalText, 8, 21.5F, black);
		font.draw(stack, this.storageText, 14, 36.5F, black);
		font.draw(stack, this.cableText, 14, 51.5F, black);
		font.draw(stack, this.machineText, 14, 66.5F, black);
		font.draw(stack, this.generatorText, 14, 81.5F, black);

		final EnergyNetwork network = this.getNetwork();
		this.renderGainIndicator(network.deltaTotalStored(), NetworkMode.WIDTH-80-15, 22F, stack);
		this.renderGainIndicator(network.deltaStored(MemberType.STORAGE), NetworkMode.WIDTH-80-15, 37F, stack);
		this.renderGainIndicator(network.deltaStored(MemberType.CABLE), NetworkMode.WIDTH-80-15, 52F, stack);
		this.renderGainIndicator(network.deltaStored(MemberType.MACHINE), NetworkMode.WIDTH-80-15, 67F, stack);
		this.renderGainIndicator(network.deltaStored(MemberType.GENERATOR), NetworkMode.WIDTH-80-15, 82F, stack);

		if(this.hasTypeOverride())
			font.draw(stack, this.configChangedText, 8, 100, black);
	}

	private boolean isOverConfigChanged(int mouseX, int mouseY) {
		if(!this.hasTypeOverride())
			return false;
		mouseX -= this.screen.getGuiLeft();
		mouseY -= this.screen.getGuiTop();
		final int width = this.screen.getFont().width(this.configChangedText);
		return mouseX >= 8 && mouseX < 8+width && mouseY >= 100 && mouseY < 100+this.screen.getFont().lineHeight;
	}

	private void renderGainIndicator(final long gain, final float x, final float y, final MatrixStack stack) {
		if(gain == 0)
			return;
		final int color = gain > 0 ? TextFormatting.DARK_GREEN.getColor():TextFormatting.DARK_RED.getColor();
		final String text = gain > 0 ? "+":"-";
		this.screen.getFont().draw(stack, new StringTextComponent(text).withStyle(TextFormatting.BOLD), x, y, color);
	}

	@Override
	public boolean mouseScrolled(final double mouseX, final double mouseY, final double scroll) {
		if(!this.needsScrollBars())
			return false;
		final int numRows = this.getTotalRows();
		this.scrollBar = (float) (this.scrollBar-scroll/(numRows-3));
		this.scrollBar = MathHelper.clamp(this.scrollBar, 0, 1);
		this.updateInventoryAndSlots();
		return true;
	}

	@Override
	public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
		if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_1)
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		if(this.isInScrollBar(mouseX, mouseY)) {
			final int scrollBarY = this.screen.getGuiTop()+123+Math.round((52-15)*this.scrollBar);
			this.scrollBarRelY = scrollBarY - mouseY;
			this.scrolling = true;
			return true;
		}
		final Optional<UPMScreenModeType> hoveredTab = this.getHoveredTab(mouseX, mouseY).filter(tab -> tab != this.getType());
		if(hoveredTab.isPresent()) {
			this.screen.changeMode(hoveredTab.get());
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseDragged(final double newX, final double newY, final int button, final double deltaX, final double deltaY) {
		if(button != GLFW.GLFW_MOUSE_BUTTON_1)
			return super.mouseDragged(newX, newY, button, deltaX, deltaY);
		if(this.scrolling) {
			final int barMinY = this.screen.getGuiTop()+123;
			final int barMaxY = barMinY+52-15;
			final double newScrollY = MathHelper.clamp(newY+this.scrollBarRelY, barMinY, barMaxY);
			final float prevScroll = this.scrollBar;
			this.scrollBar = (float) ((newScrollY-barMinY)/(barMaxY-barMinY));

			final int totalRows =this.getTotalRows();
			final int toSkipBefore = Math.round((totalRows-3)*prevScroll);
			final int toSkipNow = Math.round((totalRows-3)*this.scrollBar);
			if(toSkipBefore != toSkipNow)
				this.updateInventoryAndSlots();
			return true;
		}
		return super.mouseDragged(newX, newY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
		if(button != GLFW.GLFW_MOUSE_BUTTON_1)
			return super.mouseReleased(mouseX, mouseY, button);
		if(this.scrolling) {
			this.scrolling = false;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private boolean needsScrollBars() {
		return this.typeToHelpers != null && this.typeToHelpers.size() > this.slotInv.getContainerSize();
	}

	private int getTotalRows() {
		return MathHelper.ceil(this.typeToHelpers.size()/9F);
	}

	public boolean isInScrollBar(final double mouseX, final double mouseY) {
		final int scrollBarX = this.screen.getGuiLeft()+175;
		final int scrollBarY = this.screen.getGuiTop()+123+Math.round((52-15)*this.scrollBar);
		return mouseX >= scrollBarX && mouseX < scrollBarX + 12 && mouseY >= scrollBarY && mouseY < scrollBarY+15;
	}

	public Optional<UPMScreenModeType> getHoveredTab(double mouseX, double mouseY){
		mouseX -= this.screen.getGuiLeft();
		mouseY -= this.screen.getGuiTop();
		if(mouseY >= 0 && mouseY < 28 && mouseX < 2 && mouseX >= -25)
			return Optional.of(UPMScreenModeType.NETWORK);
		if(mouseY >= 28 && mouseY < 56 && mouseX < 0 && mouseX >= -24)
			return Optional.of(UPMScreenModeType.REDSTONE);
		return Optional.empty();
	}

	private List<ITextComponent> tooltipForBarHover(final int mouseX, final int mouseY){
		if(mouseX >= this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8 && mouseX < this.screen.getGuiLeft()+NetworkMode.WIDTH-8) {
			boolean isTotal = false;
			MemberType type = null;

			if(mouseY >= this.screen.getGuiTop()+20 && mouseY < this.screen.getGuiTop()+32)
				isTotal = true;
			else if(mouseY >= this.screen.getGuiTop()+35 && mouseY < this.screen.getGuiTop()+47)
				type = MemberType.STORAGE;
			else if(mouseY >= this.screen.getGuiTop()+50 && mouseY < this.screen.getGuiTop()+62)
				type = MemberType.CABLE;
			else if(mouseY >= this.screen.getGuiTop()+65 && mouseY < this.screen.getGuiTop()+77)
				type = MemberType.MACHINE;
			else if(mouseY >= this.screen.getGuiTop()+80 && mouseY < this.screen.getGuiTop()+92)
				type = MemberType.GENERATOR;

			if(isTotal || type != null) {
				long stored;
				long storage;
				long gain;
				final EnergyNetwork network = this.getNetwork();
				if(isTotal) {
					stored = network.getTotalStored();
					storage = network.getTotalStorage();
					gain = network.deltaTotalStored();
				}else {
					stored = network.getStored(type);
					storage = network.getStorage(type);
					gain = network.deltaStored(type);
				}
				final List<ITextComponent> tooltip = new ArrayList<>();

				final String percentage = storage == 0 ? "-":Integer.toString((int) Math.round(100*(double) stored/storage));
				final IFormattableTextComponent storedPower = new StringTextComponent(String.format("%s/%s FE (%s%%)", NumberUtil.format(stored), NumberUtil.format(storage), percentage));
				tooltip.add(storedPower);

				final IFormattableTextComponent powerGain = new StringTextComponent(String.format("%s FE/t", NumberUtil.format(gain)));
				tooltip.add(powerGain);
				return tooltip;
			}
		}
		return null;
	}

	@Override
	public void slotClicked(final Slot slot, final int mouseX, final int mouseY, final ClickType type) {
		if(slot instanceof NetworkBlockSlot)
			((NetworkBlockSlot)slot).cycleType();
	}

	@Override
	public int getSlotBackgroundColor(final Slot slot) {
		if(slot instanceof NetworkBlockSlot)
			return ((NetworkBlockSlot)slot).getType().color().getColor() | 120 << 24;
		return super.getSlotBackgroundColor(slot);
	}

	@SuppressWarnings("resource")
	@Override
	public List<ITextComponent> tooltipForSlot(final Slot slot) {
		final List<ITextComponent> tooltip = super.tooltipForSlot(slot);
		if(slot instanceof NetworkBlockSlot && ((NetworkBlockSlot)slot).member != null) {
			final MemberType type = ((NetworkBlockSlot)slot).getType();
			tooltip.add(new StringTextComponent(type.name()).withStyle(type.color()));
			if(((NetworkBlockSlot)slot).hasMultipleItems())
				tooltip.add(this.multipleTypesText);
			if(((NetworkBlockSlot)slot).isTypeChanged())
				tooltip.add(NetworkMode.rescanSaveText);
			tooltip.add(NetworkMode.clickToCycleText);
			if(this.screen.getMinecraft().options.advancedItemTooltips) {
				final Set<TileEntityType<?>> tileTypes = Collections.newSetFromMap(new IdentityHashMap<>());
				((NetworkBlockSlot)slot).member.getMembers().forEach(wrapped -> tileTypes.addAll(wrapped.getTileTypes()));
				tileTypes.forEach(tileType -> tooltip.add(new StringTextComponent(tileType.getRegistryName().toString()).withStyle(TextFormatting.DARK_GRAY)));
			}
		}
		return tooltip;
	}

	public boolean hasTypeOverride() {
		for(final Slot slot:this.screen.getMenu().slots)
			if(slot instanceof NetworkBlockSlot && ((NetworkBlockSlot)slot).isTypeChanged())
				return true;
		return false;
	}

	@Override
	public int getWidth() {
		return NetworkMode.WIDTH;
	}

	@Override
	public int getHeight() {
		return NetworkMode.HEIGHT;
	}

	@Override
	public UPMScreenModeType getType() {
		return UPMScreenModeType.NETWORK;
	}

	public static class NetworkBlockSlot extends Slot{

		private NetworkItemStackHelper member;
		private MemberType type;
		private List<ItemStack> stacks;
		private SlotChangeTimer timer;

		public NetworkBlockSlot(final IInventory inv, final int slot, final int x, final int y, final NetworkItemStackHelper member) {
			super(inv, slot, x, y);
			this.setMember(member);
		}

		@Override
		public ItemStack getItem() {
			if(this.member == null)
				return ItemStack.EMPTY;
			return this.stacks.get(this.timer.getValue());
		}

		@Override
		public boolean mayPickup(final PlayerEntity p_82869_1_) {
			return false;
		}

		@Override
		public boolean mayPlace(final ItemStack p_75214_1_) {
			return false;
		}

		public MemberType getType() {
			return this.type;
		}

		public void cycleType() {
			this.type = this.type.cycle();
		}

		public boolean isTypeChanged() {
			return this.member != null && this.type != this.member.getType();
		}

		public boolean hasMultipleItems() {
			return this.stacks != null && this.stacks.size() > 1;
		}

		public Collection<WrappedNetworkMember> getMembers(){
			return this.member == null ? Collections.emptyList():this.member.getMembers();
		}

		public void setMember(final NetworkItemStackHelper member) {
			this.member = member;
			this.type = this.member == null ? MemberType.UNKNOWN:this.member.getType();
			if(member != null) {
				this.stacks = member.toStacks();
				this.timer = new SlotChangeTimer(member.toStacks().size(), 1800);
			}

		}

	}

	public static class UnlimitedInventory extends Inventory{

		public UnlimitedInventory(final int size) {
			super(size);
		}

		@Override
		public int getMaxStackSize() {
			return Integer.MAX_VALUE;
		}

	}

}
