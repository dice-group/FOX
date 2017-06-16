package org.aksw.fox.webservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.fox.Fox;
import org.aksw.fox.FoxParameter;
import org.aksw.fox.IFox;
import org.aksw.fox.data.exception.PortInUseException;
import org.aksw.fox.output.FoxJenaNew;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.web.api.ApiUtil;
import org.aksw.fox.webservice.util.Pool;
import org.aksw.fox.webservice.util.RouteConfig;
import org.aksw.gerbil.transfer.nif.Document;
import org.apache.jena.riot.Lang;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.sparql.lib.org.json.JSONObject;

import spark.Spark;

public class FoxServer extends AServer {

  final String turtleContentType = "application/x-turtle";
  final String jsonContentType = "application/json";

  protected final RouteConfig routeConfig = new RouteConfig();
  protected final ApiUtil apiUtil = new ApiUtil();

  public static Map<String, Pool<IFox>> pool = null;
  static {
    try {
      initPools();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   *
   * Constructor.
   *
   * @throws PortInUseException
   */
  public FoxServer() throws PortInUseException {
    super();
    Spark.staticFileLocation("/public/demo");
  }

  protected static void initPools() throws Exception {
    pool = new HashMap<>();
    for (final String lang : ToolsGenerator.usedLang) {
      int poolsize = CFG.getInt(CFG_KEY_POOL_SIZE.concat("[@").concat(lang).concat("]"));
      if (poolsize < 1) {
        LOG.error(
            "Could not find pool size for the given lang" + lang + ". We use a poolsize of 1.");
        poolsize = 1;
      }
      pool.put(lang, new Pool<IFox>(Fox.class.getName(), lang, poolsize));
    }
  }

  protected Set<String> allowedHeaderFields() {
    return new HashSet<>(Arrays.asList(//
        FoxParameter.Parameter.TYPE.toString(), //
        FoxParameter.Parameter.INPUT.toString(), //
        FoxParameter.Parameter.TASK.toString(), //
        FoxParameter.Parameter.OUTPUT.toString()//
    ));
  }

  protected Map<String, String> defaultParameter() {
    final Map<String, String> parameter = new HashMap<>();
    parameter.put(FoxParameter.Parameter.TYPE.toString(), FoxParameter.Type.TEXT.toString());
    parameter.put(FoxParameter.Parameter.LANG.toString(), FoxParameter.Langs.EN.toString());
    parameter.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
    parameter.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
    return parameter;
  }

  @Override
  public void mapRoutes() {

    /**
     * path: config <br>
     * method: GET <br>
     * <code>
                    curl http://0.0.0.0:9090/config
    </code>
     */
    Spark.get("/config", (req, res) -> {
      res.type(jsonContentType.concat(";charset=utf-8"));
      return routeConfig.getConfig();
    });
    /**
     * path: fox <br>
     * method: POST <br>
     * Content-Type: application/json <br>
     *
     * <code>
                  curl -X POST -H "task:asdasd" -H "Content-Type:application/json" http://0.0.0.0:9090/fox
                  curl -X POST -H "task:asdasd" -H "Content-Type:application/x-turtle" http://0.0.0.0:9090/fox
    </code>
     */
    Spark.post("/fox", (req, res) -> {

      String errorMessage = "";
      // checks content type
      final String ct = req.contentType();
      LOG.info("ContentType: " + ct);

      Map<String, String> parameter = defaultParameter();

      // JSON
      if ((ct != null) && (ct.indexOf(jsonContentType) != -1)) {

        final JSONObject jo = new JSONObject(req.body());
        final Map<String, Object> d = new ObjectMapper().readValue(jo.toString(), HashMap.class);

        parameter = d.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, //
            e -> String.valueOf(e.getValue())//
        ));

        final FoxJenaNew foxJenaNew = new FoxJenaNew();
        foxJenaNew.addInput(parameter.get(FoxParameter.Parameter.INPUT.toString()), null);
        parameter.put(FoxParameter.Parameter.INPUT.toString(), foxJenaNew.print());

      } else

      // TURTLE
      if ((ct != null) && (ct.indexOf(turtleContentType) != -1)) {

        // read header fields
        final Set<String> headerfields = req.headers();

        // set parameter
        String field = FoxParameter.Parameter.TASK.toString();
        if (headerfields.contains(field)) {
          final String value = req.headers(field);
          parameter.put(field, value);
        }
        field = FoxParameter.Parameter.LANG.toString();
        if (headerfields.contains(field)) {
        }

        // add input
        parameter.put(FoxParameter.Parameter.INPUT.toString(), req.body());

      } // ESLE
      else {
        errorMessage = "Use a supported Content-Type please.";
        Spark.halt(415, errorMessage);
      }

      // parse input
      List<Document> docs = null;
      try {
        docs = apiUtil.parseNIF(parameter.get(FoxParameter.Parameter.INPUT.toString()));
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
        errorMessage = "Could not parse the request body.";
        LOG.warn(errorMessage);
      }

      // send fox request
      if (docs != null) {
        LOG.info("nif doc size: " + docs.size());

        LOG.info(docs);
        LOG.info(parameter);
        // request
        final String foxResponse = fox(docs, parameter);

        // create server response
        res.body(foxResponse);
        res.type(turtleContentType.concat(";charset=utf-8"));
      }

      return res.body();
    });

  }

  public String fox(final List<Document> docs, final Map<String, String> parameter) {

    LOG.info("fox");
    String nif = "";
    // annotate each doc
    if (docs != null) {

      final String lang = parameter.get(FoxParameter.Parameter.LANG.toString());
      LOG.info("lang: " + lang);
      // get a fox instance
      final Pool<IFox> pool = FoxServer.pool.get(lang);
      IFox fox = null;
      if (pool != null) {
        fox = pool.poll();
      } else {
        LOG.warn("Couldn't find a fox instance for the given language!!!!!!!");
      }

      boolean done = false;
      for (final Document document : docs) {
        final String uri = document.getDocumentURI();
        final String text = document.getText();

        parameter.put(FoxParameter.Parameter.INPUT.toString(), text);
        // TODO: add to parameter?
        parameter.put("docuri", uri);

        done = callFox(fox, parameter);
      }

      if (done) {
        nif = fox.getResultsAndClean();
        FoxServer.pool.get(lang).push(fox);
      } else {
        fox = null;
        FoxServer.pool.get(lang).add();
      }
    }
    return nif;
  }

  protected boolean callFox(final IFox fox, final Map<String, String> parameter) {
    boolean done = false;

    if (fox != null) {
      LOG.info("start");
      // init. thread
      final Fiber fiber = new ThreadFiber();
      fiber.start();
      final CountDownLatch latch = new CountDownLatch(1);
      fox.setCountDownLatch(latch);
      fox.setParameter(parameter);
      fiber.execute(fox);

      // wait
      try {
        latch.await(Integer.parseInt(FoxCfg.get(CFG_KEY_FOX_LIFETIME)), TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        LOG.error("Fox timeout after " + FoxCfg.get(CFG_KEY_FOX_LIFETIME) + "min.");
        LOG.error("\n", e);
        LOG.error("input: " + parameter.get(FoxParameter.Parameter.INPUT.toString()));
      }

      // shutdown thread
      fiber.dispose();

      if (latch.getCount() == 0) {
        // LOG.debug("fox results:" + fox.getResults());
        // LOG.debug("fox lang:" + fox.getLang());
        // LOG.debug("fox log:" + fox.getLog());

        done = true;
      }
    }
    return done;
  }
}
