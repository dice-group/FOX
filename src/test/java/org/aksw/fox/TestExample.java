package org.aksw.fox;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.fox.tools.ner.en.StanfordEN;
import org.aksw.fox.utils.FoxConst;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.Assert;

public class TestExample {

  public final static Logger LOG = LogManager.getLogger(TestExample.class);

  @Test
  public void example() {
    LOG.info("programmatic ...");

    final String lang = FoxParameter.Langs.EN.toString();
    LOG.info(lang);
    LOG.info(ToolsGenerator.usedLang);
    if (!ToolsGenerator.usedLang.contains(lang)) {
      LOG.warn("language not supported");
    } else {
      final Fox fox = new Fox(lang);

      final Map<String, String> defaults = FoxParameter.getDefaultParameter();

      defaults.put(FoxParameter.Parameter.TYPE.toString(), FoxParameter.Type.TEXT.toString());
      defaults.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
      defaults.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
      defaults.put(FoxParameter.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1);
      fox.setParameter(defaults);

      // fox light version
      final String tool = StanfordEN.class.getName();
      Set<Entity> e;
      if (!ToolsGenerator.nerTools.get(lang).contains(tool)) {
        LOG.warn("can't find the given tool " + tool);
      }

      e = fox.doNER();
      Assert.assertTrue(e.size() > 0);
      e = fox.doNERLight(tool);
      Assert.assertTrue(e.size() > 0);

      // linking
      fox.setURIs(e);

      // output
      fox.setOutput(e, null);
      Assert.assertTrue(e.size() > 0);
      Assert.assertTrue(fox.getResultsAndClean().length() > 0);
    }
  }

}
