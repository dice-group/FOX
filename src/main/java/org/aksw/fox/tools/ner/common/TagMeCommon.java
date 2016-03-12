package org.aksw.fox.tools.ner.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import de.renespeck.swissknife.http.Requests;

public abstract class TagMeCommon extends AbstractNER {

  public static final XMLConfiguration CFG = CfgManager.getCfg(TagMeCommon.class);

  public static final String CFG_KEY_TAGME_KEY = "tagMe.key";
  public static final String CFG_KEY_SPARQL_ENDPOINT = "tagMe.sparqlEndpoint";
  public static final String CFG_KEY_RDFS_PREFIX = "tagMe.rdfsPrefix";
  public static final String CFG_KEY_RDF_PREFIX = "tagMe.rdfPrefix";
  public static final String CFG_KEY_MIN_WEIGHT = "tagMe.minWeight";
  public static final String CFG_KEY_ENDPOINT = "tagMe.url";
  public static final String CFG_KEY_LANG = "tagMe.lang";

  public static final String TAGME_KEY = CFG.getString(CFG_KEY_TAGME_KEY);
  public static final String SPARQL_ENDPOINT = CFG.getString(CFG_KEY_SPARQL_ENDPOINT);
  public static final String RDFS_PREFIX = CFG.getString(CFG_KEY_RDFS_PREFIX);
  public static final String ENDPOINT = CFG.getString(CFG_KEY_ENDPOINT);
  public static final String RDF_PREFIX = CFG.getString(CFG_KEY_RDF_PREFIX);
  public static final double MIN_WEIGHT = CFG.getDouble(CFG_KEY_MIN_WEIGHT);

  public static final String include_categories = "true";
  public static final String epsilon = "0.1";
  public static final String min_comm = "0.1";
  public static final String min_link = "0.1";

  protected final String LANG;

  // public static final int paging = 100;
  public static final int delay = 1000;

  // dbpedia_categories to type
  static int cacheSize = 100000;
  protected ConcurrentMap<String, String> categoriesToTypeLRU =
      new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(cacheSize)
          .build();

  String dbpediaURL;
  String dbpediaGraph;

  /**
   *
   * @param lang
   * @param dbpediaURL
   * @param dbpediaGraph
   */
  public TagMeCommon(final String lang, final String dbpediaURL, final String dbpediaGraph) {
    LOG.info("TagMeCommon ... ");
    LANG = lang;

    entityList = new ArrayList<>();
    this.dbpediaGraph = dbpediaGraph;
    this.dbpediaURL = dbpediaURL;
  }

  @Override
  public List<Entity> retrieve(final String input) {
    return retrieveSentences(getSentences(LANG, input));
  }

  protected List<Entity> retrieveSentences(final List<String> sentences) {

    final ExecutorService executorService = Executors.newFixedThreadPool(4);
    final CompletionService<List<Entity>> completionService =
        new ExecutorCompletionService<>(executorService);

    int n = 0;
    for (int i = 0; i < sentences.size(); i++) {
      completionService.submit(new TagMeCall(sentences.get(i), LANG));
      ++n;
    }
    executorService.shutdown();
    final Set<Entity> set = new HashSet<>();
    for (int i = 0; i < n; ++i) {
      try {
        final Future<List<Entity>> future = completionService.take();
        final List<Entity> result = future.get();

        if ((result != null) && !result.isEmpty()) {
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

    String sentence;
    String lang;

    QueryExecutionFactory qef;

    public TagMeCall(final String sentence, final String lang) {
      this.sentence = sentence;
      this.lang = lang;
    }

    @Override
    public List<Entity> call() throws Exception {
      LOG.info("CALL...");
      LOG.info("send request to TagMe service");
      final String response = send();

      LOG.info("get response annotations");
      final JSONArray annos = handleResponse(response);

      QueryExecutionFactory qef = new QueryExecutionFactoryHttp(dbpediaURL, dbpediaGraph);
      try {
        // qef = new QueryExecutionFactoryPaginated(qef, paging);
        qef = new QueryExecutionFactoryDelay(qef, delay);

      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      LOG.info("get entities");
      final List<Entity> e = getEntities(annos, qef);
      LOG.info("DONE.");
      qef.close();
      return e;
    }

    /**
     * Gets 'annotations' JSONArray
     */
    public JSONArray handleResponse(final String response) {
      // parse response
      try {
        JSONObject jo;
        if (response != null) {
          jo = new JSONObject(response);
          if (jo.has("annotations")) {
            return jo.getJSONArray("annotations");
          } else {
            LOG.warn("No annotations found.");
          }
        }
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
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
        response = Requests.postForm(ENDPOINT,
            Form.form().add("key", TAGME_KEY).add("text", sentence).add("lang", LANG)
                .add("epsilon", epsilon).add("min_comm", min_comm).add("min_link", min_link)
                .add("include_categories", include_categories));
      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      return response;
    }

  } // end class TagMeCall

  protected List<Entity> getEntities(final JSONArray annos, final QueryExecutionFactory qef) {
    final Set<Entity> entities = new HashSet<>();
    final LinkedHashSet<String> titles = new LinkedHashSet<>();
    final List<JSONObject> annosList = new ArrayList<>();

    for (int i = 0; i < annos.length(); i++) {

      if (Double.valueOf(annos.getJSONObject(i).getString("rho")) < MIN_WEIGHT) {
        LOG.debug("roh too small");
      } else {

        if (annos.getJSONObject(i).has("title")) {
          final String title = annos.getJSONObject(i).getString("title");
          // cache
          LOG.debug("categoriesToTypeLRU: " + categoriesToTypeLRU);
          final String type = categoriesToTypeLRU.get(title);
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
    final List<String> types = new ArrayList<>();
    for (final String t : titles) {
      types.add(sendSparql(t, qef));
    }

    final List<String> titlesList = new ArrayList<>(titles);

    for (int i = 0; i < annosList.size(); i++) {
      final String spot = annosList.get(i).getString("spot");
      final String rho = annosList.get(i).getString("rho");
      getSet(annosList.get(i).getJSONArray("dbpedia_categories"));

      final String title = annosList.get(i).getString("title");
      final int index = titlesList.indexOf(title);
      final String type = types.get(index);
      if (!type.equals(EntityClassMap.getNullCategory())) {
        categoriesToTypeLRU.put(title, type);
        entities.add(new Entity(spot, type, Double.valueOf(rho).floatValue(), getToolName()));
      } else {
        // TODO: find type by given category? Seems not to be worth
        LOG.info("not found title: " + title);
        /*
         * if (type.equals(EntityClassMap.getNullCategory())) type = sendSparql(dbpedia_categories,
         * qef); if (!type.equals(EntityClassMap.getNullCategory())) {
         * categoriesToTypeLRU.put(title, type); entities.add(new Entity(spot, type,
         * Double.valueOf(rho).floatValue(), getToolName())); }
         */
      }
    }
    return new ArrayList<>(entities);
  }

  // get to each title a type
  public List<String> sendSparqlMulti(final LinkedHashSet<String> titles,
      final QueryExecutionFactory qef) {
    if (titles == null) {
      throw new NullPointerException("titles parameter is null");
    }

    if (titles.isEmpty()) {
      return new ArrayList<>();
    }

    final List<String> labels = new ArrayList<>(titles);
    final List<String> types = new ArrayList<>();
    final StringBuilder sb = new StringBuilder();
    sb.append("PREFIX  rdfs: <").append(RDFS_PREFIX).append("> ");
    sb.append("PREFIX  rdf: <").append(RDF_PREFIX).append("> ");
    sb.append("SELECT");
    for (int i = 0; i < labels.size(); i++) {
      sb.append(" ?").append("y").append(i);
    }

    sb.append(" WHERE {");
    for (int i = 0; i < labels.size(); i++) {
      sb.append(" ?").append("x").append(i).append(" rdfs:label \"").append(labels.get(i))
          .append("\"@").append(LANG).append(".");
      sb.append(" ?").append("x").append(i).append(" rdf:type ?y").append(i).append(".");
      types.add(EntityClassMap.getNullCategory());
    }
    sb.append(" }");

    final String q = sb.toString();

    String current_results = null;
    try {

      final QueryExecution qe = qef.createQueryExecution(q);
      qe.setTimeout(2 * 60 * 1000);
      final ResultSet rs = qe.execSelect();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsJSON(baos, rs);
      current_results = baos.toString();
    } catch (final Exception e) {
      LOG.error("query: \n" + q);
      LOG.error(e.getLocalizedMessage(), e);
    }

    if (current_results != null) {

      final JSONObject jo = new JSONObject(current_results);
      final List<String> vars = getVars(jo);
      final JSONArray bindings = getBindings(jo);

      if ((bindings.length() > 0) && (vars.size() > 0)) {
        for (int ii = 0; ii < labels.size(); ii++) {
          final String y = "y".concat(String.valueOf(ii));
          if (vars.contains(y)) {
            // all bindings
            for (int i = 0; i < bindings.length(); i++) {
              final JSONObject o = bindings.getJSONObject(i);
              if (o.has(y)) {
                final String type = findType(o.getJSONObject(y).getString("value"));
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
  protected String sendSparql(final Set<String> labelsset, final QueryExecutionFactory qef) {

    String type = EntityClassMap.getNullCategory();

    if (labelsset.isEmpty()) {
      return type;
    }

    final List<String> labels = new ArrayList<>(labelsset);

    final StringBuilder sb = new StringBuilder();
    sb.append("PREFIX  rdfs: <").append(RDFS_PREFIX).append("> ");
    sb.append("PREFIX  rdf: <").append(RDF_PREFIX).append("> ");
    sb.append("SELECT");
    for (int i = 0; i < labels.size(); i++) {
      sb.append(" ?").append("y").append(i);
    }

    sb.append(" WHERE {");
    for (int i = 0; i < labels.size(); i++) {
      sb.append(" ?").append("x").append(i).append(" rdfs:label \"").append(labels.get(i))
          .append("\"@").append(LANG).append(".");
      sb.append(" ?").append("x").append(i).append(" rdf:type ?y").append(i).append(".");
    }
    sb.append(" }");

    final String q = sb.toString();

    String current_results = null;
    try {
      final ResultSet rs = qef.createQueryExecution(q).execSelect();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsJSON(baos, rs);
      current_results = baos.toString();
    } catch (final Exception e) {
      LOG.error(q);
      LOG.error(e.getLocalizedMessage(), e);
    }

    if (current_results != null) {

      final JSONObject jo = new JSONObject(current_results);
      final List<String> vars = getVars(jo);
      final JSONArray bindings = getBindings(jo);

      if ((bindings.length() > 0) && (vars.size() > 0)) {
        for (int ii = 0; ii < labels.size(); ii++) {
          final String y = "y".concat(String.valueOf(ii));
          if (vars.contains(y)) {
            // all bindings
            for (int i = 0; i < bindings.length(); i++) {
              final JSONObject o = bindings.getJSONObject(i);
              if (o.has(y)) {
                type = findType(o.getJSONObject(y).getString("value"));
                if (!type.equals(EntityClassMap.getNullCategory())) {
                  break;
                }
              }
            }
          }
        }
      }
    }
    return type;
  }

  // get the type for all label
  protected String sendSparql(final String label, final QueryExecutionFactory qef) {
    final Set<String> set = new HashSet<String>();
    set.add(label);
    return sendSparql(set, qef);
  }

  /**
   *
   * @param jo
   * @return
   */
  protected JSONArray getBindings(final JSONObject jo) {
    JSONArray bindings = new JSONArray();
    if (jo.has("results")) {
      final JSONObject results = jo.getJSONObject("results");
      if (results.has("bindings")) {
        bindings = results.getJSONArray("bindings");
      }
    }
    return bindings;
  }

  /**
   *
   * @param jo
   * @return
   */
  protected List<String> getVars(final JSONObject jo) {
    final List<String> vars = new ArrayList<>();
    if (jo.has("head")) {
      final JSONObject head = jo.getJSONObject("head");
      if (head.has("vars")) {
        final JSONArray varsja = head.getJSONArray("vars");
        for (int i = 0; i < varsja.length(); i++) {
          vars.add(varsja.getString(i));
        }
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
  protected LinkedHashSet<String> getSet(final JSONArray ja) {
    final LinkedHashSet<String> set = new LinkedHashSet<>();
    for (int ii = 0; ii < ja.length(); ii++) {
      set.add(ja.getString(ii));
    }
    return set;
  }

  /**
   * Gets the entity type.
   */
  protected String findType(final String text) {
    String t = EntityClassMap.getNullCategory();
    if ((text == null) || text.trim().isEmpty()) {
      return t;
    }

    if (text.toLowerCase().contains("person")) {
      t = EntityClassMap.P;
    }

    if (text.toLowerCase().contains("organisation")
        || text.toLowerCase().contains("organization")) {
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
