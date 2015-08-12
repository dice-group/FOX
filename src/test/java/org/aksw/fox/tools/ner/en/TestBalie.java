package org.aksw.fox.tools.ner.en;

import java.util.List;

import junit.framework.Assert;

import org.aksw.fox.data.Entity;
import org.aksw.fox.utils.FoxConst;
import org.junit.Test;

public class TestBalie {

    @Test
    public void getNER() {
        BalieEN balie = new BalieEN();
        List<Entity> e = balie.retrieve(FoxConst.NER_EN_EXAMPLE_1);

        Assert.assertTrue(e.size() > 0);
    }
}
