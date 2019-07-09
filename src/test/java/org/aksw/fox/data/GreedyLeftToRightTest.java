package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.decode.GreedyLeftToRight;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class GreedyLeftToRightTest {

  Logger LOG = LogManager.getLogger(GreedyLeftToRightTest.class);

  GreedyLeftToRight gltr = new GreedyLeftToRight();

  String data =
      "BarackfFWAeRQ9w0L0:B_Per;ObamafFWAeRQ9w0L7:L_Per;andfFWAeRQ9w0L13:Z;MichellefFWAeRQ9w0L17:U_Per;arefFWAeRQ9w0L26:Z;marriedfFWAeRQ9w0L30:Z;sincefFWAeRQ9w0L38:Z;1992fFWAeRQ9w0L44:Z;ObamafFWAeRQ9w0L50:U_Per;wasfFWAeRQ9w0L56:Z;bornfFWAeRQ9w0L60:Z;atfFWAeRQ9w0L65:Z;KapiolanifFWAeRQ9w0L68:B_Loc;MedicalfFWAeRQ9w0L78:I_Loc;CenterfFWAeRQ9w0L86:Z;forfFWAeRQ9w0L93:Z;WomenfFWAeRQ9w0L97:Z;andfFWAeRQ9w0L103:Z;ChildrenfFWAeRQ9w0L107:Z;infFWAeRQ9w0L116:Z;HonolulufFWAeRQ9w0L119:U_Loc;ObamafFWAeRQ9w0L129:U_Per;wasfFWAeRQ9w0L135:Z;rumoredfFWAeRQ9w0L139:Z;tofFWAeRQ9w0L147:Z;befFWAeRQ9w0L150:Z;bornfFWAeRQ9w0L153:Z;infFWAeRQ9w0L158:Z;KenyafFWAeRQ9w0L161:U_Loc;ButfFWAeRQ9w0L168:Z;ObamafFWAeRQ9w0L173:U_Per;wasfFWAeRQ9w0L179:Z;bornfFWAeRQ9w0L183:Z;infFWAeRQ9w0L188:Z;HawaiifFWAeRQ9w0L191:U_Loc";

  // the data string contains 8 decoded entities
  int expect = 8;

  @Test
  public void gftrTest() {

    final List<Entity> tokenBasedBILOU = new ArrayList<>();

    // creates test data
    int numTotal = 0;
    int numWithType = 0;
    for (final String entity : data.split(";")) {
      numTotal++;
      final String[] split = entity.split(":");

      final String name = split[0];
      final String type = split[1];

      if (!type.equals("Z")) {
        numWithType++;
      }

      tokenBasedBILOU.add(new Entity(name, type, "testTool"));
    }

    LOG.info(numWithType + " " + tokenBasedBILOU);
    Assert.assertEquals(numTotal, tokenBasedBILOU.size());

    // runs test decodeing
    final List<Entity> decodedBILOU = gltr.decode(tokenBasedBILOU);

    Assert.assertEquals(expect, decodedBILOU.size());
    LOG.info(decodedBILOU);
  }
}
