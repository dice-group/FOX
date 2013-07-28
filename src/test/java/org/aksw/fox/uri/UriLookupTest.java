package org.aksw.fox.uri;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UriLookupTest {
    InterfaceURI uriLookup = null;

    @Test
    public final void test() {
        PropertyConfigurator.configure("log4j.properties");
        // TODO
        // uriLookup = new UriLookup();
        // test
        uriLookup();
    }

    public void uriLookup() {
        UriLookup.logger.info("uriLookup test ... ");
        //
        // assertTrue("http://dbpedia.org/resource/Leipzig".equals(uriLookup.getUri("Leipzig")));
        // assertTrue("http://dbpedia.org/resource/Leipzig".equals(uriLookup.getUri("Leipzig",
        // "per")));
        // assertTrue("http://dbpedia.org/resource/Leipzig".equals(uriLookup.getUri("Leipzig",
        // "loc")));
        // assertTrue("http://dbpedia.org/resource/Leipzig".equals(uriLookup.getUri("Leipzig",
        // "org")));
        //
        // assertTrue("http://dbpedia.org/resource/University_of_Leipzig".equals(uriLookup.getUri("University of Leipzig")));
        // assertTrue("http://dbpedia.org/resource/University_of_Leipzig".equals(uriLookup.getUri("University of Leipzig",
        // "per")));
        // assertTrue("http://dbpedia.org/resource/University_of_Leipzig".equals(uriLookup.getUri("University of Leipzig",
        // "loc")));
        // assertTrue("http://dbpedia.org/resource/University_of_Leipzig".equals(uriLookup.getUri("University of Leipzig",
        // "org")));
    }
}
