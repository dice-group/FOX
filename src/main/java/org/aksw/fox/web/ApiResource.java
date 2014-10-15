package org.aksw.fox.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.aksw.fox.IFox;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

@Path("ner")
public class ApiResource {

    public static Logger LOG = LogManager.getLogger(ApiResource.class);

    // http://0.0.0.0:8080/api/entities
    @Path("entities")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public String postApi(final JsonObject properties, @Context Request request, @Context HttpHeaders hh, @Context UriInfo ui) {

        LOG.info(properties);

        Map<String, String> parameter = new HashMap<>();
        parameter.put(FoxCfg.parameter_input, properties.getString(FoxCfg.parameter_input));
        parameter.put(FoxCfg.parameter_type, properties.getString(FoxCfg.parameter_type));
        parameter.put(FoxCfg.parameter_task, properties.getString(FoxCfg.parameter_task));
        parameter.put(FoxCfg.parameter_output, properties.getString(FoxCfg.parameter_output));

        // get a fox instance
        IFox fox = Server.pool.poll();

        // init. thread
        Fiber fiber = new ThreadFiber();
        fiber.start();
        final CountDownLatch latch = new CountDownLatch(1);

        // get input data
        switch (parameter.get(FoxCfg.parameter_type).toLowerCase()) {

        case "url":
            parameter.put(FoxCfg.parameter_input, FoxTextUtil.urlToText(parameter.get(FoxCfg.parameter_input)));
            break;

        case "text":
            parameter.put(FoxCfg.parameter_input, FoxTextUtil.htmlToText(parameter.get(FoxCfg.parameter_input)));
            break;
        }

        // set up fox
        fox.setCountDownLatch(latch);
        fox.setParameter(parameter);

        // run fox
        fiber.execute(fox);

        // wait 5min or till the fox instance is finished
        try {
            latch.await(Integer.parseInt(FoxCfg.get(FoxHttpHandler.CFG_KEY_FOX_LIFETIME)), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Fox timeout after " + FoxCfg.get(FoxHttpHandler.CFG_KEY_FOX_LIFETIME) + "min.");
            LOG.error("\n", e);
            LOG.error("input: " + parameter.get(FoxCfg.parameter_input));
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

        LOG.info(ui.getRequestUri());
        LOG.info(hh.getRequestHeaders());
        LOG.info("IP: " + request.getRemoteAddr());
        /*}else {
           LOG.debug("wrong parameters");
           try {
               response.sendError(HttpURLConnection.HTTP_BAD_REQUEST);
           } catch (IOException e) {
               LOG.error("\n", e);
           }
           response.finish();
        }*/
        return output;
    }

    // http://0.0.0.0:8080/api/entitiesFeedback
    @Path("entitiesFeedback")
    @POST
    public JsonObject postText() {

        LOG.info("FEEDBACK");
        return null;
    }

    public static String getPath() {
        return "/call/";
    }
}