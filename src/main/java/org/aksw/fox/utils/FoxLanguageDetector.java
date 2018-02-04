package org.aksw.fox.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.FoxParameter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;

/**
 *
 * @author rspeck
 *
 */
public class FoxLanguageDetector {

  public static Logger LOG = LogManager.getLogger(FoxLanguageDetector.class);

  public static Set<FoxParameter.Langs> lang = new HashSet<>(Arrays.asList(//
      FoxParameter.Langs.DE, //
      FoxParameter.Langs.EN, //
      FoxParameter.Langs.FR, //
      FoxParameter.Langs.ES, //
      FoxParameter.Langs.IT, //
      FoxParameter.Langs.NL));

  protected LanguageDetector languageDetector = null;

  /**
   * Test
   *
   * @param a
   */
  public static void main(final String[] a) {
    final FoxLanguageDetector ld = new FoxLanguageDetector();
    final FoxParameter.Langs lang = ld.detect(FoxConst.NER_NL_EXAMPLE_1);
    LOG.info(lang.toString() + " text.");
  }

  /*
   *
   */
  public FoxLanguageDetector() {
    initLanguageDetector(lang);
  }

  /**
   *
   * @param lang
   */
  public FoxLanguageDetector(final Set<FoxParameter.Langs> lang) {
    initLanguageDetector(lang);
  }

  protected void initLanguageDetector(final Set<FoxParameter.Langs> lang) {
    final Set<String> langs = new HashSet<>();
    for (final FoxParameter.Langs l : lang) {
      langs.add(l.toString());
    }

    try {
      // build language detetor:
      languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
          .withProfiles(new LanguageProfileReader().read(langs)).build();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  public FoxParameter.Langs detect(final String text) {
    final TextObject textObject = CommonTextObjectFactories.forDetectingOnLargeText().forText(text);
    final List<DetectedLanguage> probs = languageDetector.getProbabilities(textObject);
    FoxParameter.Langs lang = null;
    for (final DetectedLanguage prob : probs) {
      lang = FoxParameter.Langs.fromString(prob.getLanguage());
      if (lang != null) {
        break;
      }
    }
    return lang;
  }

}
/*
 *
 * af Afrikaans ar Arabic bg Bulgarian bn Bengali cs Czech da Danish de German el Greek en English
 * es Spanish et Estonian fa Persian fi Finnish fr French gu Gujarati he Hebrew hi Hindi hr Croatian
 * hu Hungarian id Indonesian it Italian ja Japanese kn Kannada ko Korean lt Lithuanian lv Latvian
 * mk Macedonian ml Malayalam mr Marathi ne Nepali nl Dutch no Norwegian pa Punjabi pl Polish pt
 * Portuguese ro Romanian ru Russian sk Slovak sl Slovene so Somali sq Albanian sv Swedish sw
 * Swahili ta Tamil te Telugu th Thai tl Tagalog tr Turkish uk Ukrainian ur Urdu vi Vietnamese zh-cn
 * Simplified Chinese zh-tw Traditional Chinese an Aragonese ast Asturian eu Basque be Belarusian br
 * Breton cat Catalan gl Galician ht Haitian is Icelandic ga Irish ms Malay mt Maltese oc Occitan sr
 * Serbian cy Welsh yi Yiddish
 *
 */
