package org.aksw.fox.web;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * 
 * Pool holds objects in a queue.
 * 
 * @author rspeck
 * @param <T>
 * 
 */
public class Pool<T> {

    protected static Logger  LOG       = Logger.getLogger(Pool.class);

    /**
     * Holds objects.
     */
    protected final Queue<T> queue     = new LinkedList<>();

    /**
     * Max. count of objects in queue.
     */
    protected int            max       = 0;

    /**
     * 
     */
    protected String         className = "";

    /**
     * 
     * @param className
     * @param count
     */
    public Pool(String className, int count) {
        this.className = className;
        if (count > 0) {
            max = count;
            while (queue.size() < max) {
                LOG.info("Creates an instance " + (queue.size() + 1) + "/" + max + "...");
                queue.add(getInstance(className));
            }
        }
    }

    /**
     * 
     */
    public void add() {
        push(getInstance());
    }

    /**
     * 
     * @param t
     */
    public void push(T t) {
        if (t != null) {
            if (queue.size() < max) {
                queue.add(t);
            } else {
                LOG.warn("pool queue is full.");
            }
        } else {
            LOG.warn("We don't push null to pool queue!!");
        }
    }

    /**
     * 
     * @return
     */
    public T poll() {
        T t = null;
        while ((t = queue.poll()) == null) {
            try {
                LOG.warn("pool queue empty, sleep 20s ...");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                LOG.error("\n", e);
            }
        }
        return t;
    }

    /**
     * 
     * @return
     */
    protected T getInstance() {
        return getInstance(className);
    }

    /**
     * Creates a new instance of the given className.
     * 
     * @param className
     * @return new instance
     */
    @SuppressWarnings("unchecked")
    protected T getInstance(String className) {
        try {
            return (T) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOG.error("\n", e);
        }
        return null;
    }
}
