package org.aksw.fox.data.encode;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * <code>
  BILOU or BMEWO <br>
  ================ <br>
  B - Begin <br>
  I - Inside <br>
  L - Last <br>
  O - Outside <br>
  U - Unit-length <br>
</code>
 *
 * @author rspeck
 *
 */
public class BILOUEncoding {

  public static final String O = "Z";

  public static final String B_Loc = "B_Loc";
  public static final String I_Loc = "I_Loc";
  public static final String L_Loc = "L_Loc";
  public static final String U_Loc = "U_Loc";

  public static final String B_Org = "B_Org";
  public static final String I_Org = "I_Org";
  public static final String L_Org = "L_Org";
  public static final String U_Org = "U_Org";

  public static final String B_Per = "B_Per";
  public static final String I_Per = "I_Per";
  public static final String L_Per = "L_Per";
  public static final String U_Per = "U_Per";

  public static final TreeSet<String> AllTypesSet = new TreeSet<>(//
      Arrays.asList(//
          O, //
          B_Loc, I_Loc, L_Loc, U_Loc, //
          B_Org, I_Org, L_Org, U_Org, //
          B_Per, I_Per, L_Per, U_Per)//
  );
}
