package org.aksw.fox.output;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.EntityClassMap;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
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
  public static final String akswNotInWiki = "http://aksw.org/notInWiki/";
  public static final String ns_fox_ontology = "http://ns.aksw.org/fox/ontology#";
  public static final String ns_fox_resource = "http://ns.aksw.org/fox/resource#";
  public static final String ns_dbpedia_ontology = "http://dbpedia.org/ontology/";
  public static final String ns_dbpedia_resource = "http://dbpedia.org/resource/";
  public static final String ns_schema = "http://schema.org/";
  public static final String ns_prov = "http://www.w3.org/ns/prov#";
  public static final String ns_foaf = "http://xmlns.com/foaf/0.1/";
  public static final String ns_its = "http://www.w3.org/2005/11/its/rdf#";
  public static final String ns_nif =
      "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";

  protected Model graph = ModelFactory.createDefaultModel();
  protected String lang = Lang.TURTLE.getName();
  // protected String lang = Lang.RDFJSON.getName();

  int documentCounter = 0;
  protected UrlValidator urlValidator = new UrlValidator();

  // nif
  protected Property pNifContext = graph.createProperty(ns_nif.concat("Context"));
  protected Property pNifBegin = graph.createProperty(ns_nif.concat("beginIndex"));
  protected Property pNifEnd = graph.createProperty(ns_nif.concat("endIndex"));
  protected Property pNifIsString = graph.createProperty(ns_nif.concat("isString"));
  protected Property pNifPhrase = graph.createProperty(ns_nif.concat("Phrase"));
  protected Property pNifAnchorOf = graph.createProperty(ns_nif.concat("anchorOf"));
  protected Property pNifreferenceContext = graph.createProperty(ns_nif.concat("referenceContext"));

  // itsrdf
  protected Property pItsrdfTaIdentRef = graph.createProperty(ns_its.concat("taIdentRef"));
  protected Property pItsrdfTaClassRef = graph.createProperty(ns_its.concat("taClassRef"));

  // prov
  protected Property pProvActivity = graph.createProperty(ns_prov.concat("Activity"));
  protected Property pProvStartedAtTime = graph.createProperty(ns_prov.concat("startedAtTime"));
  protected Property pProvUsed = graph.createProperty(ns_prov.concat("used"));
  protected Property pProvGenerated = graph.createProperty(ns_prov.concat("generated"));
  protected Property pProvEndedAtTime = graph.createProperty(ns_prov.concat("endedAtTime"));

  protected Property pProvSoftwareAgent = graph.createProperty(ns_prov.concat("SoftwareAgent"));

  // schema
  protected Property pSchemaSoftwareAgent =
      graph.createProperty(ns_schema.concat("SoftwareApplication"));
  protected Property pSchemaSoftwareVersion =
      graph.createProperty(ns_schema.concat("softwareVersion"));

  // fox onto
  protected Property pActivityRE =
      graph.createProperty(ns_fox_ontology.concat("RelationExtraction"));
  protected Property pActivityNER =
      graph.createProperty(ns_fox_ontology.concat("NamedEntityRecognition"));

  protected Property pRelationDomain = graph.createProperty(RDF.getURI().concat("subject"));
  protected Property pRelationrelation = graph.createProperty(RDF.getURI().concat("predicate"));
  protected Property pRelationRange = graph.createProperty(RDF.getURI().concat("object"));
  protected Property pRelationRelation = graph.createProperty(ns_fox_ontology.concat("Relation"));

  // foaf
  protected Property pFoafName = graph.createProperty(ns_foaf.concat("name"));

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

    graph.setNsPrefix("foxo", ns_fox_ontology);
    graph.setNsPrefix("foxr", ns_fox_resource);
    graph.setNsPrefix("dbo", ns_dbpedia_ontology);
    graph.setNsPrefix("dbr", ns_dbpedia_resource);
    graph.setNsPrefix("its", ns_its);
    graph.setNsPrefix("nif", ns_nif);
    graph.setNsPrefix("prov", ns_prov);
    graph.setNsPrefix("foaf", ns_foaf);
    graph.setNsPrefix("schema", ns_schema);
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
    RDFDataMgr.write(sw, graph, RDFLanguages.nameToLang(lang));
    return sw.toString();
  }

}
