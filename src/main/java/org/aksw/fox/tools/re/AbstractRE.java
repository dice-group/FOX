package org.aksw.fox.tools.re;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;

public abstract class AbstractRE implements IRE {

  protected Set<Relation> relations = null;
  protected CountDownLatch cdl = null;
  protected String input = null;
  protected List<Entity> entities = null;

  @Override
  public void run() {
    extract(input, entities);
    if (cdl != null) {
      cdl.countDown();
    }
  }

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    this.cdl = cdl;
  }

  @Override
  public void setInput(final String input, final List<Entity> entities) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public Set<Relation> getResults() {
    return relations;
  }
}
