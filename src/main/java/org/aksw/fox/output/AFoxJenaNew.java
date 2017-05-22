package org.aksw.fox.output;

import java.io.StringWriter;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AFoxJenaNew {
  public static Logger LOG = LogManager.getLogger(AFoxJenaNew.class);

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

  int document = 0;
  protected UrlValidator urlValidator = new UrlValidator();

  // nif
  protected Property propertyNifContext, propertyNifBeginIndex, propertyNifEndIndex,
      propertyNifIsString, propertyNifPhrase, propertyNifAnchorOf, propertyNifreferenceContext;

  // itsrdf
  protected Property propertyItsrdfTaIdentRef, propertyItsrdfTaClassRef;

  // prov
  protected Property propertyProvActivity, propertyProvStartedAtTime, propertyProvUsed,
      propertyProvGenerated, propertyProvEndedAtTime, propertyProvSoftwareAgent;

  // fox onto
  protected Property propertyActivityRelationExtraction, propertyActivityNamedEntityRecognition,
      propertyRelationDomain, propertyRelationRange, propertyRelationRelation,
      propertyRelationrelation;

  // schema
  protected Property propertySchemaSoftwareAgent, propertySchemaSoftwareVersion;

  // foaf
  protected Property propertyFoafName;

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
    document = 0;
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

    // nif
    propertyNifContext = graph.createProperty(ns_nif.concat("Context"));
    propertyNifBeginIndex = graph.createProperty(ns_nif.concat("beginIndex"));
    propertyNifEndIndex = graph.createProperty(ns_nif.concat("endIndex"));
    propertyNifIsString = graph.createProperty(ns_nif.concat("isString"));
    propertyNifPhrase = graph.createProperty(ns_nif.concat("Phrase"));
    propertyNifAnchorOf = graph.createProperty(ns_nif.concat("anchorOf"));
    propertyNifreferenceContext = graph.createProperty(ns_nif.concat("referenceContext"));

    // itsrdf
    propertyItsrdfTaIdentRef = graph.createProperty(ns_its.concat("taIdentRef"));
    propertyItsrdfTaClassRef = graph.createProperty(ns_its.concat("taClassRef"));

    // prov
    propertyProvActivity = graph.createProperty(ns_prov.concat("Activity"));
    propertyProvStartedAtTime = graph.createProperty(ns_prov.concat("startedAtTime"));
    propertyProvUsed = graph.createProperty(ns_prov.concat("used"));
    propertyProvGenerated = graph.createProperty(ns_prov.concat("generated"));
    propertyProvEndedAtTime = graph.createProperty(ns_prov.concat("endedAtTime"));

    propertyProvSoftwareAgent = graph.createProperty(ns_prov.concat("SoftwareAgent"));

    // schema
    propertySchemaSoftwareAgent = graph.createProperty(ns_schema.concat("SoftwareApplication"));
    propertySchemaSoftwareVersion = graph.createProperty(ns_schema.concat("softwareVersion"));

    // fox onto
    propertyActivityRelationExtraction =
        graph.createProperty(ns_fox_ontology.concat("RelationExtraction"));
    propertyActivityNamedEntityRecognition =
        graph.createProperty(ns_fox_ontology.concat("NamedEntityRecognition"));

    propertyRelationDomain = graph.createProperty(ns_fox_ontology.concat("domain"));

    propertyRelationRange = graph.createProperty(ns_fox_ontology.concat("range"));

    propertyRelationRelation = graph.createProperty(ns_fox_ontology.concat("Relation"));

    propertyRelationrelation = graph.createProperty(ns_fox_ontology.concat("relation"));

    // foaf
    propertyFoafName = graph.createProperty(ns_foaf.concat("name"));

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
