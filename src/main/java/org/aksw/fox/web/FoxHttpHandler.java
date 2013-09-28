package org.aksw.fox.web;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.InterfaceRunnableFox;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxTextUtil;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

/**
 * 
 * @author rspeck
 * 
 */
public class FoxHttpHandler extends AbstractFoxHttpHandler {

    protected Pool pool = new Pool(Integer.parseInt(FoxCfg.get("poolCount")));

    /**
     * 
     */
    @Override
    protected void postService(Request request, Response response, Map<String, String> parameter) {

        // get a fox instance
        InterfaceRunnableFox fox = pool.poll();

        // init. thread
        Fiber fiber = new ThreadFiber();
        fiber.start();
        final CountDownLatch latch = new CountDownLatch(1);

        // get input data
        switch (parameter.get("type").toLowerCase()) {

        case "url":
            parameter.put("input", FoxTextUtil.urlToText(parameter.get("input")));
            break;

        case "text":
            parameter.put("input", FoxTextUtil.htmlToText(parameter.get("input")));
            break;
        }

        // set up fox
        fox.setCountDownLatch(latch);
        fox.setParameter(parameter);

        // run fox
        fiber.execute(fox);

        // wait 5min or till the fox instance is finished
        try {
            latch.await(Integer.parseInt(FoxCfg.get("foxLifeTime")), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Fox timeout after " + FoxCfg.get("foxLifeTime") + "min.");
            logger.error("\n", e);
            logger.error("input: " + parameter.get("input"));
        }

        // shutdown thread
        fiber.dispose();

        // get output
        String output = "";
        if (latch.getCount() == 0) {

            output = fox.getResults();
            pool.push(fox);

        } else {

            fox = null;
            pool.add();
            // TODO
            // output
        }
        String in = null, out = null, log = null;
        if (fox != null) {
            // set response
            in = encodeURLComponent(parameter.get("input"));
            out = encodeURLComponent(output);
            log = encodeURLComponent(fox.getLog());
        }
        setResponse(response, "[{\"input\" : \" " + in + "\" , \"output\" : \"" + out + "\", \"log\" : \"" + log + "\" }]", HttpURLConnection.HTTP_OK, "text/plain");
    }

    /**
     * Checks parameter.
     * <p>
     * type: url | text<br>
     * task: ke | ner | keandner | re | all<br>
     * output: rdf | turtle | html<br>
     * nif: true : false<br>
     * input : plain text | url
     * </p>
     */
    @Override
    protected boolean checkParameter(Map<String, String> formData) {

        logger.info("checking form parameter ...");

        String type = formData.get("type");
        if (type == null || !(type.equalsIgnoreCase("url") || type.equalsIgnoreCase("text")))
            return false;

        String text = formData.get("input");
        if (text == null || text.trim().isEmpty())
            return false;

        String task = formData.get("task");
        if (task == null || !(task.equalsIgnoreCase("ke") || task.equalsIgnoreCase("ner") || task.equalsIgnoreCase("keandner") || task.equalsIgnoreCase("re") || task.equalsIgnoreCase("all")))
            return false;

        String output = formData.get("output");

        if (!output.equalsIgnoreCase("JSONLD") && !output.equalsIgnoreCase("RDF/JSON") && !output.equalsIgnoreCase("RDF/XML") && !output.equalsIgnoreCase("RDF/XML-ABBREV") && !output.equalsIgnoreCase("TURTLE") && !output.equalsIgnoreCase("N-TRIPLE") && !output.equalsIgnoreCase("N3"))
            return false;

        String nif = formData.get("nif");
        if (nif == null || !nif.equalsIgnoreCase("true"))
            formData.put("nif", "false");
        else
            formData.put("nif", "true");

        String foxlight = formData.get("foxlight");
        if (foxlight == null || !foxlight.equalsIgnoreCase("true"))
            formData.put("foxlight", "false");
        else
            formData.put("foxlight", "true");

        logger.info("ok.");
        return true;
    }

    public String encodeURLComponent(String in) {
        // try {
        // return new
        // ScriptEngineManager().getEngineByName("JavaScript").eval("encodeURIComponent('"
        // + in + "')").toString();
        // } catch (ScriptException e) {
        // logger.error("\n", e);
        // return "";
        // }
        try {
            return URLEncoder.encode(in, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!").replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            logger.error("\n", e);
            return "";
        }
    }

    @Override
    public List<String> getMappings() {
        List<String> l = new ArrayList<>();
        l.add("/api");
        return l;
    }
}
