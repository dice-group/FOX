package org.aksw.fox.data;

import java.net.URI;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Relation {
    public static Logger      LOG               = LogManager.getLogger(Relation.class);
    public static final float DEFAULT_RELEVANCE = -1;

    protected Entity          subjectEntity     = null;
    protected String          relationLabel     = null;
    protected String          relationByTool    = null;

    protected Entity          objectEntity      = null;
    protected List<URI>       relation          = null;
    protected String          tool              = null;
    protected float           relevance         = DEFAULT_RELEVANCE;

    public Relation(Entity subjectEntity, String relationLabel, String relationByTool, Entity objectEntity, List<URI> relation, String tool, float relevance) {
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

    public String getTool() {
        return tool;
    }

    public float getRelevance() {
        return relevance;
    }

    @Override
    public String toString() {
        return "Relation [subjectEntity=" + subjectEntity + ","
                + " relationLabel=" + relationLabel + ", objectEntity=" + objectEntity + ","
                + " relation=" + relation + ", tool=" + tool + ", relevance=" + relevance + "]";
    }
}
