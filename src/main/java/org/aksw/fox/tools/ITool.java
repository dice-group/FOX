package org.aksw.fox.tools;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface ITool extends Runnable {

  Logger LOG = LogManager.getLogger(ITool.class);

  /**
   * Returns the tools name.
   *
   * @return name
   */
  String getToolName();

  String getToolVersion();

  /**
   * Sets a CountDownLatch object.
   *
   * @param cdl
   */
  void setCountDownLatch(CountDownLatch cdl);
}
