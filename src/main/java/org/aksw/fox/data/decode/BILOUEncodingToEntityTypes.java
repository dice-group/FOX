package org.aksw.fox.data.decode;

import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;

/**
 *
 * @author rspeck
 *
 */
public class BILOUEncodingToEntityTypes {

  /**
   *
   * @param type BILOU encoded type
   * @return decoded type
   */
  public static String toEntiyType(final String type) {
    switch (type) {
      case BILOUEncoding.B_Loc:
      case BILOUEncoding.I_Loc:
      case BILOUEncoding.L_Loc:
      case BILOUEncoding.U_Loc:
        return EntityTypes.L;
      case BILOUEncoding.B_Org:
      case BILOUEncoding.I_Org:
      case BILOUEncoding.L_Org:
      case BILOUEncoding.U_Org:
        return EntityTypes.O;
      case BILOUEncoding.B_Per:
      case BILOUEncoding.I_Per:
      case BILOUEncoding.L_Per:
      case BILOUEncoding.U_Per:
        return EntityTypes.P;
      default:
        return BILOUEncoding.O;
    }
  }

  public static boolean isBegin(final String type) {
    switch (type) {
      case BILOUEncoding.B_Loc:
      case BILOUEncoding.B_Org:
      case BILOUEncoding.B_Per:
        return true;
      default:
        return false;
    }
  }

  public static boolean isInside(final String type) {
    switch (type) {
      case BILOUEncoding.I_Loc:
      case BILOUEncoding.I_Org:
      case BILOUEncoding.I_Per:
        return true;
      default:
        return false;
    }
  }

  public static boolean isLast(final String type) {
    switch (type) {
      case BILOUEncoding.L_Loc:
      case BILOUEncoding.L_Org:
      case BILOUEncoding.L_Per:
        return true;
      default:
        return false;
    }
  }

  public static boolean isUnit(final String type) {
    switch (type) {
      case BILOUEncoding.U_Loc:
      case BILOUEncoding.U_Org:
      case BILOUEncoding.U_Per:
        return true;
      default:
        return false;
    }
  }
}
