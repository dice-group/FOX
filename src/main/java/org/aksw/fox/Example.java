package org.aksw.fox;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.tools.ner.en.StanfordEN;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.aksw.fox.web.Server;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;
import org.junit.Assert;

import de.renespeck.swissknife.http.Requests;

public class Example {
    static {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    }
    public final static Logger LOG         = LogManager.getLogger(Example.class);

    private static Server      server      = null;
    private static long        waitingTime = 10 * 60 * 1000;                     // 10m

    public static void main(String[] args) {
        programmatic();
        // webAPI();
        // _webAPI("http://fox-demo.aksw.org");
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
    private static void _webAPI(String url) {
        LOG.info("webAPI ...");

        try {
            String r = Requests.postJson(url.concat("/call/ner/entities"),
                    new JSONObject()
                            .put(Fox.Parameter.TYPE.toString(), Fox.Type.TEXT.toString())
                            /*
                            .put(Fox.Parameter.LANG.toString(), Fox.Langs.EN.toString())
                            */
                            .put(Fox.Parameter.TASK.toString(), Fox.Task.NER.toString())
                            .put(Fox.Parameter.OUTPUT.toString(), Lang.TURTLE.getName())
                            .put(Fox.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1)
                    );
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
            String url = "http://" + server.host + ":" + server.port;
            _webAPI(url);
            // stop server
            server.stop();
        } else {
            LOG.warn("Server start toke too long");
        }
    }

}
