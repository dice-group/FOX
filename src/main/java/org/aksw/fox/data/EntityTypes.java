package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class EntityTypes {

  public static final String L = "LOCATION";
  public static final String O = "ORGANIZATION";
  public static final String P = "PERSON";

  public static final TreeSet<String> AllTypesSet =
      new TreeSet<>(Arrays.asList(L, O, P, BILOUEncoding.O));

  public static final List<String> AllTypesList = new ArrayList<>(AllTypesSet);
}
