package org.aksw.fox.tools.ner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.aksw.fox.data.exception.UnsupportedLangException;
import org.aksw.fox.tools.ner.linking.ILinking;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ToolsGenerator {
    public static Logger                          LOG                         = LogManager.getLogger(ToolsGenerator.class);
    public static final XMLConfiguration          CFG                         = CfgManager.getCfg(ToolsGenerator.class);

    public static final String                    CFG_KEY_SUPPORTED_LANG      = "toolsGenerator.lang";
    public static final String                    CFG_KEY_USED_LANG           = "toolsGenerator.usedLang";
    public static final String                    CFG_KEY_NER_TOOLS           = "toolsGenerator.nerTools";
    public static final String                    CFG_KEY_DISAMBIGUATION_TOOL = "toolsGenerator.disambiguationTool";
    public static final String                    CFG_KEY_LIGHT_TOOL          = "toolsGenerator.lightTool";

    @SuppressWarnings("unchecked")
    public static final Set<String>               supportedLang               = new HashSet<String>(CFG.getList(CFG_KEY_SUPPORTED_LANG));
    @SuppressWarnings("unchecked")
    public static final Set<String>               usedLang                    = new HashSet<String>(CFG.getList(CFG_KEY_USED_LANG));

    public static final Map<String, String>       disambiguationTools         = new HashMap<>();
    public static final Map<String, List<String>> nerTools                    = new HashMap<>();
    // public static final Map<String, String> nerLightTool = new HashMap<>();
    static {
        init();
    }

    /**
     * Read xml cfg.
     */
    @SuppressWarnings("unchecked")
    public static void init() {
        if (supportedLang.containsAll(usedLang)) {
            for (String lang : usedLang) {
                String disambiguationTool = CFG.getString(CFG_KEY_DISAMBIGUATION_TOOL.concat("[@").concat(lang).concat("]"));
                if (disambiguationTool != null && !disambiguationTool.isEmpty())
                    disambiguationTools.put(lang, disambiguationTool);

                List<String> tools = new ArrayList<String>(CFG.getList(CFG_KEY_NER_TOOLS.concat("[@").concat(lang).concat("]")));
                tools.remove("");
                tools.remove(null);
                Collections.sort(tools);
                if (!tools.isEmpty())
                    nerTools.put(lang, tools);

                /*
                String lightTool = CFG.getString(CFG_KEY_LIGHT_TOOL.concat("[@").concat(lang).concat("]"));
                if (lightTool != null && !lightTool.isEmpty())
                    nerLightTool.put(lang, lightTool);
                    */
            }
        } else {
            Set<String> l = new HashSet<>();
            l.addAll(usedLang);
            l.removeAll(supportedLang);
            // throw new UnsupportedLangException(l.toString());
        }

        LOG.info("disambiguationTools:" + disambiguationTools);
        LOG.info("nerTools" + nerTools);
        // LOG.info("nerLightTool" + nerLightTool);
    }

    /*
        public INER getNERLightTool(String lang) throws UnsupportedLangException, LoadingNotPossibleException {
            if (usedLang.contains(lang) && nerLightTool.get(lang) != null && !nerLightTool.get(lang).isEmpty()) {
                return (INER) FoxCfg.getClass(nerLightTool.get(lang));
            } else
                throw new UnsupportedLangException("Language " + lang + " is not supported.");
        }
    */
    /**
     * 
     * @param lang
     * @return
     * @throws UnsupportedLangException
     * @throws LoadingNotPossibleException
     */
    public Tools getNERTools(String lang) throws UnsupportedLangException, LoadingNotPossibleException {
        if (usedLang.contains(lang) && nerTools.get(lang) != null && !nerTools.get(lang).isEmpty()) {
            Tools tools = new Tools(nerTools.get(lang), lang);
            return tools;
        } else
            throw new UnsupportedLangException("Language " + lang + " is not supported.");
    }

    /**
     * 
     * @param lang
     * @return
     * @throws UnsupportedLangException
     * @throws LoadingNotPossibleException
     */
    public ILinking getDisambiguationTool(String lang) throws UnsupportedLangException, LoadingNotPossibleException {
        if (usedLang.contains(lang)) {
            if (disambiguationTools.get(lang) != null)
                return (ILinking) FoxCfg.getClass(disambiguationTools.get(lang));
            else
                throw new UnsupportedLangException("Disambiguation tool for language " + lang + " is not supported");
        } else
            throw new UnsupportedLangException("Language " + lang + " is not supported.");
    }

    public static void main(String[] a) throws UnsupportedLangException, LoadingNotPossibleException {
        ToolsGenerator tg = new ToolsGenerator();
        for (String l : usedLang)
            tg.getNERTools(l);
    }
}
