package me.superckl.upm.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;

import me.superckl.upm.UPM;
import me.superckl.upm.network.EnergyNetwork;
import me.superckl.upm.network.member.MemberType;
import me.superckl.upm.network.member.stack.NetworkItemStackHelper;
import me.superckl.upm.packet.RequestUPMScanPacket;
import me.superckl.upm.packet.UPMPacketHandler;
import me.superckl.upm.util.NumberUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class NetworkMode extends UPMScreenMode{

	public static final ResourceLocation BACKGROUND = new ResourceLocation(UPM.MOD_ID, "textures/gui/network.png");
	public static final int WIDTH = 195;
	public static final int HEIGHT = 183;

	public static final String STORED_TOTAL_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "total_energy"));
	public static final String STORED_STORAGE_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "storage_energy"));
	public static final String STORED_CABLE_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "cable_energy"));
	public static final String STORED_MACHINE_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "machine_energy"));
	public static final String STORED_GENERATOR_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "generator_energy"));

	public static final String CONNECTED_BLOCKS_ID = Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "connected_blocks"));

	private Button scanButton;

	private int numBlocks;

	@Override
	public void init() {
		final int width = 60;
		final int height = 20;
		this.scanButton = new Button(this.screen.getGuiLeft()+this.getWidth()-width-7, this.screen.getGuiTop()+123-24, width, height,
				new TranslationTextComponent(Util.makeDescriptionId("gui", new ResourceLocation(UPM.MOD_ID, "rescan"))), this::onScanButtonPress);
		this.scanButton.active = this.screen.getMenu().getNetwork().getOwner().canScan();
		this.screen.addButton(this.scanButton);

	}

	@Override
	public void upmScanStateChanged(final boolean state) {
		this.scanButton.active = state;
	}

	public void onScanButtonPress(final Button button) {
		final Map<TileEntityType<?>, MemberType> typeOverrides = new IdentityHashMap<>();
		if(this.hasTypeOverride())
			this.screen.getMenu().slots.forEach(slot -> {
				if(slot instanceof NetworkBlockSlot) {
					final NetworkBlockSlot nSlot = (NetworkBlockSlot) slot;
					if(nSlot.member != null && nSlot.member.hasTypeOverride())
						nSlot.member.getMembers().forEach(wrapped -> {
							wrapped.getTileTypes().forEach(type -> typeOverrides.put(type, wrapped.getType()));
						});
				}
			});
		UPMPacketHandler.INSTANCE.sendToServer(new RequestUPMScanPacket(this.screen.getMenu().getUPMPosition(), typeOverrides));
	}

	@Override
	public void initSlots(final UPMClientSideContainer container) {
		int numBlocks = 0;
		final Inventory inv = new UnlimitedInventory(9*3);
		final Multimap<MemberType, NetworkItemStackHelper> networkItems = this.consolidateToItems(container.getNetwork());
		final List<NetworkItemStackHelper> helpers = new ArrayList<>(Math.min(networkItems.size(), 9*3));
		if(!networkItems.isEmpty()) {
			int invIndex = 0;
			final int startX = 9;
			final int startY = 123;
			for(final MemberType type:MemberType.values())
				if(invIndex < inv.getContainerSize())
					for(final NetworkItemStackHelper member:networkItems.get(type)) {
						if(invIndex >= inv.getContainerSize())
							break;
						final ItemStack stack = member.toStack();
						inv.setItem(invIndex++, stack);
						numBlocks += stack.getCount();
						helpers.add(member);
					}
			this.numBlocks = numBlocks;
			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 3; j++) {
					final int index = j*9+i;
					final NetworkItemStackHelper member = index < helpers.size() ? helpers.get(index):null;
					container.addSlot(new NetworkBlockSlot(inv, index, startX+i*18, startY+j*18, member));
				}
		}
	}

	private Multimap<MemberType, NetworkItemStackHelper> consolidateToItems(final EnergyNetwork network) {
		//TODO need to map type to blocks for proper consolidation, but also attach network members to blocks. Use Multimap<Block, WrappedNetworkMember> as value, type as key??
		final Multimap<MemberType, NetworkItemStackHelper> type2Helpers = MultimapBuilder.enumKeys(MemberType.class).arrayListValues().build();
		network.getMembers().forEach(member -> {
			final Collection<NetworkItemStackHelper> helpers = type2Helpers.get(member.getType());
			boolean added = false;
			for(final NetworkItemStackHelper helper:helpers)
				if(helper.accepts(member) && helper.add(member, network.getLevel()))
					added = true;
			if(!added)
				helpers.add(NetworkItemStackHelper.from(member, network.getLevel()));
		});
		return type2Helpers;
	}

	@Override
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.screen.getMinecraft().getTextureManager().bind(NetworkMode.BACKGROUND);
		this.screen.blit(stack, this.screen.getGuiLeft(), this.screen.getGuiTop(), 0, 0, this.getWidth(), this.getHeight());

		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+20, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+35, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+50, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+65, 0, 195, 80, 12);
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+80, 0, 195, 80, 12);

		final EnergyNetwork network = this.screen.getMenu().getNetwork();

		long storage = network.getTotalStorage();
		double percentage = storage == 0 ? 1:(double)network.getTotalStored()/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+20, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.STORAGE);
		percentage = storage == 0 ? 1:(double)network.getStored(MemberType.STORAGE)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+35, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.CABLE);
		percentage = storage == 0 ? 1:(double)network.getStored(MemberType.CABLE)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+50, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.MACHINE);
		percentage = storage == 0 ? 1:(double)network.getStored(MemberType.MACHINE)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+65, 0, 183, (int) Math.round(80*percentage), 12);

		storage = network.getStorage(MemberType.GENERATOR);
		percentage = storage == 0 ? 1:(double)network.getStored(MemberType.GENERATOR)/storage;
		this.screen.blit(stack, this.screen.getGuiLeft()+NetworkMode.WIDTH-80-8, this.screen.getGuiTop()+80, 0, 183, (int) Math.round(80*percentage), 12);
	}

	@Override
	public void renderTooltip(final MatrixStack stack, final int mouseX, final int mouseY) {
		final List<ITextComponent> tooltip = this.tooltipForBarHover(mouseX, mouseY);
		if(tooltip != null)
			this.screen.renderComponentTooltip(stack, tooltip, mouseX, mouseY);
		else
			super.renderTooltip(stack, mouseX, mouseY);
	}

	@Override
	public void renderLabels(final MatrixStack stack, final int mouseX, final int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		final FontRenderer font = this.screen.getFont();
		final IFormattableTextComponent connectedBlocks = new TranslationTextComponent(NetworkMode.CONNECTED_BLOCKS_ID, this.numBlocks);
		final int black = TextFormatting.BLACK.getColor();
		font.draw(stack, connectedBlocks, 8, 123-font.lineHeight-2, black);

		font.draw(stack, new TranslationTextComponent(NetworkMode.STORED_TOTAL_ID), 8, 21.5F, black);
		font.draw(stack, new TranslationTextComponent(NetworkMode.STORED_STORAGE_ID), 14, 36.5F, black);
		font.draw(stack, new TranslationTextComponent(NetworkMode.STORED_CABLE_ID), 14, 51.5F, black);
		font.draw(stack, new TranslationTextComponent(NetworkMode.STORED_MACHINE_ID), 14, 66.5F, black);
		font.draw(stack, new TranslationTextComponent(NetworkMode.STORED_GENERATOR_ID), 14, 81.5F, black);
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

			if(isTotal || type != null) {
				long stored;
				long storage;
				long gain;
				final EnergyNetwork network = this.screen.getMenu().getNetwork();
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
			if(slot instanceof NetworkBlockSlot && ((NetworkBlockSlot)slot).member.hasTypeOverride())
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

		private final NetworkItemStackHelper member;

		public NetworkBlockSlot(final IInventory inv, final int slot, final int x, final int y, final NetworkItemStackHelper member) {
			super(inv, slot, x, y);
			this.member = member;
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
			return this.member.getType();
		}

		public void cycleType() {
			if(this.member == null)
				return;
			this.member.setType(this.getType().cycle());
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
