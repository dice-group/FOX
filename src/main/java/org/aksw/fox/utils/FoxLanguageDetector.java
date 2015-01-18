package org.aksw.fox.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
    public static Logger       LOG              = LogManager.getLogger(FoxLanguageDetector.class);

    public static Set<Langs>   lang             = new HashSet<>(Arrays.asList(
                                                        Langs.DE,
                                                        Langs.EN,
                                                        Langs.FR
                                                        ));

    protected LanguageDetector languageDetector = null;

    /**
     * Test
     * 
     * @param a
     */
    public static void main(String[] a) {
        FoxLanguageDetector ld = new FoxLanguageDetector();
        Langs lang = ld.detect("That is a test.");
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
    public FoxLanguageDetector(Set<Langs> lang) {
        initLanguageDetector(lang);
    }

    protected void initLanguageDetector(Set<Langs> lang) {
        Set<String> langs = new HashSet<>();
        for (Langs l : lang)
            langs.add(l.toString());

        try {
            // build language detetor:
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(new LanguageProfileReader().read(langs))
                    .build();
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    public Langs detect(String text) {
        TextObject textObject = CommonTextObjectFactories.forDetectingOnLargeText().forText(text);
        String lang = languageDetector.getProbabilities(textObject).get(0).getLanguage();
        return Langs.fromString(lang);
    }

    /*
     * Enum
     */
    public enum Langs {

        DE("de"),
        EN("en"),
        FR("fr");

        private String label;

        Langs(String text) {
            this.label = text;
        }

        @Override
        public String toString() {
            return this.label;
        }

        public static Langs fromString(String label) {
            if (label != null)
                for (Langs b : Langs.values())
                    if (label.equalsIgnoreCase(b.label))
                        return b;
            return null;
        }
    }
}

/*

af Afrikaans
ar Arabic
bg Bulgarian
bn Bengali
cs Czech
da Danish
de German
el Greek
en English
es Spanish
et Estonian
fa Persian
fi Finnish
fr French
gu Gujarati
he Hebrew
hi Hindi
hr Croatian
hu Hungarian
id Indonesian
it Italian
ja Japanese
kn Kannada
ko Korean
lt Lithuanian
lv Latvian
mk Macedonian
ml Malayalam
mr Marathi
ne Nepali
nl Dutch
no Norwegian
pa Punjabi
pl Polish
pt Portuguese
ro Romanian
ru Russian
sk Slovak
sl Slovene
so Somali
sq Albanian
sv Swedish
sw Swahili
ta Tamil
te Telugu
th Thai
tl Tagalog
tr Turkish
uk Ukrainian
ur Urdu
vi Vietnamese
zh-cn Simplified Chinese
zh-tw Traditional Chinese
an Aragonese
ast Asturian
eu Basque
be Belarusian
br Breton
cat Catalan
gl Galician
ht Haitian
is Icelandic
ga Irish
ms Malay
mt Maltese
oc Occitan
sr Serbian
cy Welsh
yi Yiddish
 
 */
