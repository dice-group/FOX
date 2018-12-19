package org.aksw.fox;

import java.io.IOException;

import org.aksw.fox.ui.FoxRESTful;
import org.aksw.fox.webservice.FoxServer;
import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class TestServer {

  public final static Logger LOG = LogManager.getLogger(TestServer.class);

  FoxServer server = null;

  public TestServer() throws IOException {
    server = FoxServer.instance(new CfgManager(Fox.cfgFolder).getCfg(FoxRESTful.class));
  }

  @Test
  public void server() {

    server.start();

    // TODO: test something here.

    server.stop();
    Assert.assertTrue(true);
  }
}
