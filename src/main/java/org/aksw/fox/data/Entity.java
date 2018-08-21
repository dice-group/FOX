package org.aksw.fox.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Entity implements IData {

  public static final float DEFAULT_RELEVANCE = -1;

  public int id = -1;

  protected String text = "";

  protected String type = "";

  protected String uri = "";

  protected float relevance = DEFAULT_RELEVANCE;

  protected String tool = "";

  /**
   * Start indices.
   */
  protected Set<Integer> indicies = null;

  /**
   *
   * Constructor.
   *
   * @param text
   * @param type
   */
  public Entity(final String text, final String type) {
    this(text, type, DEFAULT_RELEVANCE, "");
  }

  /**
   *
   * Copy constructor.
   *
   * @param entity
   */
  public Entity(final Entity entity) {
    this(entity.text, entity.type);
    id = entity.id;
    uri = entity.uri;
    tool = entity.tool;
    indicies = new HashSet<>();
    indicies.addAll(entity.getIndices());
    relevance = entity.relevance;
  }

  /**
   *
   * Constructor.
   *
   * @param text
   * @param type
   * @param relevance
   */
  public Entity(final String text, final String type, final float relevance) {
    this(text, type, relevance, "");
  }

  /**
   *
   * Constructor.
   *
   * @param text
   * @param type
   * @param relevance
   * @param tool
   */
  public Entity(final String text, final String type, final float relevance, final String tool) {
    this.text = text;
    this.type = type;
    this.relevance = relevance;
    this.tool = tool;
  }

  public void addText(final String text) {
    this.text += " " + text;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Entity other = (Entity) obj;
    if (text == null) {
      if (other.text != null) {
        return false;
      }
    } else if (!text.equals(other.text)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }

  /**
   * Gets all start indices.
   */
  public Set<Integer> getIndices() {
    return indicies;
  }

  /**
   * Adds start indices
   *
   * @param index
   * @return self
   */
  public Entity addIndicies(final int index) {
    if (indicies == null) {
      indicies = new TreeSet<>();
    }
    indicies.add(index);
    return this;
  }

  /**
   * Adds start indices.
   *
   * @param indices
   * @return self
   */
  public Entity addAllIndicies(final Set<Integer> indices) {
    if (indicies == null) {
      indicies = new HashSet<>();
    }
    indicies.addAll(indices);
    return this;
  }

  @Override
  public String getToolName() {
    return tool;
  }

  public String getText() {
    return text;
  }

  public String getType() {
    return type;
  }

  public float getRelevance() {
    return relevance;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(final String uri) {
    this.uri = uri;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((text == null) ? 0 : text.hashCode());
    result = (prime * result) + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  public void setText(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "Entity [text=" + text + ", type=" + type + ", uri=" + uri + ", tool=" + tool
        + ", relevance=" + relevance + (indicies != null ? ", indicies=" + indicies : "") + "]";
  }

  /**
   * The entity needs a single index pair.
   *
   * @param e
   * @return
   */
  public static int getIndex(final Entity e) {
    int index = -1;
    if (e.getIndices().size() > 1) {
      throw new UnsupportedOperationException("Break down entitiy indices to a single index pair.");
    } else if (e.getIndices().size() > 0) {
      index = e.getIndices().iterator().next();
    }
    return index;

  }

  /**
   * Each entity with just one index and sorted.
   *
   * @param entities
   * @return sorted entities with one index in the index set
   */
  public static List<Entity> breakdownAndSortEntity(final Set<Entity> entities) {

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

  public static Map<Integer, Entity> indexToEntity(final List<Entity> entities) {
    final Map<Integer, Entity> index = new HashMap<>();
    final Iterator<Entity> iter = entities.iterator();
    while (iter.hasNext()) {
      final Entity e = iter.next();
      final int i = getIndex(e);
      if (i > -1) {
        index.put(i, e);
      }
    }
    return index;
  }
}
