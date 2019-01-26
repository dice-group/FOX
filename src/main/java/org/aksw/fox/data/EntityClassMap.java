package org.aksw.fox.data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class contains constants to map the entity type/class for each NER tool. The types/classes
 * are used in FOX are {@link #L}, {@link #O}, {@link #P}, {@link #M} and {@link #N}.
 *
 * @author rspeck
 *
 */
public class EntityClassMap {

  protected static final Map<String, String> entityClassesNEEL = new HashMap<>();
  static {
    entityClassesNEEL.put("Organization", EntityTypes.O);
    entityClassesNEEL.put("Location", EntityTypes.L);
    entityClassesNEEL.put("Person", EntityTypes.P);
  }

  protected static final Map<String, String> entityClassesILLINOIS = new HashMap<>();
  static {
    entityClassesILLINOIS.put("LOC", EntityTypes.L);
    entityClassesILLINOIS.put("ORG", EntityTypes.O);
    entityClassesILLINOIS.put("PER", EntityTypes.P);
  }

  /**
   * Gets the entity class for a NEEL challenge entity type/class.
   */
  public static String neel(final String tag) {
    String t = entityClassesNEEL.get(tag);
    if (t == null) {
      t = BILOUEncoding.O;
    }
    return t;
  }

  /**
   * Gets the entity class for a illinois entity type/class.
   */
  public static String illinois(final String illinoisTag) {
    String t = entityClassesILLINOIS.get(illinoisTag);
    if (t == null) {
      t = BILOUEncoding.O;
    }
    return t;
  }
}
