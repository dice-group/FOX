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
    protected Entity          objectEntity      = null;
    protected List<URI>       relation          = null;
    protected String          tool              = null;
    protected float           relevance         = DEFAULT_RELEVANCE;

    public Relation(Entity subjectEntity, String relationLabel, Entity objectEntity, List<URI> relation, String tool, float relevance) {
        this.subjectEntity = subjectEntity;
        this.relationLabel = relationLabel;
        this.objectEntity = objectEntity;
        this.relation = relation;
        this.tool = tool;
        this.relevance = relevance;
    }

    @Override
    public String toString() {
        return "Relation [subjectEntity=" + subjectEntity + ", relationLabel=" + relationLabel + ", objectEntity=" + objectEntity + ", relation=" + relation + ", tool=" + tool + ", relevance=" + relevance + "]";
    }
}
