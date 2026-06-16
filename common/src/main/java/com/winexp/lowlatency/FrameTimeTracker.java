package com.winexp.lowlatency;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;

import java.util.ArrayDeque;

public class FrameTimeTracker {
    private final ArrayDeque<Long> queue = new ArrayDeque<>();
    private final int window;

    public FrameTimeTracker(int window) {
        if (window <= 0) throw new IllegalArgumentException();
        this.window = window;
    }

    public long getAverageTime() {
        if (queue.isEmpty()) return 0;
        int size = queue.size();
        LongArrayList list = new LongArrayList(size);
        Iterators.addAll(list, queue.iterator());
        list.sort(LongComparators.NATURAL_COMPARATOR);
        if (size % 2 == 0) {
            return (list.getLong((size - 1) / 2) + list.getLong((size - 1) / 2 + 1)) / 2;
        } else {
            return list.getLong((size - 1) / 2);
        }
    }

    public void addFrame(long frameTime) {
        queue.offer(frameTime);
        if (queue.size() > window) {
            queue.poll();
        }
    }
}
