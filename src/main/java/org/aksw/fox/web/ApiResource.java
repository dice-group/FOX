package org.aksw.fox.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.aksw.fox.Fox;
import org.aksw.fox.IFox;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxJena;
import org.aksw.fox.utils.FoxLanguageDetector;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("ner")
public class ApiResource {

  public static Logger LOG = LogManager.getLogger(ApiResource.class);
  FoxLanguageDetector languageDetector = new FoxLanguageDetector();

  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public String getConfig() {
    JSONObject cfg = new JSONObject();
    JSONArray langs = new JSONArray();
    try {
      ToolsGenerator.nerTools.keySet().forEach(lang -> {
        cfg.put(lang, new JSONObject());

        JSONObject nerTools = new JSONObject();
        ToolsGenerator.nerTools.get(lang).forEach(nerTool -> {
          nerTools.put(nerTool.substring(nerTool.lastIndexOf(".") + 1), nerTool);
        });
        cfg.getJSONObject(lang).put("ner", nerTools);
        cfg.getJSONObject(lang).put("nerlinking", ToolsGenerator.disambiguationTools.get(lang));
        langs.put(lang);
      });
      cfg.put("lang", langs);
      JSONArray ja = new JSONArray();
      FoxJena.prints.forEach(ja::put);
      cfg.put("out", ja);

    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return cfg.toString(2);
  }

  @Path("entities")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_XML)
  @POST
  public String postApi(final JsonObject properties, @Context Request request,
      @Context HttpHeaders hh, @Context UriInfo ui) {
    String output = "";

    Map<String, String> parameter = new HashMap<>();
    try {
      parameter.put(Fox.Parameter.INPUT.toString(),
          properties.getString(Fox.Parameter.INPUT.toString()));
      parameter.put(Fox.Parameter.TYPE.toString(),
          properties.getString(Fox.Parameter.TYPE.toString()));
      parameter.put(Fox.Parameter.TASK.toString(),
          properties.getString(Fox.Parameter.TASK.toString()));
      parameter.put(Fox.Parameter.OUTPUT.toString(),
          properties.getString(Fox.Parameter.OUTPUT.toString()));
      if (properties.get(Fox.Parameter.LINKING.toString()) != null)
        parameter.put(Fox.Parameter.LINKING.toString(),
            properties.getString(Fox.Parameter.LINKING.toString()));

      // get input data
      switch (parameter.get(Fox.Parameter.TYPE.toString()).toLowerCase()) {
        case "url":
          // parameter.put(Fox.Parameter.TYPE.toString(),
          // Fox.Type.TEXT.toString());
          parameter.put(Fox.Parameter.INPUT.toString(),
              FoxTextUtil.urlToText(parameter.get(Fox.Parameter.INPUT.toString())));
          break;
        case "text":
          parameter.put(Fox.Parameter.INPUT.toString(),
              FoxTextUtil.htmlToText(parameter.get(Fox.Parameter.INPUT.toString())));
          break;
      }

      // lang
      String lang = null;
      try {
        if (properties.get(Fox.Parameter.LANG.toString()) != null)
          lang = properties.getString(Fox.Parameter.LANG.toString());

        Fox.Langs l = Fox.Langs.fromString(lang);
        if (l == null) {

          l = languageDetector.detect(parameter.get(Fox.Parameter.INPUT.toString()));
          LOG.info("lang" + lang);
          if (l != null)
            lang = l.toString();
          else
            lang = "";
        }
        parameter.put(Fox.Parameter.LANG.toString(), lang);
      } catch (Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }

      if (lang != null) {
        LOG.info("parameter:");
        parameter.entrySet().forEach(p -> {
          LOG.info(p.getKey() + ":" + p.getValue());
        });

        // get a fox instance
        IFox fox = Server.pool.get(lang).poll();

        // init. thread
        Fiber fiber = new ThreadFiber();
        fiber.start();
        final CountDownLatch latch = new CountDownLatch(1);

        // set up fox
        fox.setCountDownLatch(latch);
        fox.setParameter(parameter);

        // run fox
        fiber.execute(fox);

        // wait 5min or till the fox instance is finished
        try {
          latch.await(Integer.parseInt(FoxCfg.get(FoxHttpHandler.CFG_KEY_FOX_LIFETIME)),
              TimeUnit.MINUTES);
        } catch (InterruptedException e) {
          LOG.error(
              "Fox timeout after " + FoxCfg.get(FoxHttpHandler.CFG_KEY_FOX_LIFETIME) + "min.");
          LOG.error("\n", e);
          LOG.error("input: " + parameter.get(Fox.Parameter.INPUT.toString()));
        }

        // shutdown thread
        fiber.dispose();

        if (latch.getCount() == 0) {
          output = fox.getResults();
          Server.pool.get(lang).push(fox);
        } else {
          fox = null;
          Server.pool.get(lang).add();
        }

        LOG.info(ui.getRequestUri());
        LOG.info(hh.getRequestHeaders());
        LOG.info("IP: " + request.getRemoteAddr());
      }
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return output;
  }

  @Path("entitiesFeedback")
  @POST
  public JsonObject postText() {
    // TODO: entitiesFeedback
    LOG.info("FEEDBACK");
    return null;
  }

  public static String getPath() {
    return "/call/";
  }
}
