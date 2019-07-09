package org.aksw.fox;

import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.utils.FoxLanguageDetector;
import org.junit.Assert;
import org.junit.Test;

public class FoxLanguageDetectorTest {

  @Test
  public void dutchTest() {
    final FoxLanguageDetector ld = new FoxLanguageDetector();
    final FoxParameter.Langs lang = ld.detect(DataTestFactory.NER_NL_EXAMPLE_1);
    Assert.assertEquals("nl", lang.toString());
  }
}
