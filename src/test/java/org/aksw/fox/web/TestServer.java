package org.aksw.fox.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class TestServer {

    public final static Logger LOG         = LogManager.getLogger(TestServer.class);

    private Server             server      = null;
    private int                port        = 4444;

    long                       waitingTime = 5 * 60 * 1000;                         // ms

    @Test
    public void serverTest() {

        // fox.properties need to be available
        Assert.assertTrue(FoxCfg.loadFile("fox.properties-dist"));
        Assert.assertNotNull(FoxCfg.get(Server.CFG_KEY_POOL_SIZE));

        // start server
        long startTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            public void run() {
                try {
                    server = new Server(port);
                } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
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
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.info("server started in " + (elapsedTime / 1000L) + "s");
        } else {
            LOG.warn("Server start toke too long (" + (waitingTime / 1000L) + "s)!");
        }

        demoTest();

        // stop server
        server.stop();
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

        /* TODO: endpoint paths
        urls.put(path + "/call/ner/entities", 405);
        urls.put(path + "/call/ner/entities/", 405);
        urls.put(path + "/api", 405);
        urls.put(path + "/api/", 405);
        urls.put(path + "/api/ner/feedback/", 405);
        urls.put(path + "/api/ner/feedback", 405);
        */

        try {
            for (Iterator<Entry<String, Integer>> iterator = urls.entrySet().iterator(); iterator.hasNext();) {
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
