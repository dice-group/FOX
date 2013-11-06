package org.aksw.fox.data;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author rspeck
 */
public class Entity {
    public static Logger logger = Logger.getLogger(Entity.class);

    public static final float DEFAULT_RELEVANCE = -1;

    protected String text = "";

    protected String type = "";

    public String uri = null;

    protected float relevance = DEFAULT_RELEVANCE;

    protected String tool = "";

    protected Set<Integer> indicies = null;

    public Set<Integer> getIndices() {
        return indicies;
    }

    public void addIndicies(int index) {
        if (indicies == null)
            indicies = new HashSet<>();

        indicies.add(index);
    }

    public void addAllIndicies(Set<Integer> indices) {
        if (indicies == null)
            indicies = new HashSet<>();

        indicies.addAll(indices);
    }

    // public Entity() { /**/ }

    public String getTool() {
        return tool;
    }

    public Entity(String text, String type) {
        this(text, type, DEFAULT_RELEVANCE, "");
    }

    public Entity(String text, String type, float relevance) {
        this(text, type, relevance, "");
    }

    public Entity(String text, String type, float relevance, String tool) {
        this.text = text;
        this.type = type;
        this.relevance = relevance;
        this.tool = tool;
    }

    public void addText(String text) {
        this.text += " " + text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entity other = (Entity) obj;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Entity [text=" + text + ", type=" + type + ", tool=" + tool + ", relevance=" + relevance + (indicies != null ? ", indicies=" + indicies : "") + "]";
    }
}
