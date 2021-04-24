package me.superckl.upm.screen;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import me.superckl.upm.UPM;
import me.superckl.upm.network.MemberType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

public class NetworkMode extends UPMScreenMode{

	public static final ResourceLocation BACKGROUND = new ResourceLocation(UPM.MOD_ID, "textures/gui/network.png");
	public static final int WIDTH = 195;
	public static final int HEIGHT = 183;

	@Override
	public void renderBackground(final MatrixStack stack, final int mouseX, final int mouseY) {
		this.screen.getMinecraft().getTextureManager().bind(NetworkMode.BACKGROUND);
		this.screen.blit(stack, this.screen.getGuiLeft(), this.screen.getGuiTop(), 0, 0, this.getWidth(), this.getHeight());
	}

	@Override
	public void initSlots(final UPMClientSideContainer container) {
		final Inventory inv = new Inventory(9*3);
		final List<ItemStack> network = container.getNetwork().asItems();
		for(int i = 0; i < inv.getContainerSize(); i++) {
			if(i >= network.size())
				break;
			inv.setItem(i, network.get(i));
		}
		final int startX = 9;
		final int startY = 123;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 3; j++)
				container.addSlot(new NetworkBlockSlot(inv, j*9+i, startX+i*18, startY+j*18));
	}

	@Override
	public void slotClicked(final Slot slot, final int mouseX, final int mouseY, final ClickType type) {
	}

	@Override
	public List<ITextProperties> modifyTooltip(final Slot slot, final List<? extends ITextProperties> tooltip) {
		final List<ITextProperties> props = Lists.newArrayList(tooltip);
		props.add(new StringTextComponent(MemberType.CABLE.name()));
		return props;
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

		public NetworkBlockSlot(final IInventory inv, final int slot, final int x, final int y) {
			super(inv, slot, x, y);
		}

		@Override
		public boolean mayPickup(final PlayerEntity p_82869_1_) {
			return false;
		}

		@Override
		public boolean mayPlace(final ItemStack p_75214_1_) {
			return false;
		}

	}

}
