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

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nertools.FoxNERTools;
import org.aksw.fox.nertools.InterfaceRunnableNER;
import org.aksw.fox.nertools.NERStanford;
import org.aksw.fox.uri.AGDISTISLookup;
import org.aksw.fox.uri.InterfaceURI;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxJena;
import org.aksw.fox.utils.FoxTextUtil;
import org.aksw.fox.utils.FoxWebLog;
import org.apache.log4j.Logger;

/**
 * An implementation of {@link org.aksw.fox.InterfaceRunnableFox}.
 * 
 * @author rspeck
 * 
 */
public class Fox implements InterfaceRunnableFox {

    public static Logger logger = Logger.getLogger(Fox.class);

    /**
     * 
     */
    protected InterfaceURI uriLookup = null;

    /**
     * 
     */
    protected FoxNERTools nerTools = null;

    /**
     * 
     */
    protected TokenManager tokenManager = null;

    /**
     * 
     */
    protected FoxJena foxJena = new FoxJena();

    /**
     * Holds a tool for fox's light version.
     */
    protected InterfaceRunnableNER nerLight = null;

    /**
     * 
     */
    protected FoxWebLog foxWebLog = null;

    private CountDownLatch countDownLatch = null;
    private Map<String, String> parameter = null;
    private String response = null;

    /**
     * 
     */
    public Fox() {

        // load class in fox.properties file
        if (FoxCfg.get("urilookup") != null) {
            try {
                uriLookup = (InterfaceURI) FoxCfg.getClass(FoxCfg.get("urilookup").trim());
            } catch (Exception e) {
                logger.error("InterfaceURI not found. Check your fox.properties file.");
            }
        }
        if (uriLookup == null)
            uriLookup = new AGDISTISLookup();

        // load class in fox.properties file
        if (FoxCfg.get("nerLight") != null) {
            try {
                nerLight = (InterfaceRunnableNER) FoxCfg.getClass(FoxCfg.get("nerLight").trim());
            } catch (Exception e) {
                logger.error("InterfaceRunnableNER not found. Check your fox.properties file.");
            }
        }
        if (nerLight == null)
            nerLight = new NERStanford();

        //
        this.nerTools = new FoxNERTools();
    }

    /**
     *
     */
    public Fox(InterfaceURI uriLookup, InterfaceRunnableNER nerLight) {
        this.uriLookup = uriLookup;
        this.nerLight = nerLight;
        this.nerTools = new FoxNERTools();
    }

    /**
     * 
     */
    @Override
    public void run() {

        foxWebLog = new FoxWebLog();
        foxWebLog.setMessage("Running Fox...");

        if (parameter == null) {
            logger.error("Parameter not set.");

        } else {
            final String input;
            Set<Entity> entities = null;

            if (parameter.get("input") == null || parameter.get("task") == null) {
                logger.error("Input or task parameter not set.");
                input = null;

            } else {
                String task = parameter.get("task");
                tokenManager = new TokenManager(parameter.get("input"));
                // clean input
                input = tokenManager.getInput();
                parameter.put("input", input);

                if (Boolean.valueOf(parameter.get("foxlight")) == true) {
                    // switch task
                    switch (task.toLowerCase()) {

                    case "ke":
                        logger.info("starting foxlight ke ...");
                        // TODO
                        break;

                    case "ner":
                        logger.info("starting foxlight ner ...");

                        foxWebLog.setMessage("Fox-Light start retrieving ner ...");
                        // TODO
                        entities = new HashSet<Entity>(nerLight.retrieve(input));
                        foxWebLog.setMessage("Fox-Light start retrieving ner done.");

                        tokenManager.repairEntities(entities);

                        /* make an index for each entity */

                        // make index map
                        Map<Integer, Entity> indexMap = new HashMap<>();
                        for (Entity entity : entities) {
                            for (Integer i : FoxTextUtil.getIndex(entity.getText(), input)) {
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
                    }

                } else {

                    // switch task
                    switch (task.toLowerCase()) {

                    case "ke":
                        logger.info("starting ke ...");
                        // TODO
                        break;

                    case "ner":
                        logger.info("starting ner ...");
                        foxWebLog.setMessage("Start retrieving ner ...");
                        entities = nerTools.getEntities(input);
                        foxWebLog.setMessage("Start retrieving ner done.");

                        // remove duplicate annotations
                        // TODO: why they here?
                        Map<String, Entity> wordEntityMap = new HashMap<>();
                        for (Entity entity : entities) {
                            if (wordEntityMap.get(entity.getText()) == null) {
                                wordEntityMap.put(entity.getText(), entity);
                            } else {
                                logger.debug("We have a duplicate annotation: " + entity.getText() + " " + entity.getType() + " " + wordEntityMap.get(entity.getText()).getType());
                                logger.debug("We remove it ...");
                                wordEntityMap.remove(entity.getText());
                            }
                        }
                        // remove them
                        entities.retainAll(wordEntityMap.values());
                    }
                }
            }

            if (entities != null) {
                // TODO
                // 4. set URIs
                foxWebLog.setMessage("Start looking up uri ...");
                uriLookup.setUris(entities, input);
                foxWebLog.setMessage("Start looking up uri done.");
                // for (Entity e : entities)
                // e.uri = uriLookup.getUri(e.getText(), e.getType());
                // switch output
                final boolean useNIF = Boolean.parseBoolean(parameter.get("nif"));

                String out = parameter.get("output");
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
                            logger.error("Entity has no URI: " + entity.getText());
                        }
                    }

                    html += input.substring(last);
                    parameter.put("input", html);

                    // INFO TRACE
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
        logger.info("Entities:");
        for (String toolname : this.nerTools.getToolResult().keySet()) {
            logger.info(toolname + ": " + this.nerTools.getToolResult().get(toolname).size());
            if (logger.isTraceEnabled())
                for (Entity e : this.nerTools.getToolResult().get(toolname))
                    logger.trace(e);
        }
        logger.info("fox" + ": " + entities.size());
        if (logger.isTraceEnabled())
            for (Entity e : entities)
                logger.trace(e);
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
        map.put(
                "input",
                    "Leipzig was first documented in 1015 in the chronicles of Bishop Thietmar of Merseburg and endowed with city and market privileges in 1165 by Otto the Rich. Leipzig has fundamentally shaped the history of Saxony and of Germany and has always been known as a place of commerce. The Leipzig Trade Fair, started in the Middle Ages, became an event of international importance and is the oldest remaining trade fair in the world.");
        map.put("task", "ner");
        map.put("output", "rdf");
        map.put("nif", "false");
        return map;
    }

    @Override
    public String getLog() {
        return foxWebLog.getConsoleOutput();
    }
}
