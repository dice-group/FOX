package org.aksw.fox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nertools.FoxNERTools;
import org.aksw.fox.uri.AGDISTISLookup;
import org.aksw.fox.uri.InterfaceURI;
import org.aksw.fox.utils.FoxJena;
import org.apache.log4j.Logger;

/**
 * An implementation of FoxInterface and Runnable.
 * 
 * @author rspeck
 * 
 */
public class Fox implements InterfaceRunnableFox {

    public static Logger logger = Logger.getLogger(Fox.class);
    /**
     * 
     */
    protected InterfaceURI uriLookup = new AGDISTISLookup();
    protected FoxNERTools nerTools = null;
    protected TokenManager tokenManager = null;
    protected FoxJena foxJena = new FoxJena();

    private CountDownLatch countDownLatch = null;
    private Map<String, String> parameter = null;
    private String response = null;

    public Fox() {

        nerTools = new FoxNERTools();
    }

    /**
     * 
     */
    @Override
    public void run() {

        if (parameter == null) {

            logger.error("Parameter not set.");

        } else {

            Set<Entity> entities = null;

            // plain text
            String input = parameter.get("input");
            String task = parameter.get("task");

            if (input == null || task == null) {

                logger.error("Input or task parameter not set.");

            } else {
                tokenManager = new TokenManager(input);
                input = null;
                parameter.put("input", tokenManager.getInput());

                // switch task
                switch (task.toLowerCase()) {

                case "ke":
                    logger.info("starting ke ...");
                    // TODO

                    break;

                case "ner":
                    logger.info("starting ner ...");

                    entities = nerTools.getNER(tokenManager.getInput());

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

            // TODO
            if (entities != null) {

                // TODO move loop to uri tool i.e. interface for list
                // 4. set URIs
                uriLookup.setUris(entities, tokenManager.getInput());
                // for (Entity e : entities)
                // e.uri = uriLookup.getUri(e.getText(), e.getType());
                // switch output
                final boolean useNIF = Boolean.parseBoolean(parameter.get("nif"));

                String out = parameter.get("output");
                if (useNIF) {
                    // TODO
                } else {

                    foxJena.clearGraph();
                    foxJena.setAnnotations(entities);
                    response = foxJena.print(out, false, tokenManager.getInput());
                }

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
                    input = tokenManager.getInput();
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
                }
            }
        }

        // done
        if (countDownLatch != null)
            countDownLatch.countDown();

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
        map.put("input",
                "Leipzig was first documented in 1015 in the chronicles of Bishop Thietmar of Merseburg and endowed with city and market privileges in 1165 by Otto the Rich. Leipzig has fundamentally shaped the history of Saxony and of Germany and has always been known as a place of commerce. The Leipzig Trade Fair, started in the Middle Ages, became an event of international importance and is the oldest remaining trade fair in the world.");
        map.put("task", "ner");
        map.put("output", "rdf");
        map.put("nif", "false");
        return map;
    }
}
