package org.aksw.fox.tools.re;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.fox.FoxParameter;
import org.aksw.fox.tools.re.de.BoaDE;
import org.aksw.fox.tools.re.en.BoaEN;
import org.aksw.fox.tools.re.en.REStanford;
import org.aksw.fox.tools.re.fr.BoaFR;

public class FoxRETools {

  protected Map<String, List<IRE>> relationTool = new HashMap<>();

  public FoxRETools() {

    // TODO: move RE to config
    relationTool.put(FoxParameter.Langs.EN.name().toLowerCase(),
        Arrays.asList(new REStanford(), new BoaEN()));
    relationTool.put(FoxParameter.Langs.DE.name().toLowerCase(), Arrays.asList(new BoaDE()));
    relationTool.put(FoxParameter.Langs.FR.name().toLowerCase(), Arrays.asList(new BoaFR()));
  }

  public List<IRE> getRETool(final String lang) {
    return relationTool.get(lang);
  }
}
