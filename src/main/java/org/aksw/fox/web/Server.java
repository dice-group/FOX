package org.aksw.fox.web;

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

    public static Logger logger = Logger.getLogger(Server.class);

    protected final HttpServer server = new HttpServer();

    /**
     * 
     * @param port
     */
    public Server(int port) {

        server.addListener(new NetworkListener("FoxNetworkListener", NetworkListener.DEFAULT_NETWORK_HOST, port));

        StaticHttpHandler shl = new StaticHttpHandler("demo");
        shl.setFileCacheEnabled(false);

        server.getServerConfiguration().addHttpHandler(shl, "/");
        server.getServerConfiguration().addHttpHandler(shl, "/demo");
        server.getServerConfiguration().addHttpHandler(new FoxHttpHandler(), "/api");
        server.getServerConfiguration().addHttpHandler(new FeedbackHttpHandler(), "/api/ner/feedback");
    }

    /**
     *  
     */
    public void start() {

        int port = server.getListener("FoxNetworkListener").getPort();
        String host = server.getListener("FoxNetworkListener").getHost();

        try {
            logger.info("----------------------------------------------------------\n");
            server.start();

            FoxServerUtil.writeShutDownFile("fox_close");
            logger.info("http://" + host + ":" + port + "/api");
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
