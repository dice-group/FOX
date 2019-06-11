package org.aksw.fox.data;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Entity implements IData, Comparable<Entity> {

  public static final float DEFAULT_RELEVANCE = -1;

  protected String text;

  protected String type;

  protected String uri = "";

  protected float relevance;

  protected String tool;

  protected int index = -1;

  /**
   *
   * Copy constructor.
   *
   * @param entity
   */
  public Entity(final Entity entity) {
    this(entity.text, entity.type, entity.relevance, entity.tool, entity.index);

    uri = entity.uri;
  }

  /**
   * Constructor.
   *
   * @param text
   * @param type
   * @param relevance
   * @param tool
   * @param index
   */
  public Entity(final String text, final String type, final float relevance, final String tool,
      final int index) {

    this.text = text;
    this.type = type;
    this.relevance = relevance;
    this.tool = tool;
    this.index = index;
  }

  /**
   * Constructor.
   *
   * @param text
   * @param type
   * @param relevance
   * @param tool
   */
  public Entity(final String text, final String type, final float relevance, final String tool) {
    this(text, type, relevance, tool, -1);
  }

  /**
   * Constructor.
   *
   * @param text
   * @param type
   * @param tool
   */
  public Entity(final String text, final String type, final String tool) {
    this(text, type, DEFAULT_RELEVANCE, tool, -1);
  }

  /**
   * Adds a space and the parameter text to the entities text;
   *
   * @param text
   */
  public void addText(final String text) {
    this.text += " " + text;
  }

  /**
   * Compares index
   */
  @Override
  public int compareTo(final Entity o) {
    return index - o.index;
  }

  /**
   * Takes entity text and type into account.
   */
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

  public int getBeginIndex() {
    return index;
  }

  public int getEndIndex() {
    return index > -1 ? index + getText().length() : -1;
  }

  public float getRelevance() {
    return relevance;
  }

  public String getText() {
    return text;
  }

  public String getTool() {
    return tool;
  }

  @Override
  public String getToolName() {
    return tool;
  }

  public String getType() {
    return type;
  }

  public String getUri() {
    return uri;
  }

  /**
   * Takes entity text and type into account.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (text == null ? 0 : text.hashCode());
    result = prime * result + (type == null ? 0 : type.hashCode());
    return result;
  }

  public void setRelevance(final float relevance) {
    this.relevance = relevance;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void setTool(final String tool) {
    this.tool = tool;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setUri(final String uri) {
    this.uri = uri;
  }

  @Override
  public String toString() {
    return "Entity [text=" + text + ", type=" + type + ", uri=" + uri + ", tool=" + tool
        + ", relevance=" + relevance + ", index=" + index + "]";
  }
}
