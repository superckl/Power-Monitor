package me.superckl.upm.network.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import me.superckl.upm.UPM;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor
public enum MemberType {

	STORAGE(new ResourceLocation(UPM.MOD_ID, "storage"), TextFormatting.BLUE, true),
	MACHINE(new ResourceLocation(UPM.MOD_ID, "machine"), TextFormatting.RED, false),
	GENERATOR(new ResourceLocation(UPM.MOD_ID, "machine"), TextFormatting.GREEN, true),
	CABLE(new ResourceLocation(UPM.MOD_ID, "cable"), TextFormatting.YELLOW, true),
	UNKNOWN(null, TextFormatting.GRAY, true);

	private final ResourceLocation tag;
	private final TextFormatting color;
	private final boolean connects;

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

}
