package org.aksw.fox.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentCreator;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.apache.jena.riot.Lang;
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
  private final FoxLanguageDetector languageDetector = new FoxLanguageDetector();
  private final TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
  private final TurtleNIFDocumentCreator creator = new TurtleNIFDocumentCreator();

  private static final String PERSON_TYPE_URI = "scmsann:PERSON";
  private static final String LOCATION_TYPE_URI = "scmsann:LOCATION";
  private static final String ORGANIZATION_TYPE_URI = "scmsann:ORGANIZATION";

  private static final String DOLCE_PERSON_TYPE_URI =
      "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person";
  private static final String DOLCE_LOCATION_TYPE_URI =
      "http://www.ontologydesignpatterns.org/ont/d0.owl#Location";
  private static final String DOLCE_ORGANIZATION_TYPE_URI =
      "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Organization";

  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
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
      FoxJena.prints.forEach(ja::put);
      cfg.put("out", ja);

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return cfg.toString(2);
  }

  /**
   * <code>

    curl -d "@example.ttl" -H "Content-Type: application/x-turtle" http://0.0.0.0:4444/call/ner/entities
  
    </code>
   */
  @Path("entities")
  @Consumes("application/x-turtle")
  @Produces("application/x-turtle")
  @POST
  public String postApiNif(@Context final Request request, @Context final HttpHeaders hh) {
    String nifDocument = "";
    try {
      // read request
      final InputStream in = request.getInputStream();
      final Reader reader = new InputStreamReader(in);
      Document document;
      try {
        document = parser.getDocumentFromNIFReader(reader);
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
        return "";
      }
      try {
        reader.close();
        in.close();
      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }

      // send request to fox
      final Map<String, String> parameter = new HashMap<>();
      parameter.put(Fox.Parameter.INPUT.toString(), document.getText());
      parameter.put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString());
      parameter.put(Fox.Parameter.LANG.toString(), Fox.Langs.EN.toString());
      parameter.put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString());
      parameter.put(Fox.Parameter.OUTPUT.toString(), Lang.JSONLD.getName());

      final String response = requestFox(parameter);
      LOG.info(response);
      // parse fox response
      if ((response != null) && !response.isEmpty()) {

        final JSONObject outObj = new JSONObject(response);
        new ArrayList<>();
        if (outObj.has("@graph")) {
          final JSONArray graph = outObj.getJSONArray("@graph");
          for (int i = 0; i < graph.length(); i++) {
            parseType(graph.getJSONObject(i), document);
          }
        } else {
          parseType(outObj, document);
        }

      }

      nifDocument = creator.getDocumentAsNIFString(document);
    } catch (final Exception e) {
      LOG.error(e.getStackTrace(), e);
    }
    return nifDocument;
  }

  @Path("entities")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_XML)
  @POST
  public String postApi(final JsonObject properties, @Context final Request request,
      @Context final HttpHeaders hh, @Context final UriInfo ui) {

    String output = "";

    final Map<String, String> parameter = new HashMap<>();
    try {
      parameter.put(Fox.Parameter.INPUT.toString(),
          properties.getString(Fox.Parameter.INPUT.toString()));
      parameter.put(Fox.Parameter.TYPE.toString(),
          properties.getString(Fox.Parameter.TYPE.toString()));
      parameter.put(Fox.Parameter.TASK.toString(),
          properties.getString(Fox.Parameter.TASK.toString()));
      parameter.put(Fox.Parameter.OUTPUT.toString(),
          properties.getString(Fox.Parameter.OUTPUT.toString()));
      if (properties.get(Fox.Parameter.LINKING.toString()) != null) {
        parameter.put(Fox.Parameter.LINKING.toString(),
            properties.getString(Fox.Parameter.LINKING.toString()));
      }

      // get input data
      switch (parameter.get(Fox.Parameter.TYPE.toString()).toLowerCase()) {
        case "url":
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
        if (properties.get(Fox.Parameter.LANG.toString()) != null) {
          lang = properties.getString(Fox.Parameter.LANG.toString());
        }

        Fox.Langs l = Fox.Langs.fromString(lang);
        if (l == null) {

          l = languageDetector.detect(parameter.get(Fox.Parameter.INPUT.toString()));
          LOG.info("lang" + lang);
          if (l != null) {
            lang = l.toString();
          } else {
            lang = "";
          }
        }
        parameter.put(Fox.Parameter.LANG.toString(), lang);
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }

      if (lang != null) {
        LOG.info("parameter:");
        parameter.entrySet().forEach(p -> {
          LOG.info(p.getKey() + ":" + p.getValue());
        });

        output = requestFox(parameter);
      }
    } catch (final Exception e) {
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

  private String requestFox(final Map<String, String> parameter) {
    String output = "";
    // get a fox instance
    IFox fox = Server.pool.get(parameter.get(Fox.Parameter.LANG.toString())).poll();

    // init. thread
    final Fiber fiber = new ThreadFiber();
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
    } catch (final InterruptedException e) {
      LOG.error("Fox timeout after " + FoxCfg.get(FoxHttpHandler.CFG_KEY_FOX_LIFETIME) + "min.");
      LOG.error("\n", e);
      LOG.error("input: " + parameter.get(Fox.Parameter.INPUT.toString()));
    }

    // shutdown thread
    fiber.dispose();

    if (latch.getCount() == 0) {
      output = fox.getResults();
      Server.pool.get(parameter.get(Fox.Parameter.LANG.toString())).push(fox);
    } else {
      fox = null;
      Server.pool.get(parameter.get(Fox.Parameter.LANG.toString())).add();
    }
    return output;
  }

  protected void parseType(final JSONObject entity, final Document resultDoc) {
    try {

      if ((entity != null) && entity.has("means") && entity.has("beginIndex")
          && entity.has("ann:body")) {

        String uri = entity.getString("means");
        if (uri.startsWith("dbpedia:")) {
          uri = "http://dbpedia.org/resource/" + uri.substring(8);
        }
        final String body = entity.getString("ann:body");
        final Object begin = entity.get("beginIndex");
        final Object typeObject = entity.get("@type");
        final Set<String> types = new HashSet<String>();
        if (typeObject instanceof JSONArray) {
          final JSONArray typeArray = (JSONArray) typeObject;
          for (int i = 0; i < typeArray.length(); ++i) {
            addType(typeArray.getString(i), types);
          }
        } else {
          addType(typeObject.toString(), types);
        }
        uri = URLDecoder.decode(uri, "UTF-8");
        if (begin instanceof JSONArray) {
          // for all indices
          for (int i = 0; i < ((JSONArray) begin).length(); ++i) {
            resultDoc.addMarking(new TypedNamedEntity(
                Integer.valueOf(((JSONArray) begin).getString(i)), body.length(), uri, types));
          }
        } else if (begin instanceof String) {
          resultDoc.addMarking(
              new TypedNamedEntity(Integer.valueOf((String) begin), body.length(), uri, types));
        } else if (LOG.isDebugEnabled()) {
          LOG.debug("Couldn't find start position for annotation.");
        }
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  protected void addType(final String typeString, final Set<String> types) {
    switch (typeString) {
      case PERSON_TYPE_URI: {
        types.add(DOLCE_PERSON_TYPE_URI);
        types.add("Schema:Person");
        types.add("http://dbpedia.org/ontology/Person");
        break;
      }
      case LOCATION_TYPE_URI: {
        types.add(DOLCE_LOCATION_TYPE_URI);
        types.add("Schema:Place");
        types.add("Schema:Location");
        types.add("http://dbpedia.org/ontology/Place");
        break;
      }
      case ORGANIZATION_TYPE_URI: {
        types.add(DOLCE_ORGANIZATION_TYPE_URI);
        types.add("schema:Organisation");
        types.add("http://dbpedia.org/ontology/Organisation");
        break;
      }
    }
    types.add(typeString.replaceFirst("scmsann:", "http://ns.aksw.org/scms/annotations/"));
  }
}
