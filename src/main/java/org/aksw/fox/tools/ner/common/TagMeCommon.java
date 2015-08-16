package org.aksw.fox.tools.ner.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.http.client.fluent.Form;
import org.json.JSONArray;
import org.json.JSONObject;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class TagMeCommon extends AbstractNER {

    public static final XMLConfiguration    CFG                     = CfgManager.getCfg(TagMeCommon.class);
    public static final Charset             UTF_8                   = Charset.forName("UTF-8");

    public static final String              CFG_KEY_TAGME_KEY       = "tagMe.key";
    public static final String              CFG_KEY_SPARQL_ENDPOINT = "tagMe.sparqlEndpoint";
    public static final String              CFG_KEY_RDFS_PREFIX     = "tagMe.rdfsPrefix";
    public static final String              CFG_KEY_RDF_PREFIX      = "tagMe.rdfPrefix";
    public static final String              CFG_KEY_MIN_WEIGHT      = "tagMe.minWeight";
    public static final String              CFG_KEY_ENDPOINT        = "tagMe.url";
    public static final String              CFG_KEY_LANG            = "tagMe.lang";

    public static final String              TAGME_KEY               = CFG.getString(CFG_KEY_TAGME_KEY);
    public static final String              SPARQL_ENDPOINT         = CFG.getString(CFG_KEY_SPARQL_ENDPOINT);
    public static final String              RDFS_PREFIX             = CFG.getString(CFG_KEY_RDFS_PREFIX);
    public static final String              ENDPOINT                = CFG.getString(CFG_KEY_ENDPOINT);
    public static final String              RDF_PREFIX              = CFG.getString(CFG_KEY_RDF_PREFIX);
    public static final double              MIN_WEIGHT              = CFG.getDouble(CFG_KEY_MIN_WEIGHT);

    public static final String              include_categories      = "true";
    // ? other results for FoxConst.NER_EN_EXAMPLE_1
    public static final String              epsilon                 = "0.1";
    public static final String              min_comm                = "0.1";
    public static final String              min_link                = "0.1";

    protected final String                  LANG;

    // public static final int paging = 100;
    public static final int                 delay                   = 1000;

    // dbpedia_categories to type
    static int                              cacheSize               = 100000;
    protected ConcurrentMap<String, String> categoriesToTypeLRU     = new ConcurrentLinkedHashMap.
                                                                            Builder<String, String>()
                                                                                    .maximumWeightedCapacity(cacheSize)
                                                                                    .build();

    String                                  dbpediaURL;
    String                                  dbpediaGraph;

    /**
     * 
     * @param lang
     * @param dbpediaURL
     * @param dbpediaGraph
     */
    public TagMeCommon(String lang, String dbpediaURL, String dbpediaGraph) {
        LOG.info("TagMeCommon ... ");
        LANG = lang;

        entityList = new ArrayList<>();
        this.dbpediaGraph = dbpediaGraph;
        this.dbpediaURL = dbpediaURL;
    }

    @Override
    public List<Entity> retrieve(String input) {
        return retrieveSentences(getSentences(LANG, input));
    }

    protected List<Entity> retrieveSentences(List<String> sentences) {

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CompletionService<List<Entity>> completionService = new ExecutorCompletionService<>(executorService);

        int n = 0;
        for (int i = 0; i < sentences.size(); i++) {
            completionService.submit(new TagMeCall(sentences.get(i), LANG));
            ++n;
        }
        executorService.shutdown();
        Set<Entity> set = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            try {
                Future<List<Entity>> future = completionService.take();
                List<Entity> result = future.get();

                if (result != null && !result.isEmpty()) {
                    set.addAll(result);
                } else {
                    LOG.warn("No entities found.");
                }

            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        entityList.addAll(set);
        return entityList;
    }

    class TagMeCall implements Callable<List<Entity>> {

        String                sentence;
        String                lang;

        QueryExecutionFactory qef;

        public TagMeCall(String sentence, String lang) {
            this.sentence = sentence;
            this.lang = lang;
        }

        @Override
        public List<Entity> call() throws Exception {
            LOG.info("CALL...");
            LOG.info("send request to TagMe service");
            String response = send();

            LOG.info("get response annotations");
            JSONArray annos = handleResponse(response);

            QueryExecutionFactory qef = new QueryExecutionFactoryHttp(dbpediaURL, dbpediaGraph);
            try {
                // qef = new QueryExecutionFactoryPaginated(qef, paging);
                qef = new QueryExecutionFactoryDelay(qef, delay);

            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            LOG.info("get entities");
            List<Entity> e = getEntities(annos, qef);
            LOG.info("DONE.");
            qef.close();
            return e;
        }

        /**
         * Gets 'annotations' JSONArray
         */
        public JSONArray handleResponse(String response) {
            LOG.debug(new JSONObject(response).toString(2));
            // parse response
            JSONObject jo;
            if (response != null) {
                jo = new JSONObject(response);
                if (jo.has("annotations")) {
                    return jo.getJSONArray("annotations");
                } else
                    LOG.warn("No annotations found.");
            }
            return new JSONArray();
        }

        /**
         * TagMe request.
         * 
         * @return
         */
        public String send() {
            String response = "";
            try {
                response = postToJSON(ENDPOINT, Form.form()
                        .add("key", TAGME_KEY)
                        .add("text", sentence)
                        .add("lang", LANG)
                        .add("epsilon", epsilon)
                        .add("min_comm", min_comm)
                        .add("min_link", min_link)
                        .add("include_categories", include_categories));
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return response;
        }

    } // end class TagMeCall

    protected List<Entity> getEntities(JSONArray annos, QueryExecutionFactory qef) {
        Set<Entity> entities = new HashSet<>();
        LinkedHashSet<String> titles = new LinkedHashSet<>();
        List<JSONObject> annosList = new ArrayList<>();

        for (int i = 0; i < annos.length(); i++) {

            if (Double.valueOf(annos.getJSONObject(i).getString("rho")) < MIN_WEIGHT) {
                LOG.debug("roh too small");
            } else {

                if (annos.getJSONObject(i).has("title")) {
                    String title = annos.getJSONObject(i).getString("title");
                    // cache
                    LOG.debug("categoriesToTypeLRU: " + categoriesToTypeLRU);
                    String type = categoriesToTypeLRU.get(title);
                    if (type != null) {
                        LOG.info("found in cache");
                    } else {
                        titles.add(title);
                        annosList.add(annos.getJSONObject(i));
                    }
                }
            }
        }
        // send titles to dbpedia
        List<String> types = new ArrayList<>();
        for (String t : titles) {
            types.add(sendSparql(t, qef));
        }

        List<String> titlesList = new ArrayList<>(titles);

        for (int i = 0; i < annosList.size(); i++) {
            String spot = annosList.get(i).getString("spot");
            String rho = annosList.get(i).getString("rho");
            LinkedHashSet<String> dbpedia_categories = getSet(annosList.get(i).getJSONArray("dbpedia_categories"));

            String title = annosList.get(i).getString("title");
            int index = titlesList.indexOf(title);
            String type = types.get(index);
            if (!type.equals(EntityClassMap.getNullCategory())) {
                categoriesToTypeLRU.put(title, type);
                entities.add(new Entity(spot, type, Double.valueOf(rho).floatValue(), getToolName()));
            } else {
                // TODO: find type by given category? Seems not to be worth
                LOG.info("not found title: " + title);
                /*
                if (type.equals(EntityClassMap.getNullCategory()))
                    type = sendSparql(dbpedia_categories, qef);
                if (!type.equals(EntityClassMap.getNullCategory())) {
                    categoriesToTypeLRU.put(title, type);
                    entities.add(new Entity(spot, type, Double.valueOf(rho).floatValue(), getToolName()));
                }
                */
            }
        }
        return new ArrayList<>(entities);
    }

    // get to each title a type
    public List<String> sendSparqlMulti(LinkedHashSet<String> titles, QueryExecutionFactory qef) {
        if (titles == null)
            throw new NullPointerException("titles parameter is null");

        if (titles.isEmpty())
            return new ArrayList<>();

        List<String> labels = new ArrayList<>(titles);
        List<String> types = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX  rdfs: <").append(RDFS_PREFIX).append("> ");
        sb.append("PREFIX  rdf: <").append(RDF_PREFIX).append("> ");
        sb.append("SELECT");
        for (int i = 0; i < labels.size(); i++)
            sb.append(" ?").append("y").append(i);

        sb.append(" WHERE {");
        for (int i = 0; i < labels.size(); i++) {
            sb.append(" ?").append("x").append(i).append(" rdfs:label \"").append(labels.get(i)).append("\"@").append(LANG).append(".");
            sb.append(" ?").append("x").append(i).append(" rdf:type ?y").append(i).append(".");
            types.add(EntityClassMap.getNullCategory());
        }
        sb.append(" }");

        String q = sb.toString();

        String current_results = null;
        try {

            QueryExecution qe = qef.createQueryExecution(q);
            qe.setTimeout(2 * 60 * 1000);
            ResultSet rs = qe.execSelect();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(baos, rs);
            current_results = baos.toString();
        } catch (Exception e) {
            LOG.error("query: \n" + q);
            LOG.error(e.getLocalizedMessage(), e);
        }

        if (current_results != null) {

            JSONObject jo = new JSONObject(current_results);
            List<String> vars = getVars(jo);
            JSONArray bindings = getBindings(jo);

            if (bindings.length() > 0 && vars.size() > 0) {
                for (int ii = 0; ii < labels.size(); ii++) {
                    String y = "y".concat(String.valueOf(ii));
                    if (vars.contains(y)) {
                        // all bindings
                        for (int i = 0; i < bindings.length(); i++) {
                            JSONObject o = bindings.getJSONObject(i);
                            if (o.has(y)) {
                                String type = findType(o.getJSONObject(y).getString("value"));
                                if (!type.equals(EntityClassMap.getNullCategory())) {
                                    types.remove(ii);
                                    types.add(ii, type);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return types;
    }

    // get the type for all labels
    protected String sendSparql(Set<String> labelsset, QueryExecutionFactory qef) {

        String type = EntityClassMap.getNullCategory();

        if (labelsset.isEmpty())
            return type;

        List<String> labels = new ArrayList<>(labelsset);

        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX  rdfs: <").append(RDFS_PREFIX).append("> ");
        sb.append("PREFIX  rdf: <").append(RDF_PREFIX).append("> ");
        sb.append("SELECT");
        for (int i = 0; i < labels.size(); i++)
            sb.append(" ?").append("y").append(i);

        sb.append(" WHERE {");
        for (int i = 0; i < labels.size(); i++) {
            sb.append(" ?").append("x").append(i).append(" rdfs:label \"").append(labels.get(i)).append("\"@").append(LANG).append(".");
            sb.append(" ?").append("x").append(i).append(" rdf:type ?y").append(i).append(".");
        }
        sb.append(" }");

        String q = sb.toString();

        String current_results = null;
        try {
            ResultSet rs = qef.createQueryExecution(q).execSelect();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(baos, rs);
            current_results = baos.toString();
        } catch (Exception e) {
            LOG.error(q);
            LOG.error(e.getLocalizedMessage(), e);
        }

        if (current_results != null) {

            JSONObject jo = new JSONObject(current_results);
            List<String> vars = getVars(jo);
            JSONArray bindings = getBindings(jo);

            if (bindings.length() > 0 && vars.size() > 0) {
                for (int ii = 0; ii < labels.size(); ii++) {
                    String y = "y".concat(String.valueOf(ii));
                    if (vars.contains(y)) {
                        // all bindings
                        for (int i = 0; i < bindings.length(); i++) {
                            JSONObject o = bindings.getJSONObject(i);
                            if (o.has(y)) {
                                type = findType(o.getJSONObject(y).getString("value"));
                                if (!type.equals(EntityClassMap.getNullCategory()))
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return type;
    }

    // get the type for all label
    protected String sendSparql(String label, QueryExecutionFactory qef) {
        Set<String> set = new HashSet<String>();
        set.add(label);
        return sendSparql(set, qef);
    }

    /**
     * 
     * @param jo
     * @return
     */
    protected JSONArray getBindings(JSONObject jo) {
        JSONArray bindings = new JSONArray();
        if (jo.has("results")) {
            JSONObject results = jo.getJSONObject("results");
            if (results.has("bindings"))
                bindings = results.getJSONArray("bindings");
        }
        return bindings;
    }

    /**
     * 
     * @param jo
     * @return
     */
    protected List<String> getVars(JSONObject jo) {
        List<String> vars = new ArrayList<>();
        if (jo.has("head")) {
            JSONObject head = jo.getJSONObject("head");
            if (head.has("vars")) {
                JSONArray varsja = head.getJSONArray("vars");
                for (int i = 0; i < varsja.length(); i++)
                    vars.add(varsja.getString(i));
            }
        }
        return vars;
    }

    /**
     * JSONArray to LinkedHashSet<String>
     * 
     * @param ja
     * @return
     */
    protected LinkedHashSet<String> getSet(JSONArray ja) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (int ii = 0; ii < ja.length(); ii++)
            set.add(ja.getString(ii));
        return set;
    }

    /**
     * Gets the entity type.
     */
    protected String findType(String text) {
        String t = EntityClassMap.getNullCategory();
        if (text == null || text.trim().isEmpty())
            return t;

        if (text.toLowerCase().contains("person")) {
            t = EntityClassMap.P;
        }

        if (text.toLowerCase().contains("organisation") || text.toLowerCase().contains("organization")) {
            if (!t.equals(EntityClassMap.getNullCategory())) {
                LOG.info("disamb: " + t + " " + EntityClassMap.O);
            }
            t = EntityClassMap.O;
        }

        if (text.toLowerCase().contains("place") || text.toLowerCase().contains("location")) {
            if (!t.equals(EntityClassMap.getNullCategory())) {
                LOG.info("disamb: " + t + " " + EntityClassMap.L);
            }
            t = EntityClassMap.L;
        }

        return t;
    }
}
