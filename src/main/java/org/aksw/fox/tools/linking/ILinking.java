package org.aksw.fox.tools.linking;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;

public interface ILinking extends Runnable {
  /**
   * Sets a CountDownLatch object.
   *
   * @param cdl
   */
  public void setCountDownLatch(CountDownLatch cdl);

  /**
   * Sets the input.
   *
   * @param input
   */
  public void setInput(Set<Entity> entities, String input);

  /**
   * Returns results.
   *
   * @return results
   */
  public Set<Entity> getResults();

  public void setUris(Set<Entity> entities, String input);
}
