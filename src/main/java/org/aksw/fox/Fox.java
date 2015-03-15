package org.aksw.fox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.tools.ner.FoxNERTools;
import org.aksw.fox.tools.ner.INER;
import org.aksw.fox.tools.ner.en.NERStanford;
import org.aksw.fox.uri.AGDISTISLookup;
import org.aksw.fox.uri.ILookup;
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
    }

    /**
     *
     */
    public Fox(ILookup uriLookup, INER nerLight) {
        this.uriLookup = uriLookup;
        this.nerLight = nerLight;
        this.nerTools = new FoxNERTools();
    }

    /**
     * 
     */
    @Override
    public void run() {

        foxWebLog = new FoxWebLog(UUID.randomUUID().toString());
        foxWebLog.setMessage("Running Fox...");

        if (parameter == null) {
            LOG.error("Parameter not set.");
        } else {
            final String input;
            Set<Entity> entities = null;

            if (parameter.get(FoxCfg.parameter_input) == null || parameter.get(FoxCfg.parameter_task) == null) {
                LOG.error("Input or task parameter not set.");
                input = null;
            } else {
                String task = parameter.get(FoxCfg.parameter_task);
                tokenManager = new TokenManager(parameter.get(FoxCfg.parameter_input));
                // clean input
                input = tokenManager.getInput();
                parameter.put(FoxCfg.parameter_input, input);

                if (parameter.get(FoxCfg.parameter_foxlight) != null && !parameter.get(FoxCfg.parameter_foxlight).equals("OFF")) {
                    // switch task
                    switch (task.toLowerCase()) {

                    case "ke":
                        LOG.info("starting foxlight ke ...");
                        // TODO:
                        break;

                    case "ner":
                        LOG.info("starting foxlight ner ...");
                        foxWebLog.setMessage("Start light version ...");

                        // set ner light tool
                        if (nerLight == null)
                            for (INER tool : nerTools.getNerTools())
                                if (parameter.get(FoxCfg.parameter_foxlight).equals(tool.getClass().getName())) {
                                    nerLight = tool;
                                    break;
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
                            LOG.error("Timeout after " + min + "min.");
                            LOG.error("\n", e);
                            LOG.error("input:\n" + input);
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
                        for (Entity entity : entities) {
                            for (Integer i : FoxTextUtil.getIndices(entity.getText(), tokenManager.getTokenInput())) {
                                indexMap.put(i, entity);
                            }
                        }

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
                    }

                } else {

                    // switch task
                    switch (task.toLowerCase()) {

                    case "ke":
                        LOG.info("starting ke ...");
                        // TODO: ke fox
                        break;

                    case "ner":
                        LOG.info("starting ner ...");
                        foxWebLog.setMessage("Start NER ...");
                        entities = nerTools.getEntities(input);
                        foxWebLog.setMessage("NER done.");

                        // remove duplicate annotations
                        // TODO: why they here?
                        Map<String, Entity> wordEntityMap = new HashMap<>();
                        for (Entity entity : entities) {
                            if (wordEntityMap.get(entity.getText()) == null) {
                                wordEntityMap.put(entity.getText(), entity);
                            } else {
                                LOG.debug("We have a duplicate annotation: " + entity.getText() + " " + entity.getType() + " " + wordEntityMap.get(entity.getText()).getType());
                                LOG.debug("We remove it ...");
                                wordEntityMap.remove(entity.getText());
                            }
                        }
                        // remove them
                        entities.retainAll(wordEntityMap.values());
                    }
                }
            }

            if (entities != null) {
                // TODO: use interface for all tools
                // 4. set URIs
                foxWebLog.setMessage("Start looking up uri ...");
                uriLookup.setUris(entities, input);
                foxWebLog.setMessage("Start looking up uri done.");
                // for (Entity e : entities)
                // e.uri = uriLookup.getUri(e.getText(), e.getType());
                // switch output
                final boolean useNIF = Boolean.parseBoolean(parameter.get(FoxCfg.parameter_nif));

                String out = parameter.get(FoxCfg.parameter_output);
                foxWebLog.setMessage("Preparing output format ...");

                foxJena.clearGraph();
                foxJena.setAnnotations(entities);
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

                    // INFO TRACE
                    if (LOG.isTraceEnabled())
                        infotrace(entities);
                    // INFO TRACE
                }
            }
        }

        // done
        foxWebLog.setMessage("Running Fox done.");
        if (countDownLatch != null)
            countDownLatch.countDown();
    }

    // debug infos
    private void infotrace(Set<Entity> entities) {
        // INFO TRACE
        LOG.info("Entities:");
        for (String toolname : this.nerTools.getToolResult().keySet()) {
            if (this.nerTools.getToolResult().get(toolname) == null)
                return;
            LOG.info(toolname + ": " + this.nerTools.getToolResult().get(toolname).size());
            if (LOG.isTraceEnabled())
                for (Entity e : this.nerTools.getToolResult().get(toolname))
                    LOG.trace(e);
        }
        LOG.info("fox" + ": " + entities.size());
        if (LOG.isTraceEnabled())
            for (Entity e : entities)
                LOG.trace(e);
        // INFO TRACE
    }

    @Override
    public void setCountDownLatch(CountDownLatch cdl) {
        this.countDownLatch = cdl;
    }

    @Override
    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
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
        return map;
    }

    @Override
    public String getLog() {
        return foxWebLog.getConsoleOutput();
    }
}
