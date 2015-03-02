package org.aksw.fox.utils;

import junit.framework.Assert;

import org.junit.Test;

public class TestFoxTextUtil {
    String text     = "Das <a href=\"/wiki/Augusteum_(Universit%C3%A4t_Leipzig)\" title=\"Augusteum (Universität Leipzig)\">"
                            + "Neue Augusteum</a> am <a href=\"/wiki/Augustusplatz_(Leipzig)\" title=\"Augustusplatz (Leipzig)\">Augustusplatz</a> "
                            + "ist seit Sommer 2012 das Hauptgebäude der Universität Leipzig. "
                            + "Es beherbergt u.a. das <a href=\"/wiki/Auditorium_maximum\" title=\"Auditorium maximum\">Audimax</a> mit 800 Sitzplätzen. "
                            + "Rechts daneben das <a href=\"/wiki/Paulinum_(Universit%C3%A4t_Leipzig)\" title=\"Paulinum (Universität Leipzig)\">Neue Paulinum</a>.</div>";

    String expected = "Das Neue Augusteum am Augustusplatz ist seit Sommer 2012 das Hauptgebäude der Universität Leipzig. Es beherbergt u.a. das Audimax mit 800 Sitzplätzen."
                            + " Rechts daneben das Neue Paulinum.";

    @Test
    public void htmlToTextTest() {

        String clean = FoxTextUtil.htmlToText(text);
        Assert.assertEquals(
                expected,
                clean
                );

        text = "(" + text;
        expected = "(" + expected;
        clean = FoxTextUtil.htmlToText(text);
        Assert.assertEquals(
                expected,
                clean
                );

    }

    @Test
    public void getIndicesTest() {
        Assert.assertTrue(1 == FoxTextUtil.getIndices("    Augusteum    ", expected).size());
        Assert.assertTrue(9 == FoxTextUtil.getIndices("    Augusteum    ", expected).iterator().next());
        Assert.assertTrue(11 == FoxTextUtil.getIndices("    Augusteum    ", "  " + expected).iterator().next());
    }
}
