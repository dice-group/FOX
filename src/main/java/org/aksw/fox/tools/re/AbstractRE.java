package org.aksw.fox.tools.re;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

  /**
   * Each entity with just one index and sorted.
   *
   * @param entities
   * @return sorted entities with one index in the index set
   */
  public List<Entity> breakdownAndSortEntity(final Set<Entity> entities) {

    final Map<Integer, Entity> sorted = new HashMap<>();

    for (final Entity entity : entities) {
      if (entity.getIndices().size() > 1) {
        final Iterator<Integer> iter = entity.getIndices().iterator();
        while (iter.hasNext()) {
          final Entity e = new Entity(//
              entity.getText(), entity.getType(), entity.getRelevance(), entity.getToolName()//
          );

          final int index = iter.next();
          e.addIndicies(index);
          sorted.put(index, e);
        }
      } else {
        sorted.put(entity.getIndices().iterator().next(), entity);
      }
    }

    return sorted.keySet()//
        .stream().sorted().collect(Collectors.toList())//
        .stream().map(sorted::get).collect(Collectors.toList());
  }
}
