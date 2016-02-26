package org.aksw.fox.web;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.Fox;
import org.aksw.fox.IFox;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxJena;
import org.aksw.fox.utils.FoxLanguageDetector;
import org.aksw.fox.utils.FoxStringUtil;
import org.aksw.fox.utils.FoxTextUtil;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;
import org.json.JSONObject;

/**
 *
 * @author rspeck
 *
 */
public class FoxHttpHandler extends AbstractFoxHttpHandler {

  public static final String CFG_KEY_FOX_LIFETIME =
      FoxHttpHandler.class.getName().concat(".lifetime");
  FoxLanguageDetector languageDetector = new FoxLanguageDetector();

  @Override
  protected void postService(final Request request, final Response response,
      final Map<String, String> parameter) {

    String errormessage = "";
    switch (parameter.get(Fox.Parameter.TYPE.toString()).toString()) {
      case "url":
        parameter.put(Fox.Parameter.INPUT.toString(),
            FoxTextUtil.urlToText(parameter.get(Fox.Parameter.INPUT.toString())));
        break;
      case "text":
        parameter.put(Fox.Parameter.INPUT.toString(),
            FoxTextUtil.htmlToText(parameter.get(Fox.Parameter.INPUT.toString())));
        break;
    }

    String lang = parameter.get(Fox.Parameter.LANG.toString());
    Fox.Langs l = Fox.Langs.fromString(lang);
    if (l == null) {
      l = languageDetector.detect(parameter.get(Fox.Parameter.INPUT.toString()));
      if (l != null) {
        lang = l.toString();
      } else {
        lang = "";
      }
    }
    LOG.info("lang: " + lang);
    if (!lang.isEmpty()) {

      // get a fox instance
      final Pool<IFox> pool = Server.pool.get(lang);
      IFox fox = null;
      if (pool != null) {
        fox = pool.poll();
      }

      if (fox != null) {

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
          LOG.error("input: " + parameter.get(Fox.Parameter.INPUT.toString()));
        }

        // shutdown thread
        fiber.dispose();

        // get output
        String output = "";
        if (latch.getCount() == 0) {
          output = fox.getResults();
          Server.pool.get(lang).push(fox);
        } else {
          fox = null;
          Server.pool.get(lang).add();
        }

        String in = null, out = null, log = null;
        if (fox != null) {
          in = FoxStringUtil.encodeURLComponent(parameter.get(Fox.Parameter.INPUT.toString()));
          out = FoxStringUtil.encodeURLComponent(output);
          log = FoxStringUtil.encodeURLComponent(fox.getLog());
        }
        setResponse(response,
            new JSONObject().put("input", in == null ? "" : in)
                .put("output", out == null ? "" : out).put("log", log == null ? "" : log)
                .toString(),
            HttpURLConnection.HTTP_OK, "application/json");
        return;
      } else {
        errormessage = "Could not found fox from pool with lang: " + lang;
        LOG.warn(errormessage);
      }
    }

    setResponse(response, new JSONObject().put("error", errormessage).toString(),
        HttpURLConnection.HTTP_BAD_REQUEST, "application/json");
    return;
  }

  @Override
  protected boolean checkParameter(final Map<String, String> formData) {

    LOG.info("checking form parameter ...");

    final String type = formData.get(Fox.Parameter.TYPE.toString());
    if ((type == null) || !(type.equalsIgnoreCase(Fox.Type.URL.toString())
        || type.equalsIgnoreCase(Fox.Type.TEXT.toString()))) {
      return false;
    }

    final String text = formData.get(Fox.Parameter.INPUT.toString());
    if ((text == null) || text.trim().isEmpty()) {
      return false;
    }

    final String task = formData.get(Fox.Parameter.TASK.toString());
    if ((task == null) || !(task.equalsIgnoreCase("ke") || task.equalsIgnoreCase("ner")
        || task.equalsIgnoreCase("keandner") || task.equalsIgnoreCase("re")
        || task.equalsIgnoreCase("all"))) {
      return false;
    }

    final String output = formData.get(Fox.Parameter.OUTPUT.toString());

    if (!FoxJena.prints.contains(output)) {
      return false;
    }

    final String nif = formData.get(Fox.Parameter.NIF.toString());
    if ((nif == null) || !nif.equalsIgnoreCase("true")) {
      formData.put(Fox.Parameter.NIF.toString(), "false");
    } else {
      formData.put(Fox.Parameter.NIF.toString(), "true");
    }

    final String foxlight = formData.get(Fox.Parameter.FOXLIGHT.toString());
    if ((foxlight == null) || foxlight.equalsIgnoreCase("off")) {
      formData.put(Fox.Parameter.FOXLIGHT.toString(), "OFF");
    }

    LOG.info("ok.");
    return true;
  }

  @Override
  public List<String> getMappings() {
    return Arrays.asList("/api");
  }
}
