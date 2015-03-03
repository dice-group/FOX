package org.aksw.fox.utils.tweets;

import org.junit.Assert;
import org.junit.Test;

public class TestPreprocessor {

    @Test
    public void preprocessTest() {

        Preprocessor pp = new Preprocessor();
        String expected =
                " Barack Obama goes to the  White House         2018";
        String tweet =
                "@Barack Obama goes to the #WhiteHouse :) ASAP 2018";

        tweet = pp.preprocess(tweet);
        Assert.assertEquals(tweet, expected);

    }
}
