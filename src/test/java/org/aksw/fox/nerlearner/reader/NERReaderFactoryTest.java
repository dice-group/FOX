package org.aksw.fox.nerlearner.reader;

import java.io.IOException;

import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.junit.Assert;
import org.junit.Test;

public class NERReaderFactoryTest {
  @Test
  public void testInstanceofINERReader() throws IOException {
    Assert.assertTrue(PropertiesLoader.loadFile("fox.properties-dist"));
    Assert.assertNotNull(PropertiesLoader.get(NERReaderFactory.INER_READER_KEY));
    Assert.assertTrue(NERReaderFactory.getINERReader() instanceof INERReader);
  }
}
