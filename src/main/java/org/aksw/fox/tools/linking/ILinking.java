package org.aksw.fox.tools.linking;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;

public interface ILinking extends Runnable {
  /**
   * Sets a CountDownLatch object.
   *
   * @param cdl
   */
  void setCountDownLatch(CountDownLatch cdl);

  /**
   * Sets the input.
   *
   * @param input
   */
  void setInput(List<Entity> entities, String input);

  /**
   * Returns results.
   *
   * @return results
   */
  List<Entity> getResults();

  void setUris(List<Entity> entities, String input);
}
