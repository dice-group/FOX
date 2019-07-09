package org.aksw.fox;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Fox extends AFox {

  public static final String cfgFolder = "data/fox/cfg";

  protected final ToolsGenerator toolsGenerator = new ToolsGenerator();

  protected ILinking linking = null;

  protected IFoxJena foxJena = new FoxJena();

  protected FoxUtil foxUtil = new FoxUtil();

  private String startNER = "";
  private String endNER = "";
  private String startRE = "";
  private String endRE = "";

  /**
   *
   * Constructor.
   *
   * @param lang
   */
  public Fox(final String lang) {
    this.lang = lang;
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
  protected List<Entity> doNER() {
    LOG.info("Start NER (" + lang + ")...");

    final List<Entity> entities = toolsGenerator//
        .getNERTools(lang)//
        .getEntities(//
            parameter.get(FoxParameter.Parameter.INPUT.toString())//
        );

    // TODO: remove me
    /**
     * <code>
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
    </code>
     */
    LOG.info("NER done.");
    return entities;
  }

  /**
   * Gets toolname to relations
   *
   * @param entities
   * @return toolname to relations
   */
  protected Map<String, Set<Relation>> doRE(final List<Entity> entities) {
    final Map<String, Set<Relation>> relations = new HashMap<>();

    final List<IRE> tools = toolsGenerator.getRETools(lang).getRETool(lang);

    if (tools == null || tools.size() == 0) {
      LOG.info("Relation tool for " + lang.toUpperCase() + " not supported yet.");
    } else {
      LOG.info("Start RE with " + tools.size() + " tools ...");

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
        LOG.info("Timeout after " + min + " min.");
      }
      LOG.info("RE done.");
    }
    return relations;
  }

  protected List<Entity> doNERLight(final String name) {

    LOG.debug("Starting light NER version ...");

    List<Entity> entities = new ArrayList<>();
    if (name == null || name.isEmpty()) {
      return entities;
    }

    INER nerLight = null;
    for (final INER t : toolsGenerator.getNERTools(lang).getNerTools()) {
      if (name.equals(t.getClass().getName())) {
        nerLight = t;
      }
    }

    if (nerLight == null) {
      LOG.info("Given (" + name + ") tool is not supported.");
      return entities;
    }

    LOG.info("NER tool(" + lang + ") is: " + nerLight.getToolName());

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
        LOG.error("input:\n" + parameter.get(FoxParameter.Parameter.INPUT.toString()));
        LOG.error(e.getLocalizedMessage(), e);
      }

      // shutdown threads
      fiber.dispose();
      // get results
      if (latch.getCount() == 0) {
        entities = new ArrayList<>(nerLight.getResults());

      } else {
        // TODO: handle ner light timeout.
        LOG.debug("timeout after " + min + "min.");
      }
    }

    tokenManager.repairEntities(entities);

    /**
     * 
     * // make an index for each entity final Map<Integer, Entity> indexMap = new HashMap<>();
     * 
     * for (final Entity entity : entities) { for (final Integer i : FoxTextUtil//
     * .getIndices(entity.getText(), tokenManager.getTokenInput())) { indexMap.put(i, entity); } }
     * 
     * // loop index in sorted order
     * 
     * int offset = -1; for (final Integer i :
     * indexMap.keySet().stream().sorted().collect(Collectors.toList())) { final Entity e =
     * indexMap.get(i); if (offset < i) { offset = i + e.getText().length(); e.addIndicies(i); } }
     * 
     */
    nerLight = null;
    LOG.info("Light version done.");

    return entities;
  }

  protected void setURIs(List<Entity> entities) {
    if (entities != null && !entities.isEmpty()) {
      LOG.info("Start NE linking ...");

      final CountDownLatch latch = new CountDownLatch(1);

      if (linking == null) {
        try {
          linking = toolsGenerator.getDisambiguationTool(lang);
        } catch (final Exception e1) {
          LOG.info(e1.getLocalizedMessage());
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
        entities = new ArrayList<>(linking.getResults());
      } else {
        LOG.info("Timeout after " + min + " min (" + linking.getClass().getName() + ").");
        // use dev lookup after timeout
        new NoLinking().setUris(entities, parameter.get(FoxParameter.Parameter.INPUT.toString()));
      }
      linking = null;
    }
    LOG.info("Start NE linking done.");
  }

  protected void setOutput(final List<Entity> entities,
      final Map<String, Set<Relation>> relations) {

    // checks parameters
    if (entities != null && relations != null) {

      final String input = parameter.get(FoxParameter.Parameter.INPUT.toString());
      final String output = parameter.get(FoxParameter.Parameter.OUTPUT.toString());
      final String docuri = parameter.get("docuri");

      LOG.info("Preparing output format ...");

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
        LOG.info("Found " + relations.size() + " relations.");
      }
      LOG.info("Preparing output format done.");

      LOG.info("Found " + entities.size() + " entities.");
    }
  }

  /**
   *
   */
  @Override
  public void run() {

    if (parameter == null) {
      LOG.error("Parameter not set.");
    } else {
      String input = parameter.get(FoxParameter.Parameter.INPUT.toString());
      final String task = parameter.get(FoxParameter.Parameter.TASK.toString());
      final String light = parameter.get(FoxParameter.Parameter.FOXLIGHT.toString());
      LOG.info("task: " + task + " light: " + light);

      List<Entity> entities = new ArrayList<>();
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
