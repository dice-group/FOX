package org.aksw.fox.webservice.util;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.LogManager;
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

  protected static Logger LOG = LogManager.getLogger(Pool.class);

  /**
   * Holds objects.
   */
  protected final Queue<T> queue = new LinkedList<>();

  /**
   * Max. count of objects in queue.
   */
  protected int max = 0;

  /**
   * 
   */
  protected String className = "";
  protected String lang = "";

  public String getLang() {
    return lang;
  }

  /**
   * 
   * @param className
   * @param count
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   */
  public Pool(final String className, final String lang, final int count)
      throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {
    this.className = className;
    this.lang = lang;

    if (count > 0) {
      max = count;
      while (queue.size() < max) {
        LOG.info("Creates an instance " + (queue.size() + 1) + "/" + max + "...");
        queue.add(getInstance());
      }
    }
  }

  /**
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * 
   */
  public synchronized void add() {
    push(getInstance());
  }

  /**
   * 
   * @param t
   */
  public synchronized void push(final T t) {
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
      } catch (final InterruptedException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return t;
  }

  /**
   * Creates a new instance of the given className.
   * 
   * @param className
   * @return new instance
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  protected synchronized T getInstance() {
    try {
      return (T) Class.forName(className).getConstructor(String.class).newInstance(lang);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return null;
  }
}
