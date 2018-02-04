package org.aksw.fox.webservice.util;

import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.data.FoxParameter.Output;
import org.aksw.fox.tools.ToolsGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class RouteConfig {
  public static Logger LOG = LogManager.getLogger(RouteConfig.class);

  public String getConfig() {

    final JSONObject cfg = new JSONObject();
    final JSONArray langs = new JSONArray();
    try {
      ToolsGenerator.nerTools.keySet().forEach(lang -> {
        cfg.put(lang, new JSONObject());

        final JSONObject nerTools = new JSONObject();
        ToolsGenerator.nerTools.get(lang).forEach(nerTool -> {
          nerTools.put(nerTool.substring(nerTool.lastIndexOf(".") + 1), nerTool);
        });
        cfg.getJSONObject(lang).put("ner", nerTools);
        cfg.getJSONObject(lang).put("nerlinking", ToolsGenerator.disambiguationTools.get(lang));
        langs.put(lang);
      });
      cfg.put("lang", langs);
      final JSONArray ja = new JSONArray();
      for (final Output v : FoxParameter.Output.values()) {
        ja.put(v.toString());
      }
      cfg.put("out", ja);

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return cfg.toString(2);
  }
}
