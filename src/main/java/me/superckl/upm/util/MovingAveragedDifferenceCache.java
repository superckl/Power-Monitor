package me.superckl.upm.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovingAveragedDifferenceCache {

	private final int numPoints;
	private final LongArrayList cache;

	public MovingAveragedDifferenceCache(final int numPoints) {
		this.numPoints = numPoints;
		this.cache = new LongArrayList(numPoints+1);
	}

	public void cache(final long value) {
		this.cache.add(0, value);
		this.checkLength();
	}

	public long get() {
		final int size = this.cache.size();
		if(size <= 1)
			return 0;
		final LongIterator it = this.cache.iterator();
		final double numAvgs = size-1;
		long prev = it.nextLong();
		double avg = 0;
		while(it.hasNext()) {
			final long val = it.nextLong();
			avg += (prev - val)/numAvgs;
			prev = val;
		}
		return Math.round(avg);
	}

	private void checkLength() {
		while(this.cache.size() > this.numPoints)
			this.cache.removeLong(this.numPoints);
	}

}
