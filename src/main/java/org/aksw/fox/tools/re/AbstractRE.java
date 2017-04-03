package org.aksw.fox.tools.re;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.apache.log4j.Logger;

public abstract class AbstractRE implements IRE {

  public static Logger LOG = Logger.getLogger(AbstractRE.class);

  protected Set<Relation> relations = new HashSet<>();
  protected CountDownLatch cdl = null;
  protected String input = null;
  protected Set<Entity> entities = null;

  @Override
  public void run() {
    relations.clear();
    extract();
    if (cdl != null) {
      cdl.countDown();
    }
  }

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    this.cdl = cdl;
  }

  @Override
  public void setInput(final String input, final Set<Entity> entities) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public Set<Relation> getResults() {
    return relations;
  }

  @Override
  public String getToolName() {
    return this.getClass().getSimpleName();
  }
}
