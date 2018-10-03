package org.aksw.fox.utils.tweets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Preprocessor {

  protected final static String TWEET_PREPROCESSING_ON =
      Preprocessor.class.getName().concat(".enabled");

  public static boolean enabled =
      Boolean.parseBoolean(PropertiesLoader.get(TWEET_PREPROCESSING_ON));

  private final Map<String, String> slang = new HashMap<>();
  public final static Logger LOG = LogManager.getLogger(Preprocessor.class);

  public String preprocess(final String text) {

    // open resource file
    BufferedReader reader = null;
    try {
      final InputStream in = Preprocessor.class.getResource("slang.txt").openStream();
      reader = new BufferedReader(new InputStreamReader(in));
    } catch (final IOException e) {
      LOG.error("Couldn't open slang.txt resource file.", e);
    }

    // read resource file
    if (reader != null) {
      try {
        String line = null;
        while ((line = reader.readLine()) != null) {
          final String[] split = line.split("\t");
          if (split.length == 2) {
            slang.put(split[0], split[1]);
          } else {
            LOG.warn("Could not use line: " + line);
          }
        }
      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }

    final String del = " \n\t\r\f&/(){}[];,:.-_+~*!?|";
    final StringTokenizer tokenizer = new StringTokenizer(text, del, true);

    final StringBuffer sb = new StringBuffer();
    while (tokenizer.hasMoreTokens()) {
      final String token = tokenizer.nextToken();

      if (!token.equals(" ") && del.contains(token)) {
        sb.append(" ");

      } else if (token.startsWith("@") || token.startsWith("#")) {
        final String[] split = token.split("(?=\\p{Lu})");
        sb.append(" ");
        for (int i = 0; i < split.length; i++) {
          final String s = split[i];
          if (!s.startsWith("@") && !s.startsWith("#")) {
            sb.append(s);
            if (i + 1 < split.length) {
              sb.append(" ");
            }
          }
        }

      } else if (slang.get(token.toLowerCase()) != null) {
        final char[] chars = new char[token.length()];
        Arrays.fill(chars, ' ');
        sb.append(chars, 0, token.length());
      } else {
        sb.append(token);
      }
    }
    return sb.toString();
  }

  public static void main(final String[] a) {

    System.out.println(Preprocessor.enabled);
    final Preprocessor pp = new Preprocessor();
    String tweet = "@Barack Obama goes to the #WhiteHouse :) ASAP 2018";
    System.out.println(tweet);

    tweet = pp.preprocess(tweet);
    System.out.println(tweet);
  }
}
