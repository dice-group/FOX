package org.aksw.fox.output;

import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IFoxJena {

  public void reset();

  public void addInput(String input, String name);

  public void addEntities(Set<Entity> entities, String start, String end);

  public void addRelations(Set<Relation> relations, String start, String end);

  // TODO: update to : output lang
  public void setLang(final String lang);

  public String print();
}
