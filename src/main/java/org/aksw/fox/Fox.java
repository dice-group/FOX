package org.aksw.fox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.data.Relation;
import org.aksw.fox.nerlearner.TokenManager;
import org.aksw.fox.output.FoxJena;
import org.aksw.fox.output.IFoxJena;
import org.aksw.fox.tools.ATool;
import org.aksw.fox.tools.NERTools;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.fox.tools.linking.ILinking;
import org.aksw.fox.tools.linking.NoLinking;
import org.aksw.fox.tools.ner.INER;
import org.aksw.fox.tools.re.IRE;
import org.aksw.fox.tools.re.RETools;
import org.aksw.fox.utils.FoxTextUtil;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
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
  protected NERTools nerTools = null;

  /**
   *
   */
  protected RETools reTools = null;

  /**
   *
   */
  protected IFoxJena foxJena = new FoxJena();

  protected FoxUtil foxUtil = new FoxUtil();
  String startNER = "";
  String endNER = "";
  String startRE = "";
  String endRE = "";

  /**
   *
   * Constructor.
   *
   * @param lang
   */
  public Fox(final String lang) {

    this.lang = lang;

    final ToolsGenerator toolsGenerator = new ToolsGenerator();
    nerTools = toolsGenerator.getNERTools(lang);
    reTools = toolsGenerator.getRETools(lang);
  }

  @Override
  public String getResultsAndClean() {
    final String response = foxJena.print();
    foxJena.reset();
    return response;
  }

  /**
   *
   * @return
   */
  protected Set<Entity> doNER() {
    infoLog("Start NER (" + lang + ")...");

    final Set<Entity> entities;
    entities = nerTools.getEntities(parameter.get(FoxParameter.Parameter.INPUT.toString()));

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

  /**
   * Gets toolname to relations
   *
   * @param entities
   * @return toolname to relations
   */
  protected Map<String, Set<Relation>> doRE(final Set<Entity> entities) {
    final Map<String, Set<Relation>> relations = new HashMap<>();

    final List<IRE> tools = reTools.getRETool(lang);

    if (tools == null || tools.size() == 0) {
      infoLog("Relation tool for " + lang.toUpperCase() + " not supported yet.");
    } else {
      infoLog("Start RE with " + tools.size() + " tools ...");

      // use all tools to retrieve entities
      // start all threads
      final List<Fiber> fibers = new ArrayList<>();
      final CountDownLatch latch = new CountDownLatch(tools.size());
      for (final IRE tool : tools) {

        tool.setCountDownLatch(latch);
        tool.setInput(parameter.get(FoxParameter.Parameter.INPUT.toString()), entities);

        final Fiber fiber = new ThreadFiber();
        fibers.add(fiber);
        fiber.start();
        fiber.execute(tool);
      }

      // wait for finish
      final int min = Integer.parseInt(PropertiesLoader.get(NERTools.CFG_KEY_LIFETIME));
      try {
        latch.await(min, TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        LOG.error("Timeout after " + min + "min.");
        LOG.error(e.getLocalizedMessage(), e);
        LOG.error("input parameter:\n" + parameter.toString());
      }

      // shutdown threads
      for (final Fiber fiber : fibers) {
        fiber.dispose();
      }

      // get results
      if (latch.getCount() == 0) {

        for (final IRE tool : tools) {

          final Set<Relation> rs = tool.getResults();
          if (rs != null && !rs.isEmpty()) {
            relations.put(tool.getToolName(), rs);
          }
        }

      } else {
        infoLog("Timeout after " + min + " min.");
      }
      infoLog("RE done.");
    }

    return relations;
  }

  /**
   *
   * @param name
   * @return
   */
  protected Set<Entity> doNERLight(final String name) {
    infoLog("Starting light NER version ...");
    Set<Entity> entities = new HashSet<>();
    if (name == null || name.isEmpty()) {
      return entities;
    }

    INER nerLight = null;
    for (final INER t : nerTools.getNerTools()) {
      if (name.equals(t.getClass().getName())) {
        nerLight = t;
      }
    }

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

      final int min = Integer.parseInt(PropertiesLoader.get(NERTools.CFG_KEY_LIFETIME));
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
        entities = new HashSet<>(nerLight.getResults());

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
        if (e.getIndices() != null && e.getIndices().size() > 0) {
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
    if (entities != null && !entities.isEmpty()) {
      infoLog("Start NE linking ...");

      final CountDownLatch latch = new CountDownLatch(1);
      final ToolsGenerator toolsGenerator = new ToolsGenerator();
      if (linking == null) {
        try {
          linking = toolsGenerator.getDisambiguationTool(lang);
        } catch (final Exception e1) {
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
      final int min = Integer.parseInt(PropertiesLoader.get(NERTools.CFG_KEY_LIFETIME));
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
        entities = new HashSet<>(linking.getResults());
      } else {
        infoLog("Timeout after " + min + " min (" + linking.getClass().getName() + ").");
        // use dev lookup after timeout
        new NoLinking().setUris(entities, parameter.get(FoxParameter.Parameter.INPUT.toString()));
      }
      linking = null;
    }
    infoLog("Start NE linking done.");
  }

  protected void setOutput(final Set<Entity> entities, final Map<String, Set<Relation>> relations) {
    if (entities == null || relations == null) {
      LOG.warn("Parameter NULL");
    }

    final String input = parameter.get(FoxParameter.Parameter.INPUT.toString());
    final String output = parameter.get(FoxParameter.Parameter.OUTPUT.toString());
    final String docuri = parameter.get("docuri");

    infoLog("Preparing output format ...");

    foxJena.setLang(output);
    foxJena.addInput(input, docuri);

    String light = parameter.get(FoxParameter.Parameter.FOXLIGHT.toString());
    if (light == null || light.equalsIgnoreCase(FoxParameter.FoxLight.OFF.toString())) {
      light = this.getClass().getName();
    }

    foxJena.addEntities(entities, startNER, endNER, light, ATool.getToolVersion(light));
    for (final Entry<String, Set<Relation>> e : relations.entrySet()) {
      final Set<Relation> r = e.getValue();
      foxJena.addRelations(r, startRE, endRE, e.getKey(), ATool.getToolVersion(e.getKey()));
      infoLog("Found " + relations.size() + " relations.");
    }
    infoLog("Preparing output format done.");

    if (parameter.get("returnHtml") != null
        && parameter.get("returnHtml").toLowerCase().endsWith("true")) {
      html(entities, input);
    }
    infoLog("Found " + entities.size() + " entities.");
  }

  protected void html(final Set<Entity> entities, final String input) {

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
      if (entity.getUri() != null && !entity.getUri().trim().isEmpty()) {
        html += input.substring(last, index);
        html += "<a class=\"" + entity.getType().toLowerCase() + "\" href=\"" + entity.getUri()
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
      foxUtil.infotrace(nerTools, entities);
    }
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
      LOG.info("task: " + task + " light: " + light);

      Set<Entity> entities = new HashSet<>();
      Map<String, Set<Relation>> relations = new HashMap<>();

      if (input == null || task == null) {
        LOG.error("Input or task parameter not set.");
      } else {
        final TokenManager tokenManager = new TokenManager(input);
        // clean input
        input = tokenManager.getInput();
        parameter.put(FoxParameter.Parameter.INPUT.toString(), input);
        final boolean lightFox =
            light != null && !light.equalsIgnoreCase(FoxParameter.FoxLight.OFF.toString());
        switch (FoxParameter.Task.fromString(task.toLowerCase())) {
          case KE:
            LOG.warn("Operation not supported.");
            break;
          case NER:
            startNER = DatatypeConverter.printDateTime(new GregorianCalendar());
            entities = lightFox ? doNERLight(light) : doNER();
            endNER = DatatypeConverter.printDateTime(new GregorianCalendar());
            break;
          case RE:
            startNER = DatatypeConverter.printDateTime(new GregorianCalendar());
            entities = lightFox ? doNERLight(light) : doNER();
            endNER = DatatypeConverter.printDateTime(new GregorianCalendar());
            startRE = DatatypeConverter.printDateTime(new GregorianCalendar());
            relations = doRE(entities);
            endRE = DatatypeConverter.printDateTime(new GregorianCalendar());
            break;
          default:
            LOG.warn("Operation not supported.");
            break;
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

  @Override
  public void setParameter(final Map<String, String> parameter) {
    super.setParameter(parameter);

    String paraUriLookup = parameter.get(FoxParameter.Parameter.LINKING.toString());
    if (paraUriLookup != null) {
      if (paraUriLookup.equalsIgnoreCase(FoxParameter.Linking.OFF.toString())) {
        paraUriLookup = NoLinking.class.getName();
      }

      try {
        linking = (ILinking) PropertiesLoader.getClass(paraUriLookup.trim());
      } catch (final Exception e) {
        LOG.error("InterfaceURI not found. Check parameter: "
            + FoxParameter.Parameter.LINKING.toString());
      }
    }
  }
}
