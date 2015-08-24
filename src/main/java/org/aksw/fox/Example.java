package org.aksw.fox;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.tools.ner.en.StanfordEN;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.aksw.fox.web.Server;
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

public class Example {
    static {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    }
    public final static Logger LOG         = LogManager.getLogger(Example.class);

    private static Server      server      = null;
    private static long        waitingTime = 10 * 60 * 1000;                     // 10m

    public static void main(String[] args) {
        // programmatic();
        webAPI();
    }

    /**
     * Example programmatic use of FOX.
     */
    public static void programmatic() {
        LOG.info("programmatic ...");

        String lang = Fox.Langs.EN.toString();
        LOG.info(lang);
        LOG.info(ToolsGenerator.usedLang);
        if (!ToolsGenerator.usedLang.contains(lang))
            LOG.warn("language not supported");
        else {
            Fox fox = new Fox(lang);

            Map<String, String> defaults = fox.getDefaultParameter();

            defaults.put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString());
            // defaults.put(Fox.Parameter.LANG.toString(),
            // Fox.Langs.EN.toString());
            defaults.put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString());
            defaults.put(Fox.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
            defaults.put(Fox.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1);
            fox.setParameter(defaults);

            // fox light version
            String tool = StanfordEN.class.getName();
            Set<Entity> e;
            if (!ToolsGenerator.nerTools.get(lang).contains(tool))
                LOG.warn("can't find the given tool " + tool);
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
    private static void _webAPI() {
        LOG.info("webAPI ...");

        try {
            String url = "http://" + server.host + ":" + server.port;
            final Charset UTF_8 = Charset.forName("UTF-8");
            JSONObject jo = new JSONObject()
                    .put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString())
                    // .put(Fox.Parameter.LANG.toString(),
                    // Fox.Langs.EN.toString())
                    .put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString())
                    .put(Fox.Parameter.OUTPUT.toString(), Lang.TURTLE.getName())
                    .put(Fox.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1);

            LOG.info("Parameter: " + jo);

            Response response = Request
                    .Post(url.concat("/call/ner/entities"))
                    .addHeader("Content-type", "application/json;charset=".concat(UTF_8.toString()))
                    .addHeader("Accept-Charset", UTF_8.toString())
                    .body(
                            new StringEntity(
                                    jo.toString(), ContentType.APPLICATION_JSON
                            )
                    )
                    .execute();

            HttpResponse httpResponse = response.returnResponse();
            LOG.info(httpResponse.getStatusLine().toString());

            HttpEntity entry = httpResponse.getEntity();
            String r = IOUtils.toString(entry.getContent(), UTF_8);
            EntityUtils.consume(entry);

            LOG.info(r);

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private static void webAPI() {
        // start server
        long startTime = System.currentTimeMillis();
        new Thread(new Runnable() {
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
            _webAPI();
            // stop server
            server.stop();
        } else {
            LOG.warn("Server start toke too long");
        }
    }

}
