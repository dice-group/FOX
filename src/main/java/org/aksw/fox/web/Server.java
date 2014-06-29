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

    public static Logger        logger        = Logger.getLogger(Server.class);

    private final static String Listener_Name = "FoxNetworkListener";

    protected final HttpServer  server        = new HttpServer();

    /**
     * Adds HttpHandler.
     * 
     * @param port
     */
    public Server(int port) {

        server.addListener(new NetworkListener(Listener_Name, NetworkListener.DEFAULT_NETWORK_HOST, port));

        String state = null;

        // demoHttpHandler
        state = FoxCfg.get("demoHttpHandler");
        if (state != null && state.equalsIgnoreCase("true")) {
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
        state = FoxCfg.get("apiHttpHandler");
        if (state != null && state.equalsIgnoreCase("true")) {
            FoxHttpHandler foxhttp = new FoxHttpHandler();
            server.getServerConfiguration().addHttpHandler(
                    foxhttp,
                    foxhttp.getMappings().toArray(new String[foxhttp.getMappings().size() - 1]));
        }

        // feedbackHttpHandler
        state = FoxCfg.get("feedbackHttpHandler");
        if (state != null && state.equalsIgnoreCase("true")) {
            FeedbackHttpHandler fb = new FeedbackHttpHandler();

            server.getServerConfiguration().addHttpHandler(
                    fb,
                    fb.getMappings().toArray(new String[fb.getMappings().size() - 1]));
        }

        if (server.getServerConfiguration().getHttpHandlers().size() == 0) {
            logger.warn("No HttpHandler found. No available path for the server.");
        }
    }

    /**
     * Starts the server and write a shut down file.
     */
    public void start() {

        int port = server.getListener(Listener_Name).getPort();
        String host = server.getListener(Listener_Name).getHost();

        try {
            logger.info("----------------------------------------------------------\n");
            server.start();

            FoxServerUtil.writeShutDownFile("fox_close");
            logger.info("http://" + host + ":" + port + "/api");
            logger.info("http://" + host + ":" + port + "/api/ner/feedback");
            logger.info("http://" + host + ":" + port + "/demo/index.html");
            logger.info("----------------------------------------------------------\n");

            try {
                Thread.currentThread().join();
            } catch (Exception e) {
                logger.error("\n", e);
            } finally {
                server.stop();
            }

        } catch (Exception e) {
            logger.error("\n", e);
        }
    }
}
