package org.aksw.fox.tools.linking;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractLinking implements ILinking {
  public static final Logger LOG = LogManager.getLogger(AbstractLinking.class);
  protected Set<Entity> entities = null;
  protected CountDownLatch cdl = null;
  protected String input = null;

  @Override
  public void run() {
    setUris(entities, input);
    if (cdl != null) {
      cdl.countDown();
    }
  }

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    this.cdl = cdl;

  }

  @Override
  public void setInput(final Set<Entity> entities, final String input) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public Set<Entity> getResults() {
    return entities;
  }
}
