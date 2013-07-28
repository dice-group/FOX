package org.aksw.fox.data;

import java.util.ArrayList;
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

    public static final String L = "LOCATION";
    public static final String M = "MISCELLANEOUS";
    public static final String N = "NULL";
    public static final String O = "ORGANIZATION";
    public static final String P = "PERSON";

    public static final List<String> entityClasses = new ArrayList<String>();
    static {
        entityClasses.add(L);
        entityClasses.add(O);
        entityClasses.add(P);
        // entityClasses.add(M);
        entityClasses.add(N);
    }

    protected static final Map<String, String> entityClassesOracel = new HashMap<String, String>();
    static {
        entityClassesOracel.put("ORGANIZATION", O);
        entityClassesOracel.put("LOCATION", L);
        entityClassesOracel.put("PERSON", P);
        // entityClassesBalie.put("misc", M);
        // entityClassesOracel.put("nothing", N);
    }

    protected static final Map<String, String> entityClassesBalie = new HashMap<String, String>();
    static {
        entityClassesBalie.put("ORGANIZATION", O);
        entityClassesBalie.put("LOCATION", L);
        entityClassesBalie.put("PERSON", P);
        // entityClassesBalie.put("misc", M);
        entityClassesBalie.put("nothing", N);
    }

    protected static final Map<String, String> entityClassesSTAN = new HashMap<String, String>();
    static {
        entityClassesSTAN.put("ORGANIZATION", O);
        entityClassesSTAN.put("LOCATION", L);
        entityClassesSTAN.put("PERSON", P);
        // entityClassesSTAN.put("MISC", M);
        entityClassesSTAN.put("O", N);
    }

    protected static final Map<String, String> entityClassesOpenNLP = new HashMap<String, String>();
    static {
        entityClassesOpenNLP.put("location", L);
        entityClassesOpenNLP.put("organization", O);
        entityClassesOpenNLP.put("person", P);
    }

    protected static final Map<String, String> entityClassesILLINOIS = new HashMap<String, String>();
    static {
        entityClassesILLINOIS.put("LOC", L);
        entityClassesILLINOIS.put("ORG", O);
        entityClassesILLINOIS.put("PER", P);
        // entityClassesILLINOIS.put("MISC", M);
    }

    /**
     * Gets the entity class for a Balie entity type/class.
     */
    public static String oracel(String tag) {
        String t = entityClassesOracel.get(tag);
        if (t == null)
            t = getNullCategory();
        return t;
    }

    /**
     * Gets the entity class for a Balie entity type/class.
     */
    public static String balie(String tag) {
        String t = entityClassesBalie.get(tag);
        if (t == null)
            t = getNullCategory();
        return t;
    }

    /**
     * Gets the entity class for a openNLP entity type/class.
     */
    public static String openNLP(String tag) {
        String t = entityClassesOpenNLP.get(tag);
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
     * Gets the entity class for a stanford entity type/class.
     */
    public static String stanford(String stanfordTag) {
        String t = entityClassesSTAN.get(stanfordTag);
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
