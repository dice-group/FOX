package org.aksw.fox.output;

import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IFoxJena {

  /**
   * Resets the underlying model.
   */
  void reset();

  /**
   *
   * @param input
   * @param name
   */
  void addInput(String input, String name);

  /**
   *
   * @param entities
   * @param start
   * @param end
   * @param tool
   * @param version
   */
  void addEntities(List<Entity> entities, String start, String end, String tool, String version);

  /**
   *
   * @param relations
   * @param start
   * @param end
   * @param tool
   * @param version
   */
  void addRelations(Set<Relation> relations, String start, String end, String tool, String version);

  /**
   *
   * @param lang
   */
  void setLang(final String lang);

  /**
   *
   * @return
   */
  String print();
}
