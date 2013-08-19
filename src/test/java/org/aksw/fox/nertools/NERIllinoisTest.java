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
public class NERIllinoisTest {

    InterfaceRunnableNER ner = new NERIllinois();

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");
        //
        // personTest();
        // anOtherTest();
        // andAnOtherTest();
    }

    // TEST_INPUT_1
    public void personTest() {

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[0]);
        assertTrue(set.size() == 14);

        Entity e = set.iterator().next();
        assertTrue(e != null);
        assertTrue(set.contains(e));

        assertTrue(set.contains(new Entity("B.H. Lim", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("K. Johnson", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("Multiprocessor", EntityClassMap.O))); // ORGANIZATION
        //
        assertTrue(set.contains(new Entity("G.D. Souza", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("K. MacKenzie", EntityClassMap.P)));

    }

    // TEST_INPUT_2
    public void anOtherTest() {

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[1]);

        assertTrue(set.contains(new Entity("Rhine-Ruhr Metropolitan Area", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Cologne Cathedral", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("University of Cologne", EntityClassMap.O)));
        assertTrue(set.contains(new Entity("Cologne", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Rhine River", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Hamburg", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("Berlin", EntityClassMap.L)));
        assertTrue(set.contains(new Entity("North Rhine-Westphalia", EntityClassMap.L)));

        assertTrue(set.size() == 17);
    }

    // TEST_INPUT_3
    public void andAnOtherTest() {

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[2]);
        assertTrue(set.contains(new Entity("Inc.", EntityClassMap.O)));
        assertTrue(set.contains(new Entity("Stanford University", EntityClassMap.O)));
        assertTrue(set.contains(new Entity("Anant Agarwal", EntityClassMap.P)));
        assertTrue(set.contains(new Entity("Virtual Machine Works", EntityClassMap.O)));

        assertTrue(set.size() == 11);
    }
}
