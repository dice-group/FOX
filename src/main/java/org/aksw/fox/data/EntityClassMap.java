package org.aksw.fox.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * This class contains constants to map the entity type/class for each NER tool.
 * The types/classes are used in FOX are {@link #L}, {@link #O}, {@link #P},
 * {@link #M} and {@link #N}.
 * 
 * @author rspeck
 * 
 */
public class EntityClassMap {

    public static final String                 L                     = "LOCATION";
    public static final String                 N                     = "NULL";
    public static final String                 O                     = "ORGANIZATION";
    public static final String                 P                     = "PERSON";

    public static final List<String>           entityClasses         = Arrays.asList(L, O, P, N);

    protected static final Map<String, String> entityClassesOracel   = new HashMap<>();
    static {
        entityClassesOracel.put("ORGANIZATION", O);
        entityClassesOracel.put("LOCATION", L);
        entityClassesOracel.put("PERSON", P);
    }

    protected static final Map<String, String> entityClassesNEEL     = new HashMap<>();
    static {
        entityClassesNEEL.put("Organization", O);
        entityClassesNEEL.put("Location", L);
        entityClassesNEEL.put("Person", P);
    }

    protected static final Map<String, String> entityClassesILLINOIS = new HashMap<>();
    static {
        entityClassesILLINOIS.put("LOC", L);
        entityClassesILLINOIS.put("ORG", O);
        entityClassesILLINOIS.put("PER", P);
    }

    /**
     * Gets the entity class for a oracel entity type/class.
     */
    public static String oracel(String tag) {
        String t = entityClassesOracel.get(tag);
        if (t == null)
            t = getNullCategory();
        return t;
    }

    /**
     * Gets the entity class for a NEEL challenge entity type/class.
     */
    public static String neel(String tag) {
        String t = entityClassesNEEL.get(tag);
        if (t == null)
            t = getNullCategory();
        return t;
    }

    /**
     * Gets the entity class for a illinois entity type/class.
     */
    public static String illinois(String illinoisTag) {
        String t = entityClassesILLINOIS.get(illinoisTag);
        if (t == null)
            t = getNullCategory();
        return t;
    }

    /**
     * Gets the null type/class.
     */
    public static String getNullCategory() {
        return N;
    }
}
