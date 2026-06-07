package com.winexp.lowlatency;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL33C;

import java.io.Closeable;

public class GpuTimer implements Closeable {
    private final int elapsedQuery = GL15C.glGenQueries();
    private final int endQuery = GL15C.glGenQueries();
    private State state = State.IDLE;
    private long beginTime;
    private long endTime;
    private long submitTime;

    public State getState() {
        return state;
    }

    public long getTimestampNow() {
        long[] t = new long[1];
        GL33C.glGetInteger64v(GL33C.GL_TIMESTAMP, t);
        return t[0];
    }

    public void recordBegin() {
        if (state != State.IDLE) throw new IllegalStateException();
        GL15C.glBeginQuery(GL33C.GL_TIME_ELAPSED, elapsedQuery);
        state = State.BEGIN_QUERIED;
    }

    public void recordEnd() {
        if (state != State.BEGIN_QUERIED) throw new IllegalStateException();
        GL15C.glEndQuery(GL33C.GL_TIME_ELAPSED);
        GL33C.glQueryCounter(endQuery, GL33C.GL_TIMESTAMP);
        submitTime = getTimestampNow();
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
        if (GL33C.glGetQueryObjecti64(elapsedQuery, GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE
                && GL33C.glGetQueryObjecti64(endQuery, GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE) {
            long elapsedTime = GL33C.glGetQueryObjecti64(elapsedQuery, GL15C.GL_QUERY_RESULT);
            endTime = GL33C.glGetQueryObjecti64(endQuery, GL15C.GL_QUERY_RESULT);
            beginTime = endTime - elapsedTime;
            state = State.RESULT_AVAILABLE;
        }
    }

    public void reset() {
        state = State.IDLE;
    }

    @Override
    public void close() {
        GL15C.glDeleteQueries(elapsedQuery);
        GL15C.glDeleteQueries(endQuery);
    }

    public enum State {
        IDLE, BEGIN_QUERIED, END_QUERIED, RESULT_AVAILABLE
    }
}
