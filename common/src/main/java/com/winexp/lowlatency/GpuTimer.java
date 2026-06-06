package com.winexp.lowlatency;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL33C;

import java.io.Closeable;

public class GpuTimer implements Closeable {
    private final int beginQuery = GL15C.glGenQueries(),
            endQuery = GL15C.glGenQueries();
    private State state = State.IDLE;
    private long beginTime;
    private long endTime;
    private long submitTime;

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
        long[] t = new long[1];
        GL33C.glGetInteger64v(GL33C.GL_TIMESTAMP, t);
        submitTime = t[0];
        state = State.END_QUERIED;
    }

    public long getTimeElapsed() {
        if (state != State.RESULT_AVAILABLE) return 0;
        return endTime - beginTime;
    }

    public long getCompletionDelay() {
        if (state != State.RESULT_AVAILABLE) return 0;
        return endTime - submitTime;
    }

    public void pollResult() {
        if (state != State.END_QUERIED) return;
        if (GL33C.glGetQueryObjecti64(beginQuery, GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE
                && GL33C.glGetQueryObjecti64(endQuery, GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE) {
            beginTime = GL33C.glGetQueryObjecti64(beginQuery, GL15C.GL_QUERY_RESULT);
            endTime = GL33C.glGetQueryObjecti64(endQuery, GL15C.GL_QUERY_RESULT);
            state = State.RESULT_AVAILABLE;
        }
    }

    public void reset() {
        state = State.IDLE;
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
