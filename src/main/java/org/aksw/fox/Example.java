package org.aksw.fox;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.tools.ner.en.StanfordEN;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.aksw.fox.web.Server;
import org.apache.http.entity.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import de.renespeck.swissknife.http.Requests;

public class Example {
  static {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);
  }

  public final static Logger LOG = LogManager.getLogger(Example.class);

  public static void main(final String[] args) {
    programmatic();
    // webAPI();
    // _webAPI("http://fox-demo.aksw.org");
  }

  /**
   * Example programmatic use of FOX.
   */
  public static void programmatic() {
    LOG.info("programmatic ...");

    final String lang = Fox.Langs.EN.toString();
    LOG.info(lang);
    LOG.info(ToolsGenerator.usedLang);
    if (!ToolsGenerator.usedLang.contains(lang)) {
      LOG.warn("language not supported");
    } else {
      final Fox fox = new Fox(lang);

      final Map<String, String> defaults = fox.getDefaultParameter();

      defaults.put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString());
      defaults.put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString());
      defaults.put(Fox.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
      defaults.put(Fox.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1);
      fox.setParameter(defaults);

      // fox light version
      final String tool = StanfordEN.class.getName();
      Set<Entity> e;
      if (!ToolsGenerator.nerTools.get(lang).contains(tool)) {
        LOG.warn("can't find the given tool " + tool);
      }
      e = fox.doNERLight(tool);
      // e = fox.doNER();

      // linking
      fox.setURIs(e);

      // output
      fox.setOutput(e, null);

      LOG.info(fox.getResults());
    }
  }

  /**
   * Example web api use of FOX.
   */
  private static void _webAPI(final String url) {
    LOG.info("webAPI ...");

    try {
      final String r = Requests.postJson(url.concat("/call/ner/entities"),
          new JSONObject().put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString())
              /*
               * .put(Fox.Parameter.LANG.toString(), Fox.Langs.EN.toString())
               */
              .put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString())
              .put(Fox.Parameter.OUTPUT.toString(), Lang.TURTLE.getName())
              .put(Fox.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1),
          ContentType.APPLICATION_JSON);
      LOG.info(r);

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

}
