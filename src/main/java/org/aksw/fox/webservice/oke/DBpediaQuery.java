package org.aksw.fox.webservice.oke;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.Pair;

public class DBpediaQuery {

  public static Logger LOG = LogManager.getLogger(DBpediaQuery.class);

  protected String uriPrefix = "http://dbpedia.org/ontology/";

  /**
   *
   * @param s e.g. http://dbpedia.org/resource/Albert_Einstein
   * @param o e.g. http://dbpedia.org/resource/Alfred_Kleiner
   * @return pair of URIs, first for s,o order and second for o,s order.
   */
  public Pair<Set<String>, Set<String>> query(final String s, final String o) {
    final Pair<Set<String>, Set<String>> pair = new Pair<Set<String>, Set<String>>();
    if ((s != null) && !s.isEmpty() && (o != null) && !o.isEmpty()) {
      final String query = makeQuery(s, o);
      final Map<String, Set<String>> map = queryExecution(query);

      pair.first = map.get("x");
      pair.second = map.get("y");
    }
    return pair;
  }

  protected String makeQuery(final String s, final String o) {
    String query = "" //
        + " PREFIX  owl: <http://www.w3.org/2002/07/owl#> "
        + " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
        + " PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
        + " PREFIX  foaf: <http://xmlns.com/foaf/0.1/> "
        + " PREFIX  dbr: <http://dbpedia.org/resource/> "
        + " PREFIX  dbo: <http://dbpedia.org/ontology/> "
        + " PREFIX  dbp: <http://dbpedia.org/property/> "
        + " select ?x ?y { <%s> ?x  <%s> . <%s> ?y  <%s> .  }";
    query = String.format(query, s, o, o, s);
    LOG.info(query);
    return query;
  }

  protected Map<String, Set<String>> queryExecution(final String query) {
    final Map<String, Set<String>> map = new HashMap<>();
    final Query q = QueryFactory.create(query);
    try (QueryExecution qexec =
        QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q)) {
      ((QueryEngineHTTP) qexec).addParam("timeout", "10000");
      final ResultSet rs = qexec.execSelect();
      final List<QuerySolution> solutions = ResultSetFormatter.toList(rs);
      for (final QuerySolution solution : solutions) {
        final Iterator<String> iter = solution.varNames();
        while (iter.hasNext()) {
          final String varName = iter.next();
          final Resource r = solution.getResource(varName);
          if (map.get(varName) == null) {
            map.put(varName, new HashSet<String>());
          }

          final String uri = r.getURI();
          if (uri.startsWith(uriPrefix) && !uri.endsWith("wikiPageWikiLink")) {
            map.get(varName).add(r.getURI().replace(uriPrefix, ""));
          }
        }
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return map;
  }
}
