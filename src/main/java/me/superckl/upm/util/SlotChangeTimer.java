package me.superckl.upm.util;

public class SlotChangeTimer {

	private final int numChanges;
	private final int msPerCycle;
	private final long startTime;

	public SlotChangeTimer(final int numChanges, final int msPerCycle) {
		this.numChanges = numChanges;
		this.msPerCycle = msPerCycle;
		this.startTime = System.currentTimeMillis();
	}

	public int getValue() {
		return (int) Math.floorDiv(System.currentTimeMillis() - this.startTime, this.msPerCycle) % this.numChanges;
	}

}
