package org.aksw.fox.utils;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FoxCfgTest {

    @Test
    public void read() {
        PropertyConfigurator.configure("log4j.properties");

        assertTrue(FoxCfg.get("") == null);
        assertTrue(FoxCfg.get("serializedClassifier").length() > 0);
    }
}
