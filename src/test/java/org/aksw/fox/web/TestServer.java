package org.aksw.fox.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.Fox;
import org.aksw.fox.utils.FoxCfg;
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
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestServer {
  static {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);
  }

  public final static Logger LOG = LogManager.getLogger(TestServer.class);

  private Server server = null;

  long waitingTime = 10 * 60 * 1000; // 5m

  @Test
  public void serverTest() {
    // start server
    long startTime = System.currentTimeMillis();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          server = new Server();
        } catch (Exception e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
        Assert.assertTrue(server.start());
      }
    }).start();

    // wait till server is up
    while (!Server.running && (System.currentTimeMillis() - startTime) < waitingTime) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }

    // info
    if (Server.running) {
      LOG.info("server started");

      apiTest();
      demoTest();

      // stop server
      server.stop();
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
      final String url = "http://" + server.host + ":" + server.port + "/call/ner/entities";
      LOG.info("url: " + url);
       
      Response response = Request.Post(url)
          .addHeader("Content-type", "application/json;charset=".concat(UTF_8.toString()))
          .addHeader("Accept-Charset", UTF_8.toString())
          .body(new StringEntity(
              new JSONObject().put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString())
                  .put(Fox.Parameter.LANG.toString(), Fox.Langs.EN.toString())
                  .put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString())
                  .put(Fox.Parameter.OUTPUT.toString(), Lang.TURTLE.getName())
                  .put(Fox.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1).toString(),
              ContentType.APPLICATION_JSON))
          .execute();

      HttpResponse httpResponse = response.returnResponse();
      LOG.info(httpResponse.getStatusLine().toString());
      HttpEntity entry = httpResponse.getEntity();
      String r = IOUtils.toString(entry.getContent(), UTF_8);
      EntityUtils.consume(entry);
      LOG.info(r);
      Assert.assertNotNull(r);
      Assert.assertFalse(r.isEmpty());

    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * HTTP GET test
   */
  public void demoTest() {

    String path = "http://" + server.host + ":" + server.port;
    Map<String, Integer> urls = new HashMap<>();

    urls.put(path, 200);
    urls.put(path + "/demo/index.html", 200);
    urls.put(path + "/public/img/errors.png", 200);

    /*
     * TODO: endpoint paths urls.put(path + "/call/ner/entities", 405); urls.put(path +
     * "/call/ner/entities/", 405); urls.put(path + "/api", 405); urls.put(path + "/api/", 405);
     * urls.put(path + "/api/ner/feedback/", 405); urls.put(path + "/api/ner/feedback", 405);
     */

    try {
      for (Iterator<Entry<String, Integer>> iterator = urls.entrySet().iterator(); iterator
          .hasNext();) {
        Entry<String, Integer> url = iterator.next();
        LOG.info(url);
        HttpURLConnection con = (HttpURLConnection) new URL(url.getKey()).openConnection();
        Assert.assertEquals(url.getValue().intValue(), con.getResponseCode());
      }
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }
}
