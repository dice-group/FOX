package org.aksw.fox.tools.re;

import java.util.List;

import org.aksw.fox.tools.ToolsGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class REToolsTest {
  public static final Logger LOG = LogManager.getLogger(REToolsTest.class);

  @Test
  public void test() {

    LOG.info("REToolsTest start ...");
    final ToolsGenerator toolsGenerator = new ToolsGenerator();

    for (final String lang : ToolsGenerator.usedLang) {
      final RETools reTools = toolsGenerator.getRETools(lang);

      final List<IRE> tools = reTools.getRETool(lang);

      Assert.assertNotNull("test", tools);

    }
    LOG.info("REToolsTest end.");
  }
}
