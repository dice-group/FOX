package org.aksw.fox.utils;

import static org.junit.Assert.assertEquals;

import org.aksw.fox.Const;
import org.aksw.fox.data.TokenManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FoxTextUtilTest {

    String test = " The foundation of the University of Leipzig in 1409 initiated the city's development into a centre of German law and the publishing industry, and towards being a location of the Reichsgericht (High Court), and the German National Library (founded in 1912). The philosopher and mathematician Gottfried Wilhelm Leibniz was born in Leipzig in 1646, and attended the university from 1661-1666.";

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");

        getSentencesToken();
        // getSentences();
        // getSentenceToken();
    }

    public void getSentencesToken() {
        FoxTextUtil.logger.info("getSentencesToken test...");

        for (String in : Const.TEST_INPUT) {
            in = new TokenManager(in).getInput();

            // String out = StringUtils.join(FoxTextUtil.getSentencesToken(in),
            // " ");
            // out = out.substring(0, out.length() - 1);
            String out = "";
            for (String token : FoxTextUtil.getSentencesToken(in)) {
                if (!token.trim().isEmpty()) {
                    out += " " + token;
                } else {
                    if (token.isEmpty())
                        out += " ";
                    else
                        out += token;
                }
            }

            out = out.substring(1, out.length());
            FoxTextUtil.logger.info("<" + in + ">");
            FoxTextUtil.logger.info("<" + out + ">");

            assertEquals(in.length(), out.length());
        }

    }
    // public void getSentences() {
    // FoxTextUtil.logger.info("getSentences test ...");
    //
    // assertEquals(2, FoxTextUtil.getSentences(in).length);
    // }
    //
    // public void getSentenceToken() {
    //
    // FoxTextUtil.logger.info("getSentenceToken test ...");
    //
    // String[] sentences = FoxTextUtil.getSentences(in);
    //
    // String[] token;
    // token = FoxTextUtil.getSentenceToken(sentences[0]);
    // assertEquals("...", token[token.length - 2]);
    // assertEquals("  ", token[token.length - 1]);
    //
    // token = FoxTextUtil.getSentenceToken(sentences[1]);
    // assertEquals("...", token[token.length - 2]);
    // assertEquals("  ", token[token.length - 1]);
    // }
}
