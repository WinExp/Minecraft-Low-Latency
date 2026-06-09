package com.winexp.lowlatency;

import java.io.Closeable;

import static org.lwjgl.opengl.GL33C.*;

public class GpuTimer implements Closeable {
    private final int elapsedQuery = glGenQueries();
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
        glBeginQuery(GL_TIME_ELAPSED, elapsedQuery);
        state = State.BEGIN_QUERIED;
    }

    public void recordEnd() {
        if (state != State.BEGIN_QUERIED) throw new IllegalStateException();
        glEndQuery(GL_TIME_ELAPSED);
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
        if (glGetQueryObjecti64(elapsedQuery, GL_QUERY_RESULT_AVAILABLE) == GL_TRUE
                && glGetQueryObjecti64(endQuery, GL_QUERY_RESULT_AVAILABLE) == GL_TRUE) {
            long elapsedTime = glGetQueryObjecti64(elapsedQuery, GL_QUERY_RESULT);
            endTime = glGetQueryObjecti64(endQuery, GL_QUERY_RESULT);
            beginTime = endTime - elapsedTime;
            state = State.RESULT_AVAILABLE;
        }
    }

    public void reset() {
        state = State.IDLE;
    }

    @Override
    public void close() {
        glDeleteQueries(elapsedQuery);
        glDeleteQueries(endQuery);
    }

    public enum State {
        IDLE, BEGIN_QUERIED, END_QUERIED, RESULT_AVAILABLE
    }
}
