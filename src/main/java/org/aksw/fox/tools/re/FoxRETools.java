package org.aksw.fox.tools.re;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.fox.tools.re.en.REStanford;
import org.aksw.fox.tools.re.en.boa.BoaEN;

public class FoxRETools {

  protected Map<String, List<IRE>> relationTool = new HashMap<>();

  public FoxRETools() {
    // TODO: move RE to config
    relationTool.put("en", Arrays.asList(new REStanford(), new BoaEN()));
  }

  public List<IRE> getRETool(final String lang) {
    return relationTool.get(lang);
  }
}
