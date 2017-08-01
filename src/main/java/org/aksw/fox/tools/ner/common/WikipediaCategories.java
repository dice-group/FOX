package org.aksw.fox.tools.ner.common;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import de.renespeck.swissknife.io.SerializationUtil;

/**
 * Creates dbpedia types for TagMe
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WikipediaCategories {
  public static final Logger LOG = LogManager.getLogger(WikipediaCategories.class);

  public String getQuery(final String type) {
    final StringBuilder sb = new StringBuilder();
    sb.append("prefix dbr: <http://dbpedia.org/resource/>");
    sb.append("prefix dbo: <http://dbpedia.org/ontology/>");
    sb.append("prefix dct: <http://purl.org/dc/terms/>");

    sb.append("select distinct(?cat) where { ");
    sb.append("?s a ").append(type).append(".");
    sb.append("?s dct:subject ?cat");
    sb.append("}");
    // sb.append("} Limit 1000");
    return sb.toString();
  }

  public Set<String> getCategories(final JSONObject response) {
    final Set<String> cats = new HashSet<>();
    if (response.has("results")) {
      final JSONObject results = response.getJSONObject("results");
      if (results.has("bindings")) {
        final JSONArray bindings = results.getJSONArray("bindings");
        for (int i = 0; i < bindings.length(); i++) {
          final JSONObject jo = bindings.getJSONObject(i);
          if (jo.has("cat")) {
            final JSONObject cat = jo.getJSONObject("cat");
            if (cat.has("value")) {
              final String value = cat.getString("value");
              cats.add(value);
            }
          }
        }
      }
    }
    return cats;
  }

  public JSONObject queryExecution(final String query) {

    final String service = "http://dbpedia.org/sparql";
    final String graph = "http://dbpedia.org";

    QueryExecutionFactory qef = null;
    try {
      qef = new QueryExecutionFactoryHttp(service, graph);
      qef = new QueryExecutionFactoryPaginated(qef, 10000);
      // qef = new QueryExecutionFactoryDelay(qef, 1);

      final ResultSet rs = qef.createQueryExecution(query).execSelect();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsJSON(baos, rs);
      return new JSONObject(baos.toString());
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    if (qef != null) {
      try {
        qef.close();
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return new JSONObject();
  }

  @SuppressWarnings("unchecked")
  public Set<String> queryExecAndSerialize(final String file, final String type) {
    Set<String> cats = null;
    cats = SerializationUtil.deserialize(file, new HashSet<String>().getClass());
    if (cats == null) {
      cats = getCategories(queryExecution(getQuery(type)));
      try {
        SerializationUtil.serialize(file, cats);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return cats;
  }
}
