package org.aksw.fox;

import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.tools.ITool;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class IToolTest {

  public final static Logger LOG = LogManager.getLogger(ServerTest.class);

  @Test
  public void getToolName() {

    PropertiesLoader.setPropertiesFile("fox.properties");

    final String lang = FoxParameter.Langs.EN.toString();
    final IFox fox = new Fox(lang);

    Assert.assertEquals(Fox.class.getName(), ((ITool) fox).getToolName());
    Assert.assertNotNull(((ITool) fox).getToolVersion());
  }
}
