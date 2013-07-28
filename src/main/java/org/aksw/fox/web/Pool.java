package org.aksw.fox.web;

import java.util.LinkedList;
import java.util.Queue;

import org.aksw.fox.Fox;
import org.aksw.fox.InterfaceRunnableFox;
import org.apache.log4j.Logger;

/**
 * 
 * @author rspeck
 * 
 */
public class Pool {

    private static Logger logger = Logger.getLogger(Pool.class);

    /**
     * Holds implementations of FoxInterface to use.
     */
    protected final Queue<InterfaceRunnableFox> foxQueue = new LinkedList<InterfaceRunnableFox>();

    /**
     * Max. count of FoxInterface implementations in foxQueue.
     */
    protected int max = 0;

    /**
     * 
     */
    protected String className = "";

    /**
     * 
     * @param count
     */
    public Pool(int count) {
        this(Fox.class.getName(), count);
    }

    /**
     * 
     * @param className
     * @param count
     */
    public Pool(String className, int count) {
        this.className = className;
        if (count > 0) {
            max = count;
            while (foxQueue.size() < max) {
                logger.info("Creates fox instance " + (foxQueue.size() + 1) + "/" + max + "...");
                foxQueue.add(getFox(className));
            }
        }
    }

    /**
     * 
     */
    public void add() {
        push(getFox());
    }

    /**
     * 
     * @param fox
     */
    public void push(InterfaceRunnableFox fox) {
        if (foxQueue.size() < max) {
            foxQueue.add(fox);
        } else {
            logger.error("pool queue is full.");
        }
    }

    /**
     * 
     * @return
     */
    public InterfaceRunnableFox poll() {
        InterfaceRunnableFox fox = null;
        while ((fox = foxQueue.poll()) == null) {
            try {
                logger.debug("pool queue empty, sleep 10s ...");
                Thread.sleep(10000); // 10s
            } catch (InterruptedException e) {
                logger.error("\n", e);
            }
        }
        return fox;
    }

    /**
     * 
     * @return
     */
    public InterfaceRunnableFox getFox() {
        return getFox(className);
    }

    /**
     * Creates a new instance of the given className that implements
     * FoxInterface.
     * 
     * @param className
     * @return new instance
     */
    public InterfaceRunnableFox getFox(String className) {
        InterfaceRunnableFox fox = null;
        try {
            fox = (InterfaceRunnableFox) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            logger.error("\n", e);
        }
        return fox;
    }
}
