package me.superckl.upm.screen;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.upm.UPM;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@RequiredArgsConstructor
public enum UPMScreenModeType {

	NO_NETWORK(NoNetworkMode::new),
	NETWORK(NetworkMode::new),
	REDSTONE(RedstoneMode::new);

	private final Supplier<UPMScreenMode> modeFactory;
	private ITextComponent tabHover;

	public UPMScreenMode buildMode() {
		return this.modeFactory.get();
	}

	public ITextComponent getTabHover() {
		if(this.tabHover == null)
			this.tabHover = new TranslationTextComponent(Util.makeDescriptionId("gui",
					new ResourceLocation(UPM.MOD_ID, "tab."+this.name().toLowerCase())));
		return this.tabHover;
	}

}
