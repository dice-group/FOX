package org.aksw.fox.web;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
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
public class Api extends AbstractApi {

    protected Pool pool = new Pool(Integer.parseInt(FoxCfg.get("poolCount")));

    /**
     * 
     */
    @Override
    protected void service(Request request, Response response, Map<String, String> parameter) {

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

            try {
                in = encodeURLComponent(parameter.get("input"));
                out = encodeURLComponent(output);
                log = encodeURLComponent(fox.getLog());
            } catch (UnsupportedEncodingException e) {
                logger.error("\n", e);
            }
        }

        setResponse(response, "[{\"input\" : \" " + in + "\" , \"output\" : \"" + out + "\", \"log\" : \"" + log + "\" }]", HttpURLConnection.HTTP_OK, "text/plain");
    }

    public String encodeURLComponent(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!").replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
    }
}
