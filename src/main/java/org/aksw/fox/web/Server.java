package org.aksw.fox.web;

import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.feedback.FeedbackHttpHandler;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

/**
 * 
 * @author rspeck
 * 
 */
public class Server {

    public final static Logger LOG                  = Logger.getLogger(Server.class);
    public final static String LISTENER_NAME        = "FoxNetworkListener";

    /* Cfg file key. */
    public final static String DEMO_HANDLER_KEY     = "demo";
    /* Cfg file key. */
    public final static String API_HANDLER_KEY      = "api";
    /* Cfg file key. */
    public final static String FEEDBACK_HANDLER_KEY = "feedback";

    protected final HttpServer server               = new HttpServer();

    /**
     * Adds HttpHandler.
     * 
     * @param port
     *            the servers port
     */
    public Server(int port) {
        server.getServerConfiguration().setDefaultErrorPageGenerator(new ErrorPages());
        server.addListener(new NetworkListener(LISTENER_NAME, NetworkListener.DEFAULT_NETWORK_HOST, port));

        String state = null;
        // error page data
        {
            LOG.info("Adds error page data handler ...");
            StaticHttpHandler shl = new StaticHttpHandler("public");
            shl.setFileCacheEnabled(true);
            server.getServerConfiguration().addHttpHandler(shl, "/public");
        }

        // demoHttpHandler
        state = FoxCfg.get(DEMO_HANDLER_KEY);
        if (state != null && state.equalsIgnoreCase("true")) {
            LOG.info("Adds demo handler ...");
            StaticHttpHandler shl = new StaticHttpHandler("demo");

            boolean on = false;
            try {
                on = Boolean.valueOf(FoxCfg.get("staticFileCacheEnabled"));
            } catch (Exception e) {
                on = false;
            }
            shl.setFileCacheEnabled(on);
            server.getServerConfiguration().addHttpHandler(shl, "/", "/demo");
        }

        // apiHttpHandler
        state = FoxCfg.get(API_HANDLER_KEY);
        if (state != null && state.equalsIgnoreCase("true")) {
            LOG.info("Adds api handler ...");
            FoxHttpHandler foxhttp = new FoxHttpHandler();
            server.getServerConfiguration().addHttpHandler(
                    foxhttp,
                    foxhttp.getMappings().toArray(new String[foxhttp.getMappings().size() - 1]));
        }

        // feedbackHttpHandler
        state = FoxCfg.get(FEEDBACK_HANDLER_KEY);
        if (state != null && state.equalsIgnoreCase("true")) {
            LOG.info("Adds feedback handler ...");
            FeedbackHttpHandler fb = new FeedbackHttpHandler();
            server.getServerConfiguration().addHttpHandler(
                    fb,
                    fb.getMappings().toArray(new String[fb.getMappings().size() - 1]));
        }

        if (server.getServerConfiguration().getHttpHandlersWithMapping().size() == 0) {
            LOG.warn("No HttpHandler found. No available path for the server.");
        }

    }

    /**
     * Starts the server and write a shut down file.
     */
    public void start() {

        int port = server.getListener(LISTENER_NAME).getPort();
        String host = server.getListener(LISTENER_NAME).getHost();

        try {
            LOG.info("----------------------------------------------------------\n");
            server.start();

            FoxServerUtil.writeShutDownFile("fox_close");
            LOG.info("http://" + host + ":" + port + "/api");
            LOG.info("http://" + host + ":" + port + "/api/ner/feedback");
            LOG.info("http://" + host + ":" + port + "/demo/index.html");
            LOG.info("----------------------------------------------------------\n");

            try {
                Thread.currentThread().join();
            } catch (Exception e) {
                LOG.error("\n", e);
            } finally {
                server.shutdownNow();
            }

        } catch (Exception e) {
            LOG.error("\n", e);
        }
    }
}
