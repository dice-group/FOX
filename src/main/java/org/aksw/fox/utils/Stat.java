package org.aksw.fox.utils;

public class Stat {

    // timer
    public long start = 0l;

    // timer
    public long stop = 0l;

    // sentences count
    public int sentences = 0;

    // token count
    public int token = 0;

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }
}
