package org.aksw.fox.tools.re;

import java.util.HashSet;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ATool;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractRE extends ATool implements IRE {

  protected static Logger LOG = LogManager.getLogger(AbstractRE.class);

  protected Set<Relation> relations = new HashSet<>();
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
  public void setInput(final String input, final Set<Entity> entities) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public Set<Relation> getResults() {
    return relations;
  }
}
