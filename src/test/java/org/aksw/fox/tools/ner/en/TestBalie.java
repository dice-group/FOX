package org.aksw.fox.tools.ner.en;

import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.utils.DataTestFactory;
import org.junit.Test;

import junit.framework.Assert;

public class TestBalie {

  @Test
  public void getNER() {
    final BalieEN balie = new BalieEN();
    final List<Entity> e = balie.retrieve(DataTestFactory.NER_EN_EXAMPLE_1);

    Assert.assertTrue(e.size() > 0);
  }
}
