package org.aksw.fox.tools.linking;

import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ATool;

public abstract class AbstractLinking extends ATool implements ILinking {

  protected List<Entity> entities = null;
  protected String input = null;

  @Override
  public void run() {
    setUris(entities, input);
    if (cdl != null) {
      cdl.countDown();
    }
  }

  @Override
  public void setInput(final List<Entity> entities, final String input) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public List<Entity> getResults() {
    return entities;
  }
}
