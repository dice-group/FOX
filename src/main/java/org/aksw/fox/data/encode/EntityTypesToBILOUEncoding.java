package org.aksw.fox.data.encode;

import org.aksw.fox.data.EntityTypes;

public class EntityTypesToBILOUEncoding {

  public static String toBegin(final String type) {
    switch (type) {
      case EntityTypes.L:
        return BILOUEncoding.B_Loc;
      case EntityTypes.O:
        return BILOUEncoding.B_Org;
      case EntityTypes.P:
        return BILOUEncoding.B_Per;
      default:
        return BILOUEncoding.O;
    }
  }

  public static String toInside(final String type) {
    switch (type) {
      case EntityTypes.L:
        return BILOUEncoding.I_Loc;
      case EntityTypes.O:
        return BILOUEncoding.I_Org;
      case EntityTypes.P:
        return BILOUEncoding.I_Per;
      default:
        return BILOUEncoding.O;
    }
  }

  public static String toLast(final String type) {
    switch (type) {
      case EntityTypes.L:
        return BILOUEncoding.L_Loc;
      case EntityTypes.O:
        return BILOUEncoding.L_Org;
      case EntityTypes.P:
        return BILOUEncoding.L_Per;
      default:
        return BILOUEncoding.O;
    }
  }

  public static String toUnit(final String type) {
    switch (type) {
      case EntityTypes.L:
        return BILOUEncoding.U_Loc;
      case EntityTypes.O:
        return BILOUEncoding.U_Org;
      case EntityTypes.P:
        return BILOUEncoding.U_Per;
      default:
        return BILOUEncoding.O;
    }
  }
}
