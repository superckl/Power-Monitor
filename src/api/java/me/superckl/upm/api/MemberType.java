package me.superckl.upm.api;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public enum MemberType {

	STORAGE(new ResourceLocation(UPMAPI.MOD_ID, "storage"), TextFormatting.BLUE, true),
	MACHINE(new ResourceLocation(UPMAPI.MOD_ID, "machine"), TextFormatting.RED, false),
	GENERATOR(new ResourceLocation(UPMAPI.MOD_ID, "generator"), TextFormatting.GREEN, true),
	CABLE(new ResourceLocation(UPMAPI.MOD_ID, "cable"), TextFormatting.YELLOW, true),
	UNKNOWN(null, TextFormatting.GRAY, true);

	private final ResourceLocation tag;
	private final TextFormatting color;
	private final boolean connects;

	MemberType(final ResourceLocation tag, final TextFormatting color, final boolean connects) {
		this.tag = tag;
		this.color = color;
		this.connects = connects;
	}

	public MemberType cycle() {
		switch(this) {
		case STORAGE:
			return MemberType.CABLE;
		case CABLE:
			return MemberType.MACHINE;
		case MACHINE:
			return MemberType.GENERATOR;
		case GENERATOR:
		case UNKNOWN:
		default:
			return MemberType.STORAGE;
		}
	}

	public ResourceLocation tag() {
		return this.tag;
	}
	public TextFormatting color() {
		return this.color;
	}

	public boolean connects() {
		return this.connects;
	}

}
