package org.aksw.fox.nerlearner.reader;

import org.aksw.fox.exception.LoadingNotPossibleException;
import org.aksw.fox.utils.FoxCfg;
import org.junit.Assert;
import org.junit.Test;

public class TestNERReaderFactory {
    @Test
    public void testInstanceofINERReader() throws LoadingNotPossibleException {
        Assert.assertTrue(FoxCfg.loadFile("fox.properties-dist"));
        Assert.assertNotNull(FoxCfg.get(NERReaderFactory.INER_READER_KEY));
        Assert.assertTrue(NERReaderFactory.getINERReader() instanceof INERReader);
    }
}
