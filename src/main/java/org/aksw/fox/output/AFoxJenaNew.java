package org.aksw.fox.output;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Voc;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AFoxJenaNew {
  public static Logger LOG = LogManager.getLogger(AFoxJenaNew.class);

  protected Model graph = ModelFactory.createDefaultModel();
  protected String lang = Lang.TURTLE.getName();

  int documentCounter = 0;
  protected UrlValidator urlValidator = new UrlValidator();

  /**
   * Maps EntityClassMap types to KB types.
   */
  protected static Map<String, Set<String>> typesmap = new HashMap<>();
  static {
    typesmap.put(EntityClassMap.P.toString(),
        new HashSet<String>(Arrays.asList(//
            "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person", //
            "http://schema.org/Person", //
            "http://dbpedia.org/ontology/Person")));

    typesmap.put(EntityClassMap.L.toString(),
        new HashSet<String>(Arrays.asList(//
            "http://www.ontologydesignpatterns.org/ont/d0.owl#Location", //
            "http://schema.org/Place", //
            "http://schema.org/Location", //
            "http://dbpedia.org/ontology/Place")));

    typesmap.put(EntityClassMap.O.toString(),
        new HashSet<String>(Arrays.asList(//
            "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Organization", //
            "http://schema.org/Organisation", //
            "http://dbpedia.org/ontology/Organisation")));
  }

  /**
   *
   * Constructor.
   *
   */
  public AFoxJenaNew() {
    initGraph();
  }

  /**
   * Initializes an empty Model.
   */
  public void initGraph() {
    graph = ModelFactory.createDefaultModel();
    documentCounter = 0;
    lang = Lang.TURTLE.getName();

    graph.setNsPrefix("oa", Voc.ns_oa);
    graph.setNsPrefix("foxo", Voc.ns_fox_ontology);
    graph.setNsPrefix("foxr", Voc.ns_fox_resource);
    graph.setNsPrefix("dbo", Voc.ns_dbpedia_ontology);
    graph.setNsPrefix("dbr", Voc.ns_dbpedia_resource);
    graph.setNsPrefix("its", Voc.ns_its);
    graph.setNsPrefix("nif", Voc.ns_nif);
    graph.setNsPrefix("prov", Voc.ns_prov);
    graph.setNsPrefix("foaf", Voc.ns_foaf);
    graph.setNsPrefix("schema", Voc.ns_schema);
    graph.setNsPrefix("xsd", XSD.getURI());
    graph.setNsPrefix("rdf", RDF.getURI());
    graph.setNsPrefix("rdfs", RDFS.getURI());
  }

  public void setLang(final String lang) {
    this.lang = lang;
  }

  public void reset() {
    initGraph();
  }

  public String print() {
    final StringWriter sw = new StringWriter();
    if (graph == null) {
      LOG.warn("Graph is null");
    }
    if (lang == null) {
      LOG.warn("lang is null: using turtle");
      lang = Lang.TURTLE.getName();
    }

    RDFDataMgr.write(sw, graph, RDFLanguages.nameToLang(lang));
    return sw.toString();
  }
}
