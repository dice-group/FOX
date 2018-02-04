package org.aksw.fox.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.legacy.Server;
import org.aksw.fox.utils.FoxConst;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;

@Deprecated
public class TestServer {

  public final static Logger LOG = LogManager.getLogger(TestServer.class);

  private Server server = null;

  long waitingTime = 10 * 60 * 1000; // 5m

  public void serverTest() {
    // start server
    final long startTime = System.currentTimeMillis();
    new Thread(() -> {
      try {
        server = new Server();
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      Assert.assertTrue(server.start());
    }).start();

    // wait till server is up
    while (!Server.running && ((System.currentTimeMillis() - startTime) < waitingTime)) {
      try {
        Thread.sleep(1000);
      } catch (final InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }

    // info
    if (Server.running) {
      LOG.info("server started");

      apiTest();
      demoTest();

      // stop server
      final boolean down = server.stop();

      LOG.info(down ? "Server stopped!" : "Could't stop the server.");
    } else {
      LOG.warn("Server start toke too long");
    }
  }

  /**
   * HTTP GET test
   */
  public void apiTest() {
    LOG.info("apiTest ...");

    try {
      final Charset UTF_8 = Charset.forName("UTF-8");
      final String url = "http://" + server.host + ":" + server.port + "/fox";
      LOG.info("url: " + url);

      final Response response = Request.Post(url)
          .addHeader("Content-type", "application/json;charset=".concat(UTF_8.toString()))
          .addHeader("Accept-Charset",
              UTF_8
                  .toString())
          .body(
              new StringEntity(
                  new JSONObject()
                      .put(FoxParameter.Parameter.TYPE.toString(),
                          FoxParameter.Type.TEXT.toString())
                      .put(FoxParameter.Parameter.LANG.toString(), FoxParameter.Langs.EN.toString())
                      .put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString())
                      .put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName())
                      .put(FoxParameter.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1)
                      .toString(),
                  ContentType.APPLICATION_JSON))
          .execute();

      final HttpResponse httpResponse = response.returnResponse();
      LOG.info(httpResponse.getStatusLine().toString());
      final HttpEntity entry = httpResponse.getEntity();
      final String r = IOUtils.toString(entry.getContent(), UTF_8);
      EntityUtils.consume(entry);
      LOG.info(r);
      Assert.assertNotNull(r);
      Assert.assertFalse(r.isEmpty());

    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * HTTP GET test
   */
  public void demoTest() {

    final String path = "http://" + server.host + ":" + server.port;
    final Map<String, Integer> urls = new HashMap<>();

    urls.put(path, 200);
    urls.put(path + "/demo/index.html", 200);
    urls.put(path + "/public/img/errors.png", 200);
    // TODO: check all paths
    /**
     * <code>
    urls.put(path + "/call/ner/entities", 405);
    urls.put(path +      "/call/ner/entities/", 405);
    urls.put(path + "/api", 405);
    urls.put(path + "/api/", 405);
    urls.put(path + "/api/ner/feedback/", 405);
    urls.put(path + "/api/ner/feedback", 405);
       </code>
     */

    try {
      for (final Iterator<Entry<String, Integer>> iterator = urls.entrySet().iterator(); iterator
          .hasNext();) {
        final Entry<String, Integer> url = iterator.next();
        LOG.info(url);
        final HttpURLConnection con = (HttpURLConnection) new URL(url.getKey()).openConnection();
        Assert.assertEquals(url.getValue().intValue(), con.getResponseCode());
      }
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }
}
