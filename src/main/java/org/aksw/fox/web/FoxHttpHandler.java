package org.aksw.fox.web;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.IFox;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxJena;
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

    public static final String CFG_KEY_FOX_LIFETIME = FoxHttpHandler.class.getName().concat(".lifetime");

    @Override
    protected void postService(Request request, Response response, Map<String, String> parameter) {

        // get a fox instance
        IFox fox = Server.pool.poll();

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
            latch.await(Integer.parseInt(FoxCfg.get(CFG_KEY_FOX_LIFETIME)), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Fox timeout after " + FoxCfg.get(CFG_KEY_FOX_LIFETIME) + "min.");
            LOG.error("\n", e);
            LOG.error("input: " + parameter.get("input"));
        }

        // shutdown thread
        fiber.dispose();

        // get output
        String output = "";
        if (latch.getCount() == 0) {
            output = fox.getResults();
            Server.pool.push(fox);
        } else {
            fox = null;
            Server.pool.add();
            // TODO : error output
        }

        String in = null, out = null, log = null;
        if (fox != null) {
            in = FoxStringUtil.encodeURLComponent(parameter.get("input"));
            out = FoxStringUtil.encodeURLComponent(output);
            log = FoxStringUtil.encodeURLComponent(fox.getLog());
        }
        // set response
        setResponse(
                response,
                new JSONObject()
                        .put("input", in == null ? "" : in)
                        .put("output", out == null ? "" : out)
                        .put("log", log == null ? "" : log)
                        .toString(),
                HttpURLConnection.HTTP_OK,
                "application/json");
    }

    @Override
    protected boolean checkParameter(Map<String, String> formData) {

        LOG.info("checking form parameter ...");

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

        if (!FoxJena.prints.contains(output))
            return false;

        String nif = formData.get("nif");
        if (nif == null || !nif.equalsIgnoreCase("true"))
            formData.put("nif", "false");
        else
            formData.put("nif", "true");

        String foxlight = formData.get("foxlight");
        if (foxlight == null) {
            formData.put("foxlight", "OFF");
        }

        LOG.info("ok.");
        return true;
    }

    @Override
    public List<String> getMappings() {
        return Arrays.asList("/api");
    }
}
