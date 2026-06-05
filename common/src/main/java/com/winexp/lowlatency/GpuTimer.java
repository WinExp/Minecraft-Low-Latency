package com.winexp.lowlatency;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL33C;

import java.io.Closeable;

public class GpuTimer implements Closeable {
    private final int beginQuery = GL15C.glGenQueries(),
            endQuery = GL15C.glGenQueries();
    private State state = State.IDLE;
    private long result;

    public State getState() {
        return state;
    }

    public void recordBegin() {
        if (state != State.IDLE) throw new IllegalStateException();
        GL33C.glQueryCounter(beginQuery, GL33C.GL_TIMESTAMP);
        state = State.BEGIN_QUERIED;
    }

    public void recordEnd() {
        if (state != State.BEGIN_QUERIED) throw new IllegalStateException();
        GL33C.glQueryCounter(endQuery, GL33C.GL_TIMESTAMP);
        state = State.END_QUERIED;
    }

    public long getTimeElapsedNs() {
        if (state != State.RESULT_AVAILABLE) return 0;
        return result;
    }

    public void updateResult() {
        if (state != State.END_QUERIED) return;
        if (GL33C.glGetQueryObjecti64(beginQuery, GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE
                && GL33C.glGetQueryObjecti64(endQuery, GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE) {
            long beginTime = GL33C.glGetQueryObjecti64(beginQuery, GL15C.GL_QUERY_RESULT);
            long endTime = GL33C.glGetQueryObjecti64(endQuery, GL15C.GL_QUERY_RESULT);
            result = endTime - beginTime;
            state = State.RESULT_AVAILABLE;
        }
    }

    public void reset() {
        state = State.IDLE;
        result = 0;
    }

    @Override
    public void close() {
        GL15C.glDeleteQueries(beginQuery);
        GL15C.glDeleteQueries(endQuery);
    }

    public enum State {
        IDLE, BEGIN_QUERIED, END_QUERIED, RESULT_AVAILABLE
    }
}
