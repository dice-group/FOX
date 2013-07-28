package org.aksw.fox.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.aksw.fox.Const;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TokenManagerTest {

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");

        getInput();
    }

    // test TokenManager getInput
    public void getInput() {

        for (String test : Const.TEST_INPUT) {

            TokenManager tm = new TokenManager(test);
            TokenManager tmm = new TokenManager(tm.getInput());

            assertEquals(tm.getTokenInput().length(), tmm.getInput().length());
            assertTrue(tm.getTokenInput().length() <= test.length());
            assertTrue(tmm.getInput().length() <= test.length());
        }
    }
}
