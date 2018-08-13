package org.aksw.fox.tools.re;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.tools.re.de.BoaDE;
import org.aksw.fox.tools.re.en.BoaEN;
import org.aksw.fox.tools.re.en.OcelotEN;
import org.aksw.fox.tools.re.en.REStanford;
import org.aksw.fox.tools.re.fr.BoaFR;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxRETools {

  protected Map<String, List<IRE>> relationTool = new HashMap<>();

  /**
   *
   * Constructor.
   *
   */
  public FoxRETools() {
    // TODO: move to config

    // en
    // relationTool.put(FoxParameter.Langs.EN.name().toLowerCase(),
    //    Arrays.asList(new REStanford(), new BoaEN(), new OcelotEN()));
    relationTool.put(FoxParameter.Langs.EN.name().toLowerCase(),
        Arrays.asList(new REStanford(), new BoaEN()));
    // de
    relationTool.put(FoxParameter.Langs.DE.name().toLowerCase(), Arrays.asList(new BoaDE()));

    // fr
    relationTool.put(FoxParameter.Langs.FR.name().toLowerCase(), Arrays.asList(new BoaFR()));
  }

  public List<IRE> getRETool(final String lang) {
    return relationTool.get(lang);
  }
}
