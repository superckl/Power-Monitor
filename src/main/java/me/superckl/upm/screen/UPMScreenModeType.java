package me.superckl.upm.screen;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UPMScreenModeType {

	NO_NETWORK(NoNetworkMode::new),
	NETWORK(NetworkMode::new);

	private final Supplier<UPMScreenMode> modeFactory;

	public UPMScreenMode buildMode() {
		return this.modeFactory.get();
	}

}
