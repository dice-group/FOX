package org.aksw.fox.utils.tweets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Preprocessor {

    protected final static String TWEET_PREPROCESSING_ON = Preprocessor.class.getName().concat(".enabled");

    public static boolean         enabled                = Boolean.parseBoolean(FoxCfg.get(TWEET_PREPROCESSING_ON));

    private Map<String, String>   slang                  = new HashMap<>();
    public final static Logger    LOG                    = LogManager.getLogger(Preprocessor.class);

    public String preprocess(String text) {

        // open resource file
        BufferedReader reader = null;
        try {
            InputStream in = Preprocessor.class.getResource("slang.txt").openStream();
            reader = new BufferedReader(new InputStreamReader(in));
        } catch (IOException e) {
            LOG.error("Couldn't open slang.txt resource file.", e);
        }

        // read resource file
        if (reader != null) {
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split("\t");
                    if (split.length == 2)
                        slang.put(split[0], split[1]);
                    else
                        LOG.warn("Could not use line: " + line);
                }
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        String del = " \n\t\r\f&/(){}[];,:.-_+~*!?|";
        StringTokenizer tokenizer = new StringTokenizer(text, del, true);

        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (!token.equals(" ") && del.contains(token)) {
                sb.append(" ");

            } else if (token.startsWith("@") || token.startsWith("#")) {
                String[] split = token.split("(?=\\p{Lu})");
                sb.append(" ");
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (!s.startsWith("@") && !s.startsWith("#")) {
                        sb.append(s);
                        if (i + 1 < split.length) {
                            sb.append(" ");
                        }
                    }
                }

            } else if (slang.get(token.toLowerCase()) != null) {
                char[] chars = new char[token.length()];
                Arrays.fill(chars, ' ');
                sb.append(chars, 0, token.length());
            } else {
                sb.append(token);
            }
        }
        return sb.toString();
    }

    public static void main(String[] a) {

        System.out.println(Preprocessor.enabled);
        Preprocessor pp = new Preprocessor();
        String tweet =
                "@Barack Obama goes to the #WhiteHouse :) ASAP 2018";
        System.out.println(tweet);

        tweet = pp.preprocess(tweet);
        System.out.println(tweet);
    }
}
