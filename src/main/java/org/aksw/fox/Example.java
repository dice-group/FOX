package org.aksw.fox;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.fox.tools.ner.en.StanfordEN;
import org.aksw.fox.utils.FoxConst;
import org.apache.http.entity.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import de.renespeck.swissknife.http.Requests;

public class Example {

  public final static Logger LOG = LogManager.getLogger(Example.class);

  public static void main(final String[] args) {
    programmatic();
    // webAPI();
    // _webAPI("http://fox-demo.aksw.org");
  }

  /**
   * Example programmatic use of FOX.
   */
  protected static void programmatic() {
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
      // e = fox.doNERLight(tool);
      e = fox.doNER();

      // linking
      fox.setURIs(e);

      // output
      fox.setOutput(e, null);

      LOG.info(fox.getResultsAndClean());
    }
  }

  /**
   * Example web api use of FOX.
   */
  protected static void _webAPI(final String url) {
    LOG.info("webAPI ...");

    try {
      final String r = Requests.postJson(url.concat("/call/ner/entities"),
          new JSONObject()
              .put(FoxParameter.Parameter.TYPE.toString(), FoxParameter.Type.TEXT.toString())
              /*
               * .put(FoxParameter.Parameter.LANG.toString(), FoxParameter.Langs.EN.toString())
               */
              .put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString())
              .put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName())
              .put(FoxParameter.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1),
          ContentType.APPLICATION_JSON);
      LOG.info(r);

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

}
