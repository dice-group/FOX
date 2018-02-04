package org.aksw.fox.legacy.api;

import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

import org.aksw.fox.IFox;
import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.legacy.FoxHttpHandler;
import org.aksw.fox.legacy.Server;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxTextUtil;
import org.aksw.fox.webservice.util.RouteConfig;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;
import org.json.JSONArray;
import org.json.JSONObject;

@Deprecated
@Path("fox")
public class ApiResource {

  public static Logger LOG = LogManager.getLogger(ApiResource.class);

  private final RouteConfig apiResourceCfg = new RouteConfig();
  private final ApiUtil apiUtil = new ApiUtil();

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
    return apiResourceCfg.getConfig();
  }

  /**
   * <code>
  
    curl -d "@example.ttl" -H "Content-Type: application/x-turtle" http://0.0.0.0:4444/fox/
  
    </code>
   */
  @Consumes("application/x-turtle")
  @Produces("application/x-turtle")
  @POST
  public String postApiNif(@Context final Request request, @Context final HttpHeaders hh) {

    LOG.info("HttpHeaders");
    hh.getRequestHeaders().entrySet().forEach(LOG::info);

    String nifDocument = "";
    try {
      // read request
      final InputStream in = request.getInputStream();
      List<Document> docs = null;
      try {
        docs = apiUtil.parseNIF(in);
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      } finally {
        in.close();
      }

      // annotate each doc
      if (docs != null) {
        final List<Document> annotetedDocs = new ArrayList<>();
        for (final Document document : docs) {
          // send request to fox
          final Map<String, String> parameter = new HashMap<>();
          parameter.put(FoxParameter.Parameter.INPUT.toString(), document.getText());
          parameter.put(FoxParameter.Parameter.TYPE.toString(), FoxParameter.Type.TEXT.toString());
          parameter.put(FoxParameter.Parameter.LANG.toString(), FoxParameter.Langs.EN.toString());
          parameter.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
          parameter.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());

          final String response = requestFox(parameter);
          LOG.info(response);
          // parse fox response
          if ((response != null) && !response.isEmpty()) {

            final JSONObject outObj = new JSONObject(response);
            if (outObj.has("@graph")) {
              final JSONArray graph = outObj.getJSONArray("@graph");
              for (int i = 0; i < graph.length(); i++) {
                parseType(graph.getJSONObject(i), document);
              }
            } else {
              parseType(outObj, document);
            }
          }
          annotetedDocs.add(document);
        }
        nifDocument = apiUtil.writeNIF(annotetedDocs);
      }
    } catch (final Exception e) {
      LOG.error(e.getStackTrace(), e);
    }

    return nifDocument;
  }

  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_XML)
  @POST
  public String postApi(final JsonObject properties, @Context final Request request,
      @Context final HttpHeaders hh, @Context final UriInfo ui) {

    String output = "";

    final Map<String, String> parameter = new HashMap<>();
    try {
      parameter.put(FoxParameter.Parameter.INPUT.toString(),
          properties.getString(FoxParameter.Parameter.INPUT.toString()));
      parameter.put(FoxParameter.Parameter.TYPE.toString(),
          properties.getString(FoxParameter.Parameter.TYPE.toString()));
      parameter.put(FoxParameter.Parameter.TASK.toString(),
          properties.getString(FoxParameter.Parameter.TASK.toString()));
      parameter.put(FoxParameter.Parameter.OUTPUT.toString(),
          properties.getString(FoxParameter.Parameter.OUTPUT.toString()));
      if (properties.get(FoxParameter.Parameter.LINKING.toString()) != null) {
        parameter.put(FoxParameter.Parameter.LINKING.toString(),
            properties.getString(FoxParameter.Parameter.LINKING.toString()));
      }

      // get input data
      switch (parameter.get(FoxParameter.Parameter.TYPE.toString()).toLowerCase()) {
        case "url":
          parameter.put(FoxParameter.Parameter.INPUT.toString(),
              FoxTextUtil.urlToText(parameter.get(FoxParameter.Parameter.INPUT.toString())));
          break;
        case "text":
          parameter.put(FoxParameter.Parameter.INPUT.toString(),
              FoxTextUtil.htmlToText(parameter.get(FoxParameter.Parameter.INPUT.toString())));
          break;
      }

      // lang
      String lang = null;
      try {
        if (properties.get(FoxParameter.Parameter.LANG.toString()) != null) {
          lang = properties.getString(FoxParameter.Parameter.LANG.toString());
        }

        FoxParameter.Langs l = FoxParameter.Langs.fromString(lang);
        if (l == null) {

          l = apiUtil.detectLanguage(parameter.get(FoxParameter.Parameter.INPUT.toString()));
          LOG.info("lang" + lang);
          if (l != null) {
            lang = l.toString();
          } else {
            lang = "";
          }
        }
        parameter.put(FoxParameter.Parameter.LANG.toString(), lang);
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
      LOG.info("parameter");
      LOG.info(parameter);
    }
    return output;
  }

  protected String requestFox(final Map<String, String> parameter) {
    String output = "";
    // get a fox instance
    IFox fox = Server.pool.get(parameter.get(FoxParameter.Parameter.LANG.toString())).poll();

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
      LOG.error("input: " + parameter.get(FoxParameter.Parameter.INPUT.toString()));
    }

    // shutdown thread
    fiber.dispose();

    if (latch.getCount() == 0) {
      output = fox.getResultsAndClean();
      Server.pool.get(parameter.get(FoxParameter.Parameter.LANG.toString())).push(fox);
    } else {
      fox = null;
      Server.pool.get(parameter.get(FoxParameter.Parameter.LANG.toString())).add();
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
