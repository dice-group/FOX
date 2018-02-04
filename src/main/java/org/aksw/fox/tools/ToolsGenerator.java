package org.aksw.fox.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.fox.exception.LoadingNotPossibleException;
import org.aksw.fox.exception.UnsupportedLangException;
import org.aksw.fox.tools.linking.ILinking;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

interface IToolsGenerator {

}


public class ToolsGenerator implements IToolsGenerator {

  public static final Logger LOG = LogManager.getLogger(ToolsGenerator.class);
  public static final XMLConfiguration CFG = CfgManager.getCfg(ToolsGenerator.class);

  public static final String CFG_KEY_SUPPORTED_LANG = "toolsGenerator.lang";
  public static final String CFG_KEY_USED_LANG = "toolsGenerator.usedLang";
  public static final String CFG_KEY_NER_TOOLS = "toolsGenerator.nerTools";
  public static final String CFG_KEY_DISAMBIGUATION_TOOL = "toolsGenerator.disambiguationTool";
  public static final String CFG_KEY_LIGHT_TOOL = "toolsGenerator.lightTool";

  public static final Set<String> supportedLang = ((List<?>) CFG.getList(CFG_KEY_SUPPORTED_LANG))//
      .stream().map(p -> p.toString()).collect(Collectors.toSet());

  public static final Set<String> usedLang = ((List<?>) CFG.getList(CFG_KEY_USED_LANG))//
      .stream().map(p -> p.toString()).collect(Collectors.toSet());

  public static final Map<String, String> disambiguationTools = new HashMap<>();
  public static final Map<String, List<String>> nerTools = new HashMap<>();

  static {
    init();
  }

  /**
   * Read xml cfg.
   */
  public static void init() {
    if (supportedLang.containsAll(usedLang)) {
      for (final String lang : usedLang) {

        final String key = "[@".concat(lang).concat("]");
        final String disambiguationTool = CFG.getString(CFG_KEY_DISAMBIGUATION_TOOL.concat(key));
        if ((disambiguationTool != null) && !disambiguationTool.isEmpty()) {
          disambiguationTools.put(lang, disambiguationTool);
        }

        final List<String> tools = ((List<?>) CFG.getList(CFG_KEY_NER_TOOLS.concat(key)))//
            .stream().map(p -> p.toString()).collect(Collectors.toList());

        Collections.sort(tools);
        if (!tools.isEmpty()) {
          nerTools.put(lang, tools);
        }
      }
    } else {
      final Set<String> l = new HashSet<>();
      l.addAll(usedLang);
      l.removeAll(supportedLang);
    }

    LOG.info("disambiguationTools:" + disambiguationTools);
    LOG.info("nerTools" + nerTools);
  }

  /**
   *
   * @param lang
   * @return
   * @throws UnsupportedLangException
   * @throws LoadingNotPossibleException
   */
  public Tools getNERTools(final String lang)
      throws UnsupportedLangException, LoadingNotPossibleException {
    if (usedLang.contains(lang) && (nerTools.get(lang) != null) && !nerTools.get(lang).isEmpty()) {
      final Tools tools = new Tools(nerTools.get(lang), lang);
      return tools;
    } else {
      throw new UnsupportedLangException("Language " + lang + " is not supported.");
    }
  }

  /**
   *
   * @param lang
   * @return
   * @throws UnsupportedLangException
   * @throws LoadingNotPossibleException
   */
  public ILinking getDisambiguationTool(final String lang)
      throws UnsupportedLangException, LoadingNotPossibleException {
    if (usedLang.contains(lang)) {
      if (disambiguationTools.get(lang) != null) {
        return (ILinking) FoxCfg.getClass(disambiguationTools.get(lang));
      } else {
        throw new UnsupportedLangException(
            "Disambiguation tool for language " + lang + " is not supported");
      }
    } else {
      throw new UnsupportedLangException("Language " + lang + " is not supported.");
    }
  }

  public static void main(final String[] a)
      throws UnsupportedLangException, LoadingNotPossibleException {
    final ToolsGenerator tg = new ToolsGenerator();
    for (final String l : usedLang) {
      tg.getNERTools(l);
    }
  }
}
