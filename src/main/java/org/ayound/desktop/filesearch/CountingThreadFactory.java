package org.ayound.desktop.filesearch;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CountingThreadFactory implements ThreadFactory {
	protected final AtomicInteger counter = new AtomicInteger();

	public int getCount() {
		return this.counter.get();
	}
}
