package org.aksw.fox.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.fox.Fox;
import org.aksw.fox.tools.linking.ILinking;
import org.aksw.fox.tools.re.RETools;
import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

interface IToolsGenerator {
  // FIXME: implement me
}


/**
 * Reads the config file and initializes the tools to be needed.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class ToolsGenerator implements IToolsGenerator {

  public static final Logger LOG = LogManager.getLogger(ToolsGenerator.class);
  public static final XMLConfiguration CFG =
      new CfgManager(Fox.cfgFolder).getCfg(ToolsGenerator.class);

  public static final String CFG_KEY_SUPPORTED_LANG = "toolsGenerator.lang";
  public static final String CFG_KEY_USED_LANG = "toolsGenerator.usedLang";
  public static final String CFG_KEY_NER_TOOLS = "toolsGenerator.nerTools";
  public static final String CFG_KEY_RE_TOOLS = "toolsGenerator.reTools";
  public static final String CFG_KEY_DISAMBIGUATION_TOOL = "toolsGenerator.disambiguationTool";
  public static final String CFG_KEY_LIGHT_TOOL = "toolsGenerator.lightTool";

  public static final Set<String> supportedLang = ((List<?>) CFG.getList(CFG_KEY_SUPPORTED_LANG))//
      .stream().map(p -> p.toString()).collect(Collectors.toSet());

  public static final Set<String> usedLang = ((List<?>) CFG.getList(CFG_KEY_USED_LANG))//
      .stream().map(p -> p.toString()).collect(Collectors.toSet());

  public static final Map<String, String> disambiguationTools = new HashMap<>();

  public static final Map<String, List<String>> nerTools = new HashMap<>();

  public static final Map<String, List<String>> reTools = new HashMap<>();
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

        // disambiguation tool
        final String disambiguationTool = CFG.getString(CFG_KEY_DISAMBIGUATION_TOOL.concat(key));
        if (disambiguationTool != null && !disambiguationTool.isEmpty()) {
          disambiguationTools.put(lang, disambiguationTool);
        }

        // ner tools
        final List<String> nertools = ((List<?>) CFG.getList(CFG_KEY_NER_TOOLS.concat(key)))//
            .stream().map(p -> p.toString()).collect(Collectors.toList())//
            .stream().sorted().collect(Collectors.toList());
        if (!nertools.isEmpty()) {
          nerTools.put(lang, nertools);
        }

        // re tools
        final List<String> retools = ((List<?>) CFG.getList(CFG_KEY_RE_TOOLS.concat(key)))//
            .stream().map(p -> p.toString()).collect(Collectors.toList())//
            .stream().sorted().collect(Collectors.toList());
        if (!retools.isEmpty()) {
          reTools.put(lang, retools);
        }
      }
    } else {
      final Set<String> l = new HashSet<>();
      l.addAll(usedLang);
      l.removeAll(supportedLang);
      LOG.warn("language ".concat(l.toString()).concat(" is not supported."));
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
  public NERTools getNERTools(final String lang) {
    if (usedLang.contains(lang) && nerTools.get(lang) != null && !nerTools.get(lang).isEmpty()) {
      final NERTools tools = new NERTools(nerTools.get(lang), lang);
      return tools;
    } else {
      return new NERTools(new ArrayList<>(), lang);
    }
  }

  public RETools getRETools(final String lang) {
    if (usedLang.contains(lang) && reTools.get(lang) != null && !reTools.get(lang).isEmpty()) {
      final RETools tools = new RETools(reTools.get(lang), lang);
      return tools;
    } else {
      return new RETools(new ArrayList<>(), lang);
    }
  }

  /**
   *
   * @param lang
   * @return
   * @throws IOException
   */
  public ILinking getDisambiguationTool(final String lang) throws IOException {
    if (usedLang.contains(lang)) {
      if (disambiguationTools.get(lang) != null) {
        return (ILinking) PropertiesLoader.getClass(disambiguationTools.get(lang));
      } else {
        throw new UnsupportedOperationException(
            "Disambiguation tool for language " + lang + " is not supported");
      }
    } else {
      throw new UnsupportedOperationException("Language " + lang + " is not supported.");
    }
  }

  public static void main(final String[] a) throws UnsupportedOperationException {
    final ToolsGenerator tg = new ToolsGenerator();
    for (final String l : usedLang) {
      tg.getNERTools(l);
    }
  }
}
