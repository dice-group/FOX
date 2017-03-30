package org.aksw.fox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * Static class to provide general 'text' functionality.
 *
 * @author rspeck
 *
 */
public class FoxTextUtil {

  public static Logger LOG = LogManager.getLogger(FoxTextUtil.class);
  /**
   * Defines token.
   */
  public static final String tokenSpliter = "[\\p{Punct}&&[^-\\_/&+.]]| |\\t|\\n";

  private FoxTextUtil() {}

  /**
   * Gets the content from html/text as plain text.
   *
   * @param url
   * @return plain text
   */
  public static synchronized String urlToText(final String url) {
    LOG.info("urlToText: " + url);
    String html = null;
    try {
      html = IOUtils.toString(new URL(url));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      html = "";
    }
    return htmlToText(html);
  }

  /**
   * Gets the content from html/text as plain text.
   */
  public static synchronized String htmlToText(String html) {
    LOG.info("extractFromHTML ... ");

    // Adds line breaks to keep structure
    html = html.replaceAll("<li>", "<li>, ");
    html = html.replaceAll("</li>", ", </li>");
    html = html.replaceAll("<dd>", "<dd>, ");
    html = html.replaceAll("</dd>", ", </dd>");

    final Source src = new Source(html);
    return new TextExtractor(new Segment(src, src.getBegin(), src.getEnd()))
        .setConvertNonBreakingSpaces(true).toString();
  }

  /**
   *
   * @param input
   * @return
   */
  public static synchronized String[] getSentencesToken(final String input) {

    final List<String> result = new ArrayList<>();
    for (final String sentence : _getSentences(input)) {
      result.addAll(new ArrayList<>(Arrays.asList(getSentenceToken(sentence))));
    }

    return result.toArray(new String[result.size()]);
  }

  public static synchronized String[] getSentences(final String source) {

    final String[] sentences = _getSentences(source);

    // logger.info("sentences: " + sentences.length);

    return sentences;
  }

  /**
   * Gets sentences.
   *
   * @param source plain text of sentences
   * @return sentences
   */
  protected static synchronized String[] _getSentences(final String source) {

    InputStream modelIn = null;
    try {
      modelIn = new FileInputStream("data/openNLP/en-sent.bin");
    } catch (final FileNotFoundException e) {
      LOG.error("\n", e);
    }
    if (modelIn == null) {
      return null;
    }

    String[] sentences = null;
    try {
      final SentenceModel model = new SentenceModel(modelIn);
      final SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
      sentences = sentenceDetector.sentDetect(source);
    } catch (final IOException e) {
      LOG.error("\n", e);
    } finally {
      if (modelIn != null) {
        try {
          modelIn.close();
        } catch (final IOException e) {
          LOG.error("\n", e);
        }
      }
    }

    return sentences;
  }

  /**
   * Gets token of one sentence, token defined by {@link FoxTextUtil#tokenSpliter}.
   *
   * @param sentence (with punctuation mark)
   * @return token
   */
  public static synchronized String[] getSentenceToken(String sentence) {
    // System.out.println(sentence);
    // Note: Points won't removed, so we remove punctuation marks to points
    // and handle them later
    final char punctuationMark = sentence.trim().charAt(sentence.trim().length() - 1);
    if ((punctuationMark == '!') || (punctuationMark == '?')) {
      final int punctuationMarkIndex = sentence.lastIndexOf(punctuationMark);
      sentence = sentence.substring(0, punctuationMarkIndex) + "."
          + sentence.substring(punctuationMarkIndex + 1, sentence.length());
    }

    String[] token = null;
    token = getToken(sentence);

    if (token.length > 0) {
      // remove punctuation mark(points)
      final String lastToken = token[token.length - 1];
      if (lastToken.charAt(lastToken.length() - 1) == '.') {
        token[token.length - 1] = lastToken.substring(0, lastToken.length() - 1);
      }

      // add a token to keep original length
      final int len = sentence.length();

      final String cleanSentence = StringUtils.join(token, " ");

      final int cleanSentenceLen = cleanSentence.length();

      String closeLen = "";
      while ((cleanSentenceLen + closeLen.length()) < len) {
        closeLen += " ";
      }
      // add this token
      if (!closeLen.isEmpty()) {
        token = ArrayUtils.add(token, token.length, closeLen);
      }

      // logger.info("----");
      // logger.info("<" + len + ">");
      // logger.info("<" + cleanSentenceLen + ">");
      // logger.info("<" + sentence + ">");
      // logger.info("<" + cleanSentence + ">");
      // logger.info("<" + StringUtils.join(token, " ") + ">");
      // logger.info("<" + token[token.length - 1] + ">");
    } else {
      token = new String[0];
    }
    return token;
  }

  /**
   * Gets token defined by {@link FoxTextUtil#tokenSpliter}.
   *
   * @param in string to split
   * @return token
   */
  public static synchronized String[] getToken(final String in) {
    return in.split(tokenSpliter);
  }

  // token needs to bound in spaces e.g.: " Leipzig "
  public static synchronized Set<Integer> getIndices(String token, String tokenInput) {

    final Set<Integer> indices = new HashSet<>();
    if ((token != null) && (tokenInput != null) && (token.length() < tokenInput.length())) {

      token = new StringBuilder().append(" ").append(token.trim()).append(" ").toString();
      tokenInput = new StringBuilder().append(" ").append(tokenInput).append(" ").toString();

      token = Pattern.quote(token);
      final Matcher matcher = Pattern.compile(token).matcher(tokenInput);
      while (matcher.find()) {
        indices.add((matcher.start() + 1) - 1);
      }
    }
    return indices;
  }
}
