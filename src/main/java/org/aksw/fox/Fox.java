package org.aksw.fox;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.aksw.fox.tools.ner.FoxNERTools;
import org.aksw.fox.tools.ner.INER;
import org.aksw.fox.tools.ner.en.NERStanford;
import org.aksw.fox.tools.re.FoxRETools;
import org.aksw.fox.tools.re.IRE;
import org.aksw.fox.uri.AGDISTISLookup;
import org.aksw.fox.uri.ILookup;
import org.aksw.fox.uri.NullLookup;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.aksw.fox.utils.FoxJena;
import org.aksw.fox.utils.FoxTextUtil;
import org.aksw.fox.utils.FoxWebLog;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

/**
 * An implementation of {@link org.aksw.fox.IFox}.
 * 
 * @author rspeck
 * 
 */
public class Fox implements IFox {
    public static final String  CFG_KEY_URI_LOOKUP        = Fox.class.getName().concat(".urilookup");
    public static final String  CFG_KEY_DEFAULT_LIGHT_NER = Fox.class.getName().concat(".defaultLightNER");

    public static final Logger  LOG                       = LogManager.getLogger(Fox.class);

    /**
     * 
     */
    protected ILookup           uriLookup                 = null;

    /**
     * 
     */
    protected FoxNERTools       nerTools                  = null;
    protected FoxRETools        reTools                   = null;

    /**
     * 
     */
    protected TokenManager      tokenManager              = null;

    /**
     * 
     */
    protected FoxJena           foxJena                   = new FoxJena();

    /**
     * Holds a tool for fox's light version.
     */
    protected INER              nerLight                  = null;

    /**
     * 
     */
    protected FoxWebLog         foxWebLog                 = null;

    private CountDownLatch      countDownLatch            = null;
    private Map<String, String> parameter                 = null;
    private String              response                  = null;

    /**
     * 
     */
    public Fox() {
        // load class in fox.properties file
        if (FoxCfg.get(CFG_KEY_URI_LOOKUP) != null)
            try {
                uriLookup = (ILookup) FoxCfg.getClass(FoxCfg.get(CFG_KEY_URI_LOOKUP).trim());
            } catch (Exception e) {
                LOG.error("InterfaceURI not found. Check your " + FoxCfg.CFG_FILE + " file and the " + CFG_KEY_URI_LOOKUP + " key.");
            }

        if (uriLookup == null)
            uriLookup = new AGDISTISLookup();

        nerTools = new FoxNERTools();
        reTools = new FoxRETools();
    }

    /**
     * 
     * @param uriLookup
     * @param nerLight
     */
    public Fox(ILookup uriLookup, INER nerLight) {
        this.uriLookup = uriLookup;
        this.nerLight = nerLight;
        this.nerTools = new FoxNERTools();
    }

    /**
     * Searches for an instance in FoxNERTools to set the NER light version.
     */
    protected void setLightVersionNER() {
        if (nerLight == null) {
            // old version
            String lightversionner = parameter.get(FoxCfg.parameter_foxlight).trim();
            List<String> old = Arrays.asList("org.aksw.fox.nertools.NERSpotlight", "org.aksw.fox.nertools.NERBalie", "org.aksw.fox.nertools.NERIllinoisExtended", "org.aksw.fox.nertools.NERStanford", "org.aksw.fox.nertools.NEROpenNLP");
            if (old.contains(lightversionner)) {
                lightversionner = lightversionner.replace("fox.nertools", "fox.tools.ner.en");
            }

            for (INER tool : nerTools.getNerTools())
                if (lightversionner.equals(tool.getClass().getName())) {
                    nerLight = tool;
                    break;
                }
        }

        if (nerLight == null) {
            if (FoxCfg.get(CFG_KEY_DEFAULT_LIGHT_NER) != null)
                try {
                    nerLight = (INER) FoxCfg.getClass(FoxCfg.get(CFG_KEY_DEFAULT_LIGHT_NER).trim());
                } catch (Exception e) {
                    LOG.error("INER not found. Check your " + FoxCfg.CFG_FILE + " file and the " + CFG_KEY_DEFAULT_LIGHT_NER + " key");
                }

            if (nerLight == null)
                nerLight = new NERStanford();

        }
    }

    protected Set<Entity> doNER() {
        LOG.info("starting ner ...");
        foxWebLog.setMessage("Start NER ...");
        Set<Entity> entities = nerTools.getEntities(parameter.get(FoxCfg.parameter_input));

        // remove duplicate annotations
        // TODO: why they here?
        Map<String, Entity> wordEntityMap = new HashMap<>();
        for (Entity entity : entities) {
            if (wordEntityMap.get(entity.getText()) == null) {
                wordEntityMap.put(entity.getText(), entity);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("We have a duplicate annotation and removing those: "
                            + entity.getText() + " "
                            + entity.getType() + " "
                            + wordEntityMap.get(entity.getText()).getType());
                }
                wordEntityMap.remove(entity.getText());
            }
        }
        // remove
        entities.retainAll(wordEntityMap.values());
        foxWebLog.setMessage("NER done.");
        return entities;
    }

    protected Set<Relation> doRE() {
        // info
        {
            String info = "Start RE ...";
            LOG.info(info);
            foxWebLog.setMessage(info);
        }
        final CountDownLatch latch = new CountDownLatch(1);
        IRE reTool = reTools.getRETool();
        reTool.setCountDownLatch(latch);
        reTool.setInput(parameter.get(FoxCfg.parameter_input));

        Fiber fiber = new ThreadFiber();
        fiber.start();
        fiber.execute(reTool);

        int min = Integer.parseInt(FoxCfg.get(FoxNERTools.NERLIFETIME_KEY));
        try {
            latch.await(min, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        // shutdown threads
        fiber.dispose();

        // get results
        Set<Relation> relations = null;
        if (latch.getCount() == 0) {
            relations = reTool.getResults();

        } else {
            // info
            String info = "Timeout after " + min + " min.";
            LOG.error(info);
            foxWebLog.setMessage(info);
        }

        // info
        {
            String info = "RE done.";
            LOG.info(info);
            foxWebLog.setMessage(info);
        }
        return relations == null ? new HashSet<Relation>() : relations;

    }

    protected Set<Entity> doNERLight() {
        LOG.info("Starting light NER version ...");
        foxWebLog.setMessage("Starting light NER version ...");
        Set<Entity> entities = null;
        setLightVersionNER();
        foxWebLog.setMessage("NER tool is: " + nerLight.getToolName());

        final CountDownLatch latch = new CountDownLatch(1);

        nerLight.setCountDownLatch(latch);
        nerLight.setInput(tokenManager.getInput());
        // nerLight.setTokenManager(tokenManager);

        Fiber fiber = new ThreadFiber();
        fiber.start();
        fiber.execute(nerLight);

        int min = Integer.parseInt(FoxCfg.get(FoxNERTools.NERLIFETIME_KEY));
        try {
            latch.await(min, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Timeout after " + min + " min.");
            LOG.error("\n", e);
            LOG.error("input:\n" + parameter.get(FoxCfg.parameter_input));
        }

        // shutdown threads
        fiber.dispose();
        // get results
        if (latch.getCount() == 0) {
            entities = new HashSet<Entity>(nerLight.getResults());

        } else {
            if (LOG.isDebugEnabled())
                LOG.debug("timeout after " + min + "min.");

            // TODO: handle timeout
        }

        foxWebLog.setMessage("Light version done.");

        tokenManager.repairEntities(entities);

        /* make an index for each entity */

        // make index map
        Map<Integer, Entity> indexMap = new HashMap<>();
        /*  
         for (Entity entity : entities)
              for (Integer i : FoxTextUtil.getIndices(entity.getText(), tokenManager.getTokenInput()))
                  indexMap.put(i, entity);
        */
        entities.forEach(
                entity -> FoxTextUtil.getIndices(entity.getText(), tokenManager.getTokenInput())
                        .forEach(
                                index -> indexMap.put(index, entity))
                );

        // sort
        List<Integer> sortedIndices = new ArrayList<>(indexMap.keySet());
        Collections.sort(sortedIndices);

        // loop index in sorted order
        int offset = -1;
        for (Integer i : sortedIndices) {
            Entity e = indexMap.get(i);
            if (offset < i) {
                offset = i + e.getText().length();
                e.addIndicies(i);
            }
        }

        // remove entity without an index
        Set<Entity> cleanEntity = new HashSet<>();
        for (Entity e : entities) {
            if (e.getIndices() != null && e.getIndices().size() > 0) {
                cleanEntity.add(e);
            }
        }
        entities = cleanEntity;
        nerLight = null;
        return entities;
    }

    protected void setURIs(Set<Entity> entities) {
        if (entities != null && !entities.isEmpty()) {
            foxWebLog.setMessage("Start looking up uri ...");
            final CountDownLatch latch = new CountDownLatch(1);

            uriLookup.setCountDownLatch(latch);
            uriLookup.setInput(entities, parameter.get(FoxCfg.parameter_input));

            Fiber fiber = new ThreadFiber();
            fiber.start();
            fiber.execute(uriLookup);

            // use another time for the uri lookup?
            int min = Integer.parseInt(FoxCfg.get(FoxNERTools.NERLIFETIME_KEY));
            try {
                latch.await(min, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                LOG.error("Timeout after " + min + "min.");
                LOG.error("\n", e);
            }

            // shutdown threads
            fiber.dispose();
            // get results
            if (latch.getCount() == 0) {
                entities = new HashSet<Entity>(uriLookup.getResults());
            } else {

                String s = "Timeout after " + min + " min (" + uriLookup.getClass().getName() + ").";
                if (LOG.isDebugEnabled())
                    LOG.debug(s);
                foxWebLog.setMessage(s);

                // use dev lookup after timeout
                new NullLookup().setUris(entities, parameter.get(FoxCfg.parameter_input));
            }

            // for (Entity e : entities)
            // e.uri = uriLookup.getUri(e.getText(), e.getType());
            foxWebLog.setMessage("Start looking up uri done.");
        }
    }

    protected void setOutput(Set<Entity> entities, Set<Relation> relations) {
        if (entities == null) {
            LOG.warn("Entities are empty.");
            return;
        }

        String input = parameter.get(FoxCfg.parameter_input);

        // switch output
        final boolean useNIF = Boolean.parseBoolean(parameter.get(FoxCfg.parameter_nif));

        String out = parameter.get(FoxCfg.parameter_output);
        foxWebLog.setMessage("Preparing output format ...");

        foxJena.clearGraph();
        foxJena.setAnnotations(entities);

        if (relations != null) {
            foxJena.setRelations(relations);
        }

        response = foxJena.print(out, useNIF, input);
        foxWebLog.setMessage("Preparing output format done.");

        if (parameter.get("returnHtml") != null && parameter.get("returnHtml").toLowerCase().endsWith("true")) {

            Map<Integer, Entity> indexEntityMap = new HashMap<>();
            for (Entity entity : entities) {
                for (Integer startIndex : entity.getIndices()) {
                    // TODO : check contains
                    indexEntityMap.put(startIndex, entity);
                }
            }

            Set<Integer> startIndices = new TreeSet<>(indexEntityMap.keySet());

            String html = "";

            int last = 0;
            for (Integer index : startIndices) {
                Entity entity = indexEntityMap.get(index);
                if (entity.uri != null && !entity.uri.trim().isEmpty()) {
                    html += input.substring(last, index);
                    html += "<a class=\"" + entity.getType().toLowerCase() + "\" href=\"" + entity.uri + "\"  target=\"_blank\"  title=\"" + entity.getType().toLowerCase() + "\" >" + entity.getText() + "</a>";
                    last = index + entity.getText().length();
                } else {
                    LOG.error("Entity has no URI: " + entity.getText());
                }
            }

            html += input.substring(last);
            parameter.put(FoxCfg.parameter_input, html);

            if (LOG.isTraceEnabled())
                infotrace(entities);
        }

        foxWebLog.setMessage("Found " + entities.size() + " entities.");
        foxWebLog.setMessage("Found " + relations.size() + " relations.");
    }

    /**
     * 
     */
    @Override
    public void run() {

        foxWebLog = new FoxWebLog();
        foxWebLog.setMessage("Running Fox...");

        if (parameter == null) {
            LOG.error("Parameter not set.");
        } else {
            String input = parameter.get(FoxCfg.parameter_input);
            String task = parameter.get(FoxCfg.parameter_task);
            String light = parameter.get(FoxCfg.parameter_foxlight);

            Set<Entity> entities = null;
            Set<Relation> relations = null;

            if (input == null || task == null) {
                LOG.error("Input or task parameter not set.");
            } else {
                tokenManager = new TokenManager(input);
                // clean input
                input = tokenManager.getInput();
                parameter.put(FoxCfg.parameter_input, input);

                // light version
                if (light != null && !light.equals("OFF")) {
                    // switch task
                    switch (task.toLowerCase()) {

                    case "ke":
                        // TODO: ke fox
                        LOG.info("Starting light KE version ...");
                        break;

                    case "ner":
                        entities = doNERLight();
                        break;
                    }

                } else {
                    // switch task
                    switch (task.toLowerCase()) {
                    case "ke":
                        // TODO: ke fox
                        LOG.info("starting ke ...");
                        break;

                    case "ner":
                        entities = doNER();
                        break;

                    case "re":
                        entities = doNER();
                        relations = doRE();
                        break;
                    }
                }
            }

            setURIs(entities);
            setOutput(entities, relations);
        }

        // done
        foxWebLog.setMessage("Running Fox done.");
        if (countDownLatch != null)
            countDownLatch.countDown();
    }

    /**
     * Prints debug infos about entities for each tool and final entities in
     * fox.
     * 
     * @param entities
     *            final entities
     */
    private void infotrace(Set<Entity> entities) {
        if (LOG.isTraceEnabled()) {

            LOG.trace("entities:");
            for (String toolname : nerTools.getToolResult().keySet()) {
                if (nerTools.getToolResult().get(toolname) == null)
                    return;

                LOG.trace(toolname + ": " + nerTools.getToolResult().get(toolname).size());
                for (Entity e : nerTools.getToolResult().get(toolname))
                    LOG.trace(e);
            }

            LOG.trace("fox" + ": " + entities.size());
            for (Entity e : entities)
                LOG.trace(e);
        }
    }

    @Override
    public void setCountDownLatch(CountDownLatch cdl) {
        this.countDownLatch = cdl;
    }

    @Override
    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;

        String paraUriLookup = parameter.get(FoxCfg.parameter_disamb);
        if (paraUriLookup != null) {
            if (paraUriLookup.equalsIgnoreCase("off"))
                paraUriLookup = "org.aksw.fox.uri.NullLookup";

            try {
                uriLookup = (ILookup) FoxCfg.getClass(paraUriLookup.trim());
            } catch (Exception e) {
                LOG.error("InterfaceURI not found. Check parameter: " + FoxCfg.parameter_disamb);
            }
        } else {
            // TODO: clean code
            uriLookup = new AGDISTISLookup();
        }
    }

    @Override
    public String getResults() {
        return response;
    }

    @Override
    public Map<String, String> getDefaultParameter() {
        Map<String, String> map = new HashMap<>();
        map.put(FoxCfg.parameter_input, FoxConst.NER_EN_EXAMPLE_1);
        map.put(FoxCfg.parameter_task, "NER");
        map.put(FoxCfg.parameter_output, Lang.RDFXML.getName());
        map.put(FoxCfg.parameter_nif, "false");
        map.put(FoxCfg.parameter_foxlight, "OFF");
        map.put(FoxCfg.parameter_disamb, "org.aksw.fox.uri.AGDISTISLookup");
        return map;
    }

    @Override
    public String getLog() {
        return foxWebLog.getConsoleOutput();
    }
}
