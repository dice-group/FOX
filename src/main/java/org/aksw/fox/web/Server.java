package org.aksw.fox.web;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.ext.RuntimeDelegate;

import org.aksw.fox.Fox;
import org.aksw.fox.IFox;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.feedback.FeedbackHttpHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * 
 * @author rspeck
 * 
 */
public class Server {

    public static final String     CFG_KEY_POOL_SIZE    = Server.class.getName().concat(".poolsize");
    public static final int        poolsize             = Integer.parseInt(FoxCfg.get(CFG_KEY_POOL_SIZE));

    // TODO
    public static final Pool<IFox> pool                 = new Pool<IFox>(Fox.class.getName(), poolsize);

    public final static Logger     LOG                  = LogManager.getLogger(Server.class);

    /* Cfg file key. */
    public final static String     DEMO_HANDLER_KEY     = Server.class.getName().concat(".demo_handler");
    /* Cfg file key. */
    public final static String     API_HANDLER_KEY      = Server.class.getName().concat(".api_handler");
    /* Cfg file key. */
    public final static String     FEEDBACK_HANDLER_KEY = Server.class.getName().concat(".feedback_handler");
    /* Cfg file key. */
    public final static String     STATIC_CACHE         = Server.class.getName().concat(".static_file_cache_on");

    protected final HttpServer     server               = new HttpServer();

    private final static String    LISTENER_NAME        = "FoxNetworkListener";

    /**
     * Create Jersey server-side application resource configuration.
     * 
     * @return Jersey server-side application configuration.
     */
    public static ResourceConfig createApiResourceConfig() {
        return new ResourceConfig()
                .registerClasses(ApiResource.class)
                .register(JsonProcessingFeature.class)
                .packages("org.glassfish.jersey.examples.jsonp")
                .property(JsonGenerator.PRETTY_PRINTING, true);
    }

    /**
     * Adds HttpHandler.
     * 
     * @param port
     *            the servers port
     */
    public Server(int port) {

        server.getServerConfiguration().setDefaultErrorPageGenerator(new ErrorPages());
        server.addListener(new NetworkListener(LISTENER_NAME, NetworkListener.DEFAULT_NETWORK_HOST, port));

        server.getServerConfiguration().addHttpHandler(
                RuntimeDelegate.getInstance().createEndpoint(createApiResourceConfig(), GrizzlyHttpContainer.class),
                "/call/"// BASE_URI.getPath()
        );

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
                on = Boolean.valueOf(FoxCfg.get(STATIC_CACHE));
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

        if (server.getServerConfiguration().getHttpHandlers().size() == 0) {
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
            server.start();
            FoxServerUtil.writeShutDownFile("close");
            LOG.info("----------------------------------------------------------\n");
            LOG.info("http://" + host + ":" + port + "/api");
            LOG.info("http://" + host + ":" + port + "/api/ner/feedback");
            LOG.info("http://" + host + ":" + port + "/demo/index.html");
            LOG.info("----------------------------------------------------------\n");
        } catch (Exception e) {
            LOG.error("\n", e);
        }

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Checking data before shutting down server...");
                // TODO: check data then shut down server.
                LOG.info("Stopping server with shutdownHook.");
                server.shutdownNow();
            }
        }, "ServerShutdownHook"));

        try {
            Thread.currentThread().join();
        } catch (Exception e) {
            LOG.error("\n", e);
        } finally {
            server.shutdownNow();
        }
    }
}
