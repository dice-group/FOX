package org.aksw.fox.webservice;

import org.aksw.fox.data.exception.PortInUseException;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.FoxHttpHandler;
import org.aksw.fox.web.Server;
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

  public static final String CFG_KEY_FOX_LIFETIME =
      FoxHttpHandler.class.getName().concat(".lifetime");

  public static final XMLConfiguration CFG = CfgManager.getCfg(Server.class);

  /**
   *
   * Constructor.
   *
   * @throws PortInUseException
   *
   */
  public AServer() throws PortInUseException {

    final int port = CFG.getInt(KEY_PORT);

    if (!FoxServerUtil.isPortAvailable(port)) {
      throw new PortInUseException(port);
    }

    FoxServerUtil.writeShutDownFile("stop");

    Spark.port(port);
  }

  /**
   *
   */
  public void stop() {
    Spark.stop();
  }

  /**
   *
   */
  public final void start() {
    LOG.info("Start ...");

    Runtime.getRuntime().addShutdownHook(new Thread((Runnable) () -> stop(), "shutdown hook"));

    Spark.staticFileLocation("/public");

    Spark.before((req, res) -> {

      // for all POST requests, utf-8 only
      if (req.requestMethod().toLowerCase().equals("post")) {
        // utf-8 only
        final String encoding = req.raw().getCharacterEncoding();
        if ((encoding == null) || !encoding.toLowerCase().trim().equals("utf-8")) {
          Spark.halt(415, "Use utf-8");
        }
      }
    });

    mapRoutes();

    LOG.info("Server is ready to use.");
  }

  /**
   *
   */
  public abstract void mapRoutes();

}
