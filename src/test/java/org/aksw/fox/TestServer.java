package org.aksw.fox;

import org.aksw.fox.exception.PortInUseException;
import org.aksw.fox.webservice.FoxServer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.Assert;

public class TestServer {

  public final static Logger LOG = LogManager.getLogger(TestServer.class);

  FoxServer server = null;

  public TestServer() throws PortInUseException {
    server = new FoxServer();
  }

  @Test
  public void server() {

    server.start();

    // TODO: test something here.

    server.stop();
    Assert.assertTrue(true);
  }
}
