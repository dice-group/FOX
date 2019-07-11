package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.decode.GreedyLeftToRight;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

// TODO: This test depends on the trained model
@Deprecated
@Ignore
public class GreedyLeftToRightTest {

  Logger LOG = LogManager.getLogger(GreedyLeftToRightTest.class);

  GreedyLeftToRight gltr = new GreedyLeftToRight();

  String data = "";

  // the data string contains 8 decoded entities
  int expect = 0;

  // creates and checks test data
  private List<Entity> data(final String data) {
    final List<Entity> tokenBasedBILOU = new ArrayList<>();
    for (final String entity : data.split(";")) {
      final String[] split = entity.split(":");

      final String name = split[0];
      final String type = split[1];

      if (!type.equals("Z")) {
        //
      }

      tokenBasedBILOU.add(new Entity(name, type, "testTool"));
    }
    return tokenBasedBILOU;
  }

  @Test
  public void gftrTestA() {
    data = "BarackfFWAeRQ9w0L0:B_Per;ObamafFWAeRQ9w0L7:L_Per;andfFWAeRQ9w0L13:Z;"
        + "MichellefFWAeRQ9w0L17:U_Per;arefFWAeRQ9w0L26:Z;marriedfFWAeRQ9w0L30:Z;"
        + "sincefFWAeRQ9w0L38:Z;1992fFWAeRQ9w0L44:Z;ObamafFWAeRQ9w0L50:U_Per;"
        + "wasfFWAeRQ9w0L56:Z;bornfFWAeRQ9w0L60:Z;atfFWAeRQ9w0L65:Z;"
        + "KapiolanifFWAeRQ9w0L68:B_Loc;MedicalfFWAeRQ9w0L78:I_Loc;"
        + "CenterfFWAeRQ9w0L86:Z;forfFWAeRQ9w0L93:Z;WomenfFWAeRQ9w0L97:Z;"
        + "andfFWAeRQ9w0L103:Z;ChildrenfFWAeRQ9w0L107:Z;infFWAeRQ9w0L116:Z;"
        + "HonolulufFWAeRQ9w0L119:U_Loc;ObamafFWAeRQ9w0L129:U_Per;wasfFWAeRQ9w0L135:Z;"
        + "rumoredfFWAeRQ9w0L139:Z;tofFWAeRQ9w0L147:Z;befFWAeRQ9w0L150:Z;"
        + "bornfFWAeRQ9w0L153:Z;infFWAeRQ9w0L158:Z;KenyafFWAeRQ9w0L161:U_Loc;"
        + "ButfFWAeRQ9w0L168:Z;ObamafFWAeRQ9w0L173:U_Per;wasfFWAeRQ9w0L179:Z;"
        + "bornfFWAeRQ9w0L183:Z;infFWAeRQ9w0L188:Z;HawaiifFWAeRQ9w0L191:U_Loc";
    expect = 8;

    //
    final List<Entity> decodedBILOU = gftrTest(data, expect);
    LOG.info(decodedBILOU);
    final boolean test = decodedBILOU
        .contains(new Entity("Barack Obama", EntityTypes.P, Entity.DEFAULT_RELEVANCE, "fox", 0));
    Assert.assertTrue(test);
  }

  @Test
  public void gftrTestB() {
    data =
        "ColognefFWAeRQ9w0L0:U_Loc;GermanfFWAeRQ9w0L8:Z;KölnfFWAeRQ9w0L16:Z;KölschfFWAeRQ9w0L22:Z;"
            + "KöllefFWAeRQ9w0L30:Z;isfFWAeRQ9w0L36:Z;GermanyfFWAeRQ9w0L39:U_Loc;sfFWAeRQ9w0L47:Z;"
            + "fourth-largestfFWAeRQ9w0L49:Z;cityfFWAeRQ9w0L64:Z;afterfFWAeRQ9w0L70:Z;"
            + "BerlinfFWAeRQ9w0L76:U_Loc;HamburgfFWAeRQ9w0L84:U_Loc;andfFWAeRQ9w0L93:Z;"
            + "MunichfFWAeRQ9w0L97:U_Loc;andfFWAeRQ9w0L106:Z;isfFWAeRQ9w0L110:Z;thefFWAeRQ9w0L113:Z;"
            + "largestfFWAeRQ9w0L117:Z;cityfFWAeRQ9w0L125:Z;bothfFWAeRQ9w0L130:Z;infFWAeRQ9w0L135:Z;"
            + "thefFWAeRQ9w0L138:Z;GermanfFWAeRQ9w0L142:B_Loc;FederalfFWAeRQ9w0L149:I_Loc;"
            + "StatefFWAeRQ9w0L157:I_Loc;offFWAeRQ9w0L163:I_Loc;NorthfFWAeRQ9w0L166:I_Loc;"
            + "Rhine-WestphaliafFWAeRQ9w0L172:L_Loc;andfFWAeRQ9w0L189:Z;withinfFWAeRQ9w0L193:Z;"
            + "thefFWAeRQ9w0L200:Z;Rhine-RuhrfFWAeRQ9w0L204:Z;MetropolitanfFWAeRQ9w0L215:Z;"
            + "AreafFWAeRQ9w0L228:Z;onefFWAeRQ9w0L234:Z;offFWAeRQ9w0L238:Z;thefFWAeRQ9w0L241:Z;"
            + "majorfFWAeRQ9w0L245:Z;EuropeanfFWAeRQ9w0L251:Z;metropolitanfFWAeRQ9w0L260:Z;"
            + "areasfFWAeRQ9w0L273:Z;withfFWAeRQ9w0L279:Z;morefFWAeRQ9w0L284:Z;thanfFWAeRQ9w0L289:Z;"
            + "tenfFWAeRQ9w0L294:Z;millionfFWAeRQ9w0L298:Z;inhabitantsfFWAeRQ9w0L306:Z;"
            + "ColognefFWAeRQ9w0L319:U_Loc;isfFWAeRQ9w0L327:Z;locatedfFWAeRQ9w0L330:Z;"
            + "onfFWAeRQ9w0L338:Z;bothfFWAeRQ9w0L341:Z;sidesfFWAeRQ9w0L346:Z;offFWAeRQ9w0L352:Z;"
            + "thefFWAeRQ9w0L355:Z;RhinefFWAeRQ9w0L359:B_Loc;RiverfFWAeRQ9w0L365:L_Loc;"
            + "ThefFWAeRQ9w0L372:Z;cityfFWAeRQ9w0L376:Z;sfFWAeRQ9w0L381:Z;famousfFWAeRQ9w0L383:Z;"
            + "ColognefFWAeRQ9w0L390:B_Loc;CathedralfFWAeRQ9w0L398:L_Loc;KölnerfFWAeRQ9w0L409:B_Per;"
            + "DomfFWAeRQ9w0L416:L_Per;isfFWAeRQ9w0L421:Z;thefFWAeRQ9w0L424:Z;seatfFWAeRQ9w0L428:Z;"
            + "offFWAeRQ9w0L433:Z;thefFWAeRQ9w0L436:Z;CatholicfFWAeRQ9w0L440:B_Org;"
            + "ArchbishopfFWAeRQ9w0L449:I_Org;offFWAeRQ9w0L460:I_Org;ColognefFWAeRQ9w0L463:L_Org;"
            + "ThefFWAeRQ9w0L472:Z;UniversityfFWAeRQ9w0L476:B_Org;offFWAeRQ9w0L487:I_Org;"
            + "ColognefFWAeRQ9w0L490:L_Org;UniversitätfFWAeRQ9w0L499:B_Org;zufFWAeRQ9w0L511:I_Org;"
            + "KölnfFWAeRQ9w0L514:L_Org;isfFWAeRQ9w0L520:Z;onefFWAeRQ9w0L523:Z;offFWAeRQ9w0L527:Z;"
            + "EuropefFWAeRQ9w0L530:U_Loc;sfFWAeRQ9w0L537:Z;oldestfFWAeRQ9w0L539:Z;"
            + "andfFWAeRQ9w0L546:Z;largestfFWAeRQ9w0L550:Z;universitiesfFWAeRQ9w0L558:Z";
    expect = 14;
    gftrTest(data, expect);
  }

  public List<Entity> gftrTest(final String data, final int expect) {

    final List<Entity> tokenBasedBILOU = data(data);

    // runs test decoding
    final List<Entity> decodedBILOU = gltr.decode(tokenBasedBILOU);

    Assert.assertEquals(expect, decodedBILOU.size());
    return decodedBILOU;
  }
}
