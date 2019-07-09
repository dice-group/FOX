package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.aksw.fox.data.encode.BILOUEncoding;

/**
 *
 * @author rspeck
 *
 */
public class EntityTypes {

  public static final String L = "LOCATION";
  public static final String O = "ORGANIZATION";
  public static final String P = "PERSON";

  public static final List<String> AllTypesList = new ArrayList<>(//
      new TreeSet<>(Arrays.asList(L, O, P, BILOUEncoding.O))//
  );
}
