package org.aksw.fox.webservice;

import java.io.IOException;

import org.aksw.simba.knowledgeextraction.commons.io.WebAppsUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import spark.Spark;

public abstract class AServer {
  public static Logger LOG = LogManager.getLogger(AServer.class);

  public AServer() {}

  /**
   * Sets the folder in classpath serving static files and checks the port as well as writes a file
   * to kill the application.
   *
   * @param staticLocation
   * @param port
   * @throws IOException
   */
  public AServer(final String staticLocation, final int port) throws IOException {
    LOG.info("Fox web service ...");

    Spark.staticFileLocation(staticLocation); // must be called before all other methods

    if (!WebAppsUtil.isPortAvailable(port)) {
      throw new IOException("Port " + port + " in use.");
    }
    Spark.port(port);

    WebAppsUtil.writeShutDownFile("stop");
  }

  /**
   * Stops the service.
   */
  public void stop() {
    Spark.stop();
  }

  /**
   * Starts the server and allows utf-8 requests only. Calls {@link #mapRoutes()) to initializes the
   * roots of the server.
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
        if (encoding == null || !encoding.toLowerCase().trim().equals("utf-8")) {
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
