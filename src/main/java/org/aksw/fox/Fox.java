package org.aksw.fox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.aksw.fox.data.exception.UnsupportedLangException;
import org.aksw.fox.tools.Tools;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.fox.tools.ner.INER;
import org.aksw.fox.tools.ner.linking.ILinking;
import org.aksw.fox.tools.ner.linking.NoLinking;
import org.aksw.fox.tools.re.FoxRETools;
import org.aksw.fox.tools.re.IRE;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxJena;
import org.aksw.fox.utils.FoxTextUtil;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Fox extends AFox {

  /**
   *
   */
  protected ILinking linking = null;

  /**
   *
   */
  protected Tools nerTools = null;

  /**
   *
   */
  protected FoxRETools reTools = new FoxRETools();

  /**
   *
   */
  protected FoxJena foxJena = new FoxJena();

  /**
   *
   * Constructor.
   *
   * @param lang
   */
  public Fox(final String lang) {
    this.lang = lang;

    try {
      final ToolsGenerator toolsGenerator = new ToolsGenerator();
      nerTools = toolsGenerator.getNERTools(lang);
    } catch (UnsupportedLangException | LoadingNotPossibleException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  protected Set<Entity> doNER() {
    infoLog("Start NER (" + lang + ")...");
    final Set<Entity> entities =
        nerTools.getEntities(parameter.get(FoxParameter.Parameter.INPUT.toString()));

    // remove duplicate annotations
    final Map<String, Entity> wordEntityMap = new HashMap<>();
    for (final Entity entity : entities) {
      if (wordEntityMap.get(entity.getText()) == null) {
        wordEntityMap.put(entity.getText(), entity);
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("We have a duplicate annotation and removing those: " + entity.getText() + " "
              + entity.getType() + " " + wordEntityMap.get(entity.getText()).getType());
        }
        wordEntityMap.remove(entity.getText());
      }
    }
    // remove
    entities.retainAll(wordEntityMap.values());
    infoLog("NER done.");
    return entities;
  }

  protected Set<Relation> doRE() {
    Set<Relation> relations = null;
    final IRE reTool = reTools.getRETool(lang);
    if (reTool == null) {
      infoLog("Relation tool for " + lang.toUpperCase() + " not supported yet.");
    } else {
      infoLog("Start RE ...");
      final CountDownLatch latch = new CountDownLatch(1);
      reTool.setCountDownLatch(latch);
      reTool.setInput(parameter.get(FoxParameter.Parameter.INPUT.toString()), null); // TODO: null

      final Fiber fiber = new ThreadFiber();
      fiber.start();
      fiber.execute(reTool);

      final int min = Integer.parseInt(FoxCfg.get(Tools.CFG_KEY_LIFETIME));
      try {
        latch.await(min, TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      // shutdown threads
      fiber.dispose();

      // get results
      if (latch.getCount() == 0) {
        relations = reTool.getResults();
      } else {
        infoLog("Timeout after " + min + " min.");
      }
      infoLog("RE done.");
    }
    return relations;
  }

  protected Set<Entity> doNERLight(final String name) {
    infoLog("Starting light NER version ...");
    Set<Entity> entities = new HashSet<>();
    if ((name == null) || name.isEmpty()) {
      return entities;
    }

    INER nerLight = null;
    for (final INER t : nerTools.getNerTools()) {
      if (name.equals(t.getClass().getName())) {
        nerLight = t;
      }
    }
    /*
     * // loads a tool try { nerLight = (INER) FoxCfg.getClass(name); } catch
     * (LoadingNotPossibleException e) { LOG.error(e.getLocalizedMessage(), e); return entities; }
     */
    if (nerLight == null) {
      LOG.info("Given (" + name + ") tool is not supported.");
      return entities;
    }

    infoLog("NER tool(" + lang + ") is: " + nerLight.getToolName());

    // clean input
    String input = parameter.get(FoxParameter.Parameter.INPUT.toString());
    final TokenManager tokenManager = new TokenManager(input);
    input = tokenManager.getInput();
    parameter.put(FoxParameter.Parameter.INPUT.toString(), input);

    {
      final CountDownLatch latch = new CountDownLatch(1);
      final Fiber fiber = new ThreadFiber();
      nerLight.setCountDownLatch(latch);
      nerLight.setInput(parameter.get(FoxParameter.Parameter.INPUT.toString()));

      fiber.start();
      fiber.execute(nerLight);

      final int min = Integer.parseInt(FoxCfg.get(Tools.CFG_KEY_LIFETIME));
      try {
        latch.await(min, TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        LOG.error("Timeout after " + min + " min.");
        LOG.error("\n", e);
        LOG.error("input:\n" + parameter.get(FoxParameter.Parameter.INPUT.toString()));
      }

      // shutdown threads
      fiber.dispose();
      // get results
      if (latch.getCount() == 0) {
        entities = new HashSet<Entity>(nerLight.getResults());

      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("timeout after " + min + "min.");
          // TODO: handle timeout
        }
      }
    }

    tokenManager.repairEntities(entities);

    // make an index for each entity
    final Map<Integer, Entity> indexMap = new HashMap<>();
    for (final Entity entity : entities) {
      for (final Integer i : FoxTextUtil.getIndices(entity.getText(),
          tokenManager.getTokenInput())) {
        indexMap.put(i, entity);
      }
    }

    // sort
    final List<Integer> sortedIndices = new ArrayList<>(indexMap.keySet());
    Collections.sort(sortedIndices);

    // loop index in sorted order
    int offset = -1;
    for (final Integer i : sortedIndices) {
      final Entity e = indexMap.get(i);
      if (offset < i) {
        offset = i + e.getText().length();
        e.addIndicies(i);
      }
    }

    // remove entity without an index
    {
      final Set<Entity> cleanEntity = new HashSet<>();
      for (final Entity e : entities) {
        if ((e.getIndices() != null) && (e.getIndices().size() > 0)) {
          cleanEntity.add(e);
        }
      }
      entities = cleanEntity;
    }

    nerLight = null;
    infoLog("Light version done.");
    return entities;
  }

  protected void setURIs(Set<Entity> entities) {
    if ((entities != null) && !entities.isEmpty()) {
      infoLog("Start NE linking ...");

      final CountDownLatch latch = new CountDownLatch(1);
      final ToolsGenerator toolsGenerator = new ToolsGenerator();
      if (linking == null) {
        try {
          linking = toolsGenerator.getDisambiguationTool(lang);
        } catch (UnsupportedLangException | LoadingNotPossibleException e1) {
          infoLog(e1.getLocalizedMessage());
          linking = new NoLinking();
        }
      }
      linking.setCountDownLatch(latch);
      linking.setInput(entities, parameter.get(FoxParameter.Parameter.INPUT.toString()));

      final Fiber fiber = new ThreadFiber();
      fiber.start();
      fiber.execute(linking);

      // use another time for the uri lookup?
      final int min = Integer.parseInt(FoxCfg.get(Tools.CFG_KEY_LIFETIME));
      try {
        latch.await(min, TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        LOG.error("Timeout after " + min + "min.");
        LOG.error("\n", e);
      }

      // shutdown threads
      fiber.dispose();
      // get results
      if (latch.getCount() == 0) {
        entities = new HashSet<Entity>(linking.getResults());
      } else {
        infoLog("Timeout after " + min + " min (" + linking.getClass().getName() + ").");
        // use dev lookup after timeout
        new NoLinking().setUris(entities, parameter.get(FoxParameter.Parameter.INPUT.toString()));
      }
      linking = null;
    }
    infoLog("Start NE linking done.");
  }

  protected void setOutput(final Set<Entity> entities, final Set<Relation> relations) {
    if (entities == null) {
      LOG.warn("Entities are empty.");
      return;
    }

    final String input = parameter.get(FoxParameter.Parameter.INPUT.toString());

    // switch output
    final boolean useNIF =
        Boolean.parseBoolean(parameter.get(FoxParameter.Parameter.NIF.toString()));

    final String out = parameter.get(FoxParameter.Parameter.OUTPUT.toString());
    infoLog("Preparing output format ...");

    foxJena.clearGraph();
    foxJena.setAnnotations(entities);

    if (relations != null) {
      foxJena.setRelations(relations);
      infoLog("Found " + relations.size() + " relations.");
    }

    response = foxJena.print(out, useNIF, input);
    infoLog("Preparing output format done.");

    if ((parameter.get("returnHtml") != null)
        && parameter.get("returnHtml").toLowerCase().endsWith("true")) {

      final Map<Integer, Entity> indexEntityMap = new HashMap<>();
      for (final Entity entity : entities) {
        for (final Integer startIndex : entity.getIndices()) {
          // TODO : check contains
          indexEntityMap.put(startIndex, entity);
        }
      }

      final Set<Integer> startIndices = new TreeSet<>(indexEntityMap.keySet());

      String html = "";

      int last = 0;
      for (final Integer index : startIndices) {
        final Entity entity = indexEntityMap.get(index);
        if ((entity.uri != null) && !entity.uri.trim().isEmpty()) {
          html += input.substring(last, index);
          html += "<a class=\"" + entity.getType().toLowerCase() + "\" href=\"" + entity.uri
              + "\"  target=\"_blank\"  title=\"" + entity.getType().toLowerCase() + "\" >"
              + entity.getText() + "</a>";
          last = index + entity.getText().length();
        } else {
          LOG.error("Entity has no URI: " + entity.getText());
        }
      }

      html += input.substring(last);
      parameter.put(FoxParameter.Parameter.INPUT.toString(), html);

      if (LOG.isTraceEnabled()) {
        infotrace(entities);
      }
    }
    infoLog("Found " + entities.size() + " entities.");
  }

  /**
   *
   */
  @Override
  public void run() {
    super.run();
    infoLog("Running Fox...");

    if (parameter == null) {
      LOG.error("Parameter not set.");
    } else {
      String input = parameter.get(FoxParameter.Parameter.INPUT.toString());
      final String task = parameter.get(FoxParameter.Parameter.TASK.toString());
      final String light = parameter.get(FoxParameter.Parameter.FOXLIGHT.toString());

      Set<Entity> entities = null;
      Set<Relation> relations = null;

      if ((input == null) || (task == null)) {
        LOG.error("Input or task parameter not set.");
      } else {
        final TokenManager tokenManager = new TokenManager(input);
        // clean input
        input = tokenManager.getInput();
        parameter.put(FoxParameter.Parameter.INPUT.toString(), input);

        // light version
        if ((light != null) && !light.equalsIgnoreCase(FoxParameter.FoxLight.OFF.toString())) {

          switch (FoxParameter.Task.fromString(task.toLowerCase())) {
            case KE:
              throw new UnsupportedOperationException();

            case NER:
              entities = doNERLight(light);
              break;
            case RE:
              throw new UnsupportedOperationException();
            default:
              throw new UnsupportedOperationException();
          }

        } else {
          // no light version
          switch (FoxParameter.Task.fromString(task.toLowerCase())) {
            case KE:
              throw new UnsupportedOperationException();
            case NER:
              entities = doNER();
              break;
            case RE:
              entities = doNER();
              relations = doRE();
              break;
            default:
              throw new UnsupportedOperationException();
          }
        }
      }

      setURIs(entities);
      setOutput(entities, relations);
    }

    // done
    infoLog("Running Fox done.");
    if (countDownLatch != null) {
      countDownLatch.countDown();
    }
  }

  /**
   * Prints debug infos about entities for each tool and final entities in fox.
   *
   * @param entities final entities
   */
  private void infotrace(final Set<Entity> entities) {
    if (LOG.isTraceEnabled()) {

      LOG.trace("entities:");
      for (final String toolname : nerTools.getToolResult().keySet()) {
        if (nerTools.getToolResult().get(toolname) == null) {
          return;
        }

        LOG.trace(toolname + ": " + nerTools.getToolResult().get(toolname).size());
        for (final Entity e : nerTools.getToolResult().get(toolname)) {
          LOG.trace(e);
        }
      }

      LOG.trace("fox" + ": " + entities.size());
      for (final Entity e : entities) {
        LOG.trace(e);
      }
    }
  }

  @Override
  public void setParameter(final Map<String, String> parameter) {
    super.setParameter(parameter);

    String paraUriLookup = parameter.get(FoxParameter.Parameter.LINKING.toString());
    if (paraUriLookup != null) {
      if (paraUriLookup.equalsIgnoreCase("off")) {
        paraUriLookup = NoLinking.class.getName();
      }

      try {
        linking = (ILinking) FoxCfg.getClass(paraUriLookup.trim());
      } catch (final Exception e) {
        LOG.error("InterfaceURI not found. Check parameter: "
            + FoxParameter.Parameter.LINKING.toString());
      }
    }
  }

  private void infoLog(final String m) {
    if (foxWebLog != null) {
      foxWebLog.setMessage(m);
    }
    LOG.info(m);
  }
}
