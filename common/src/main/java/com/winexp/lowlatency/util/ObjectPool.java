package com.winexp.lowlatency.util;

import com.winexp.lowlatency.LowLatencyMod;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ObjectPool<T> implements Closeable {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    private final Factory<T> factory;
    private final Resetter<T> resetter;

    public ObjectPool(Factory<T> factory, Resetter<T> resetter) {
        this.factory = factory;
        this.resetter = resetter;
    }

    public T borrowObject() {
        T obj = pool.poll();
        if (obj == null) {
            obj = factory.create();
        }
        return obj;
    }

    public void returnObject(T obj) {
        if (obj != null) {
            resetter.reset(obj);
            pool.offer(obj);
        }
    }

    @Override
    public void close() {
        if (pool.peek() instanceof Closeable) {
            while (!pool.isEmpty()) {
                T obj = pool.poll();
                try {
                    ((Closeable) obj).close();
                } catch (IOException e) {
                    LowLatencyMod.LOGGER.error("Error while closing object", e);
                }
            }
        }
    }

    public interface Factory<T> {
        T create();
    }

    public interface Resetter<T> {
        void reset(T obj);
    }
}
