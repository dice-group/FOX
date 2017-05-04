package org.aksw.fox.data;

import java.net.URI;
import java.util.List;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Relation implements IData {

  public static final float DEFAULT_RELEVANCE = -1;

  protected Entity subjectEntity = null;
  protected String relationLabel = null;
  protected String relationByTool = null;

  protected Entity objectEntity = null;
  protected List<URI> relation = null;
  protected String tool = null;
  protected float relevance = DEFAULT_RELEVANCE;

  /***
   *
   * Constructor.
   *
   * @param subjectEntity
   * @param relationLabel
   * @param relationByTool
   * @param objectEntity
   * @param relation
   * @param tool
   * @param relevance
   */
  public Relation(final Entity subjectEntity, final String relationLabel,
      final String relationByTool, final Entity objectEntity, final List<URI> relation,
      final String tool, final float relevance) {

    this.subjectEntity = subjectEntity;
    this.relationLabel = relationLabel;
    this.relationByTool = relationByTool;
    this.objectEntity = objectEntity;
    this.relation = relation;
    this.tool = tool;
    this.relevance = relevance;
  }

  public Entity getSubjectEntity() {
    return subjectEntity;
  }

  public String getRelationLabel() {
    return relationLabel;
  }

  public Entity getObjectEntity() {
    return objectEntity;
  }

  public List<URI> getRelation() {
    return relation;
  }

  public String getRelationByTool() {
    return relationByTool;
  }

  @Override
  public String getToolName() {
    return tool;
  }

  public float getRelevance() {
    return relevance;
  }

  @Override
  public String toString() {
    return "Relation [subjectEntity=" + subjectEntity + "," + " relationLabel=" + relationLabel
        + ", objectEntity=" + objectEntity + "," + " relation=" + relation + ", tool=" + tool
        + ", relevance=" + relevance + "]";
  }
}
