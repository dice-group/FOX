package org.aksw.fox.data;

import org.aksw.fox.nerlearner.TokenManager;
import org.junit.Assert;
import org.junit.Test;

public class TestTokenManager {

    @Test
    public void emptyInputTest() {
        TokenManager tm = new TokenManager("");
        Assert.assertEquals("", tm.getInput());
        Assert.assertEquals("", tm.getTokenInput());
        Assert.assertEquals(0, tm.getTokenSplit().length);
    }

    @Test
    public void test() {
        String sentences = "(1) The philosopher and mathematician Leibniz "
                + "was born in Leipzig in 1646 and attended the "
                + "University of Leipzig from 1661-1666.";

        TokenManager tm = new TokenManager(sentences);

        Assert.assertEquals(sentences, tm.getInput());
        Assert.assertEquals(sentences.length(), tm.getTokenInput().length());
        Assert.assertEquals("1", tm.getToken(1));
        Assert.assertEquals("The", tm.getToken(4));
        Assert.assertEquals("1661-1666", tm.getToken(118));
    }
}
