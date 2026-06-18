package com.winexp.lowlatency;

import static org.lwjgl.opengl.GL33C.*;

public class GpuTimer implements AutoCloseable {
    private final int beginQuery = glGenQueries();
    private final int endQuery = glGenQueries();
    private State state = State.IDLE;
    private long beginTime;
    private long endTime;
    private long submitTime;

    public static long getTimestampNow() {
        long[] t = new long[1];
        glGetInteger64v(GL_TIMESTAMP, t);
        return t[0];
    }

    public State getState() {
        return state;
    }

    public void recordBegin() {
        if (state != State.IDLE) throw new IllegalStateException();
        glQueryCounter(beginQuery, GL_TIMESTAMP);
        state = State.BEGIN_QUERIED;
    }

    public void recordEnd() {
        if (state != State.BEGIN_QUERIED) throw new IllegalStateException();
        glQueryCounter(endQuery, GL_TIMESTAMP);
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
        if (glGetQueryObjecti64(beginQuery, GL_QUERY_RESULT_AVAILABLE) == GL_TRUE
                && glGetQueryObjecti64(endQuery, GL_QUERY_RESULT_AVAILABLE) == GL_TRUE) {
            beginTime = glGetQueryObjecti64(beginQuery, GL_QUERY_RESULT);
            endTime = glGetQueryObjecti64(endQuery, GL_QUERY_RESULT);
            state = State.RESULT_AVAILABLE;
        }
    }

    public void reset() {
        state = State.IDLE;
    }

    @Override
    public void close() {
        glDeleteQueries(beginQuery);
        glDeleteQueries(endQuery);
    }

    public enum State {
        IDLE, BEGIN_QUERIED, END_QUERIED, RESULT_AVAILABLE
    }
}
