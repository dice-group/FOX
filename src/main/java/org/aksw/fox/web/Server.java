package org.aksw.fox.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.ext.RuntimeDelegate;

import org.aksw.fox.Fox;
import org.aksw.fox.IFox;
import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.aksw.fox.data.exception.PortInUseException;
import org.aksw.fox.data.exception.UnsupportedLangException;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.feedback.FeedbackHttpHandler;
import org.apache.commons.configuration.XMLConfiguration;
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
    public final static Logger            LOG                      = LogManager.getLogger(Server.class);
    public static final XMLConfiguration  CFG                      = CfgManager.getCfg(Server.class);

    public static Map<String, Pool<IFox>> pool                     = null;
    static {
        try {
            Server.initPools();
        } catch (IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | SecurityException |
                UnsupportedLangException | LoadingNotPossibleException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    public static final String            CFG_KEY_POOL_SIZE        = "server.poolSize";
    public final static String            KEY_DEMO                 = "server.demo";
    public final static String            KEY_API                  = "server.api";
    public final static String            KEY_FEEDBACK             = "server.feedback";
    public final static String            KEY_CACHE                = "server.staticFileCache";
    public final static String            KEY_LISTENER_NAME        = "server.listenerName";
    public final static String            KEY_PORT                 = "server.port";
    public final static String            KEY_DEFAULT_NETWORK_HOST = "server.host";

    protected HttpServer                  server                   = null;
    public static boolean                 running                  = false;

    public int                            port                     = CFG.getInt(KEY_PORT);
    public String                         host                     = CFG.getString(KEY_DEFAULT_NETWORK_HOST);

    /**
     * 
     * @param port
     *            the servers port
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     * @throws PortInUseException
     */
    public Server()
            throws LoadingNotPossibleException, UnsupportedLangException,
            NumberFormatException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, PortInUseException {

        int port = CFG.getInt(KEY_PORT);
        if (!FoxServerUtil.isPortAvailable(port))
            throw new PortInUseException(port);

        init();
    }

    protected static void initPools()
            throws UnsupportedLangException, LoadingNotPossibleException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        pool = new HashMap<>();
        for (String lang : ToolsGenerator.usedLang) {
            int poolsize = CFG.getInt(CFG_KEY_POOL_SIZE.concat("[@").concat(lang).concat("]"));
            if (poolsize < 1) {
                LOG.error("Could not find pool size for the given lang" + lang + ". We use a poolsize of 1.");
                poolsize = 1;
            }
            pool.put(
                    lang,
                    new Pool<IFox>(Fox.class.getName(), lang, poolsize));
        }
    }

    protected void init()
            throws IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        server = new HttpServer();
        server.getServerConfiguration().setDefaultErrorPageGenerator(new ErrorPages());
        server.addListener(
                new NetworkListener(
                        CFG.getString(KEY_LISTENER_NAME),
                        CFG.getString(KEY_DEFAULT_NETWORK_HOST),
                        CFG.getInt(KEY_PORT)
                ));

        server.getServerConfiguration().addHttpHandler(
                RuntimeDelegate.getInstance().createEndpoint(createApiResourceConfig(), GrizzlyHttpContainer.class),
                ApiResource.getPath()
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
        state = CFG.getString(KEY_DEMO);
        if (state != null && state.equalsIgnoreCase("true")) {
            LOG.info("Adds demo handler ...");
            StaticHttpHandler shl = new StaticHttpHandler("demo");

            boolean on = false;
            try {
                on = CFG.getBoolean(KEY_CACHE);
            } catch (Exception e) {
                on = false;
            }
            shl.setFileCacheEnabled(on);
            server.getServerConfiguration().addHttpHandler(shl, "/", "/demo");
        }

        // apiHttpHandler
        state = CFG.getString(KEY_API);
        if (state != null && state.equalsIgnoreCase("true")) {
            LOG.info("Adds api handler ...");
            FoxHttpHandler foxhttp = new FoxHttpHandler();
            server.getServerConfiguration().addHttpHandler(
                    foxhttp,
                    foxhttp.getMappings().toArray(new String[foxhttp.getMappings().size() - 1]));
        }

        // feedbackHttpHandler
        state = CFG.getString(KEY_FEEDBACK);
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
    public boolean start() {
        String host = server.getListener(CFG.getString(KEY_LISTENER_NAME)).getHost();
        int port = server.getListener(CFG.getString(KEY_LISTENER_NAME)).getPort();

        try {
            server.start();
            running = true;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (running) {
            FoxServerUtil.writeShutDownFile("close");
            LOG.info("----------------------------------------------------------\n");
            LOG.info("http://" + host + ":" + port + "/api");
            LOG.info("http://" + host + ":" + port + "/api/ner/feedback");
            LOG.info("http://" + host + ":" + port + "/demo/index.html");
            LOG.info("----------------------------------------------------------\n");

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
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                server.shutdownNow();
            }
        }
        return running;
    }

    public void stop() {
        server.shutdownNow();
    }

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
}
