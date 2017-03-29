package org.aksw.fox.tools.ner.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.fox.data.Entity;
import org.aksw.fox.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.http.client.fluent.Form;
import org.apache.http.entity.ContentType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import de.renespeck.swissknife.http.Requests;

class TagMeCall implements Callable<List<Entity>> {
  public static final Logger LOG = LogManager.getLogger(TagMeCall.class);

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
  public static final double MIN_RHO = CFG.getDouble(CFG_KEY_MIN_WEIGHT);

  public static final String include_categories = "true";
  public static final String epsilon = "0.1";
  public static final String min_comm = "0.1";
  public static final String min_link = "0.1";
  String sentence;
  Locale lang;

  // QueryExecutionFactory qef;
  Map<String, String> entityClassMap;

  public TagMeCall(final String sentence, final Locale lang,
      final Map<String, String> entityClassMap) {
    this.sentence = sentence;
    this.lang = lang;
    this.entityClassMap = entityClassMap;
  }

  @Override
  public List<Entity> call() {

    final JSONObject response = send();
    final Set<Entity> entities = new HashSet<>();
    if (response.has("annotations")) {
      final JSONArray annos = response.getJSONArray("annotations");
      for (int i = 0; i < annos.length(); i++) {
        final JSONObject anno = annos.getJSONObject(i);
        if (anno.has("dbpedia_categories") && anno.has("spot") && anno.has("rho")
            && (anno.getDouble("rho") >= MIN_RHO)) {
          final JSONArray ja = anno.getJSONArray("dbpedia_categories");

          for (int ii = 0; ii < ja.length(); ii++) {
            final String tmpType = entityClassMap.get(ja.getString(ii).replace(" ", "_"));

            if ((tmpType != null)) {
              entities.add(new Entity(anno.getString("spot"), tmpType));
              break;
            }
          }
        }
      }
    } else {
      LOG.warn("No annotations found.");
    }
    return new ArrayList<>(entities);

  }

  /**
   * TagMe request.
   */
  public JSONObject send() {
    String response = "";
    try {
      response = Requests.postForm(ENDPOINT,
          Form.form()//
              .add("key", TAGME_KEY)//
              .add("text", sentence)//
              .add("lang", lang.getLanguage())//
              .add("epsilon", epsilon)//
              .add("min_comm", min_comm)//
              .add("min_link", min_link)//
              .add("include_categories", include_categories),
          ContentType.APPLICATION_JSON);
      return new JSONObject(response);
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return new JSONObject();
  }
}
