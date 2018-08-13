package org.aksw.fox.webservice;

import org.aksw.fox.exception.PortInUseException;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxServerUtil;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import spark.Spark;

public abstract class AServer {
  public static Logger LOG = LogManager.getLogger(AServer.class);

  public static final String CFG_KEY_POOL_SIZE = "server.poolSize";
  public final static String KEY_DEMO = "server.demo";
  public final static String KEY_API = "server.api";
  public final static String KEY_FEEDBACK = "server.feedback";
  public final static String KEY_CACHE = "server.staticFileCache";
  public final static String KEY_LISTENER_NAME = "server.listenerName";
  public final static String KEY_PORT = "server.port";
  public final static String KEY_DEFAULT_NETWORK_HOST = "server.host";
  public static final String CFG_KEY_FOX_LIFETIME = "server.lifetime";

  public static final XMLConfiguration CFG = CfgManager.getCfg(AServer.class);

  /**
   *
   * Constructor.
   *
   * @throws PortInUseException
   *
   */
  public AServer(final String staticLocation) throws PortInUseException {

    // must be called before all other methods
    Spark.staticFileLocation(staticLocation);

    final int port = CFG.getInt(KEY_PORT);
    if (!FoxServerUtil.isPortAvailable(port)) {
      throw new PortInUseException(port);
    }
    Spark.port(port);

    FoxServerUtil.writeShutDownFile("stop");
  }

  /**
   *
   */
  public void stop() {
    Spark.stop();
  }

  /**
   * Starts the server and allows utf-8 requests only. Calls {@link #mapRoutes()) to initializes the
   * roots of the server.
   *
   */
  public final void start() {
    LOG.info("Start ...");

    Runtime.getRuntime().addShutdownHook(new Thread((Runnable) () -> {
      //
      stop();
    }, "shutdown hook"));

    Spark.before((req, res) -> {

      // for all POST requests, utf-8 only
      if (req.requestMethod().toLowerCase().equals("post")) {
        // utf-8 only
        final String encoding = req.raw().getCharacterEncoding();
        LOG.info("requested encoding:" + encoding);
        if ((encoding == null) || !encoding.toLowerCase().trim().equals("utf-8")) {
          Spark.halt(415, "Use utf-8");
        }
      }
    });
    mapRoutes();
    LOG.info("Server is ready to use.");
  }

  /**
   * Initializes the roots of the server
   */
  public abstract void mapRoutes();

  /**
   * Runs before the server shuts down.
   */
  public abstract void addShutdownHook();
}
