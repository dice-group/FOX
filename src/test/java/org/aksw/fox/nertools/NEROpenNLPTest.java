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
public class NEROpenNLPTest {

    InterfaceRunnableNER ner = new NEROpenNLP();

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");

        personTest();
        retrieve();
    }

    public void personTest() {
        NEROpenNLP.logger.info("person test ...");

        Set<Entity> set = ner.retrieve(Const.TEST_INPUT[0]);

        assertTrue(set.contains(new Entity("M. Parkin", EntityClassMap.O))); // ORGANIZATION
                                                                             // :-(
        assertTrue(set.contains(new Entity("Johnson", EntityClassMap.P)));

        assertTrue(set.size() == 2);
    }

    public void retrieve() {
        NEROpenNLP.logger.info("retrieve ...");

        String in = "";
        in += "Anant Agarwal holds a Ph.D. (1987) and an MS in Electrical Engineering from  Stanford University. ";
        in += "He got his bachelor's degree in Electrical Engineering  from  IIT Madras  (1982). ";
        in += "He is currently a professor of  Electrical Engineering and Computer Science  at  MIT, where his teaching and research interests include VLSI, computer architecture, compilation, and software systems. ";
        in += "He also led the  VirtualWires  project at  MIT  and was founder of  Virtual Machine Works, Inc., which took the  VirtualWires  logic emulation technology to market.";

        Set<Entity> results = ner.retrieve(in);

        assertTrue(results.contains(new Entity("Stanford University", EntityClassMap.O)));

    }
}
