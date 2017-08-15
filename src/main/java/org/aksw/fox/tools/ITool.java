package org.aksw.fox.tools;

import java.util.concurrent.CountDownLatch;

public interface ITool extends Runnable {

  /**
   * Returns the tools name.
   *
   * @return name
   */
  public String getToolName();

  public String getToolVersion();

  /**
   * Sets a CountDownLatch object.
   *
   * @param cdl
   */
  public void setCountDownLatch(CountDownLatch cdl);
}
