package org.aksw.fox.tools.ner.en;

import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.tools.ner.INER;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class NERToolsTest {

  public final static Logger LOG = LogManager.getLogger(NERToolsTest.class);

  @Test
  public void test() {

    LOG.info("BalieEN");
    nerTest(new BalieEN());

    LOG.info("IllinoisExtendedEN");
    nerTest(new IllinoisExtendedEN());

    LOG.info("StanfordEN");
    nerTest(new StanfordEN());

    LOG.info("OpenNLPEN");
    nerTest(new OpenNLPEN());
  }

  public void nerTest(final INER tool) {

    String text;
    text = "But, Obama was born in Hawaii. ";
    text += text;
    text += text;
    text = text.trim();

    final List<Entity> list = tool.retrieve(text);
    list.forEach(LOG::info);
    int mentions = 0;
    for (final Entity entity : list) {
      if (entity.getText().equals("Hawaii") && entity.getType().equals(EntityTypes.L)) {
        mentions++;
      }
    }
    Assert.assertTrue(mentions == 4);
  }
}
