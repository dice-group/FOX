package org.aksw.fox.nertools;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.aksw.fox.Const;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NERBalieTest {
    InterfaceRunnableNER ner = new NERBalie();

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");

        personTest();
        anOtherTest();
        // andAnOtherTest();
    }

    // TEST_INPUT_1
    public void personTest() {

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[0]);

        assertTrue(set.contains(new Entity("Babb", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("Parkin", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("Souza", EntityClassMap.O))); // ORGANIZATION
                                                                         // :-(

        assertTrue(set.contains(new Entity("Johnson", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("MacKenzie", EntityClassMap.P)));

        assertTrue(set.size() == 5);
    }

    // TEST_INPUT_2
    public void anOtherTest() {

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[1]);

        assertTrue(set.contains(new Entity("Dom", EntityClassMap.P))); // :-(
        // TODO
        // assertTrue(set.contains(new Entity("North Rhine-Westphalia",
        // EntityClassMap.P))); // :-(
        assertTrue(set.contains(new Entity("Munich", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Germany", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Cologne", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Rhine River", EntityClassMap.P))); // :-(
        assertTrue(set.contains(new Entity("Ruhr", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Rhine", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Hamburg", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Berlin", EntityClassMap.L)));

        assertTrue(set.size() == 10);
    }

    // TEST_INPUT_3
    public void andAnOtherTest() {

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[2]);
        NERBalie.logger.info(set);

        assertTrue(set.contains(new Entity("Inc.", EntityClassMap.O))); // :-(
        assertTrue(set.contains(new Entity("Stanford University", EntityClassMap.P))); // :-(
        assertTrue(set.size() == 9);
    }
}
