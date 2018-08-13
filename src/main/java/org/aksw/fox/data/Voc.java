package org.aksw.fox.data;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

public class Voc {

  public static final String akswNotInWiki = "http://aksw.org/notinwiki/";
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
  public static final String ns_oa = "http://www.w3.org/ns/oa#";

  // nif
  public static final Property pNifContext =
      ResourceFactory.createProperty(ns_nif.concat("Context"));
  public static final Property pNifBegin =
      ResourceFactory.createProperty(ns_nif.concat("beginIndex"));
  public static final Property pNifEnd = ResourceFactory.createProperty(ns_nif.concat("endIndex"));
  public static final Property pNifIsString =
      ResourceFactory.createProperty(ns_nif.concat("isString"));
  public static final Property pNifCString =
      ResourceFactory.createProperty(ns_nif.concat("CString"));
  public static final Property pNifPhrase = ResourceFactory.createProperty(ns_nif.concat("Phrase"));
  public static final Property pNifAnchorOf =
      ResourceFactory.createProperty(ns_nif.concat("anchorOf"));
  public static final Property pNifreferenceContext =
      ResourceFactory.createProperty(ns_nif.concat("referenceContext"));

  // itsrdf
  public static final Property pItsrdfTaIdentRef =
      ResourceFactory.createProperty(ns_its.concat("taIdentRef"));
  public static final Property pItsrdfTaClassRef =
      ResourceFactory.createProperty(ns_its.concat("taClassRef"));

  // prov
  public static final Property pProvActivity =
      ResourceFactory.createProperty(ns_prov.concat("Activity"));
  public static final Property pProvStartedAtTime =
      ResourceFactory.createProperty(ns_prov.concat("startedAtTime"));
  public static final Property pProvUsed = ResourceFactory.createProperty(ns_prov.concat("used"));
  public static final Property pProvGenerated =
      ResourceFactory.createProperty(ns_prov.concat("generated"));
  public static final Property pProvEndedAtTime =
      ResourceFactory.createProperty(ns_prov.concat("endedAtTime"));
  public static final Property pProvSoftwareAgent =
      ResourceFactory.createProperty(ns_prov.concat("SoftwareAgent"));

  // schema
  public static final Property pSchemaSoftwareAgent =
      ResourceFactory.createProperty(ns_schema.concat("SoftwareApplication"));
  public static final Property pSchemaSoftwareVersion =
      ResourceFactory.createProperty(ns_schema.concat("softwareVersion"));

  // fox onto
  public static final Property pFoxRE =
      ResourceFactory.createProperty(ns_fox_ontology.concat("RelationExtraction"));
  public static final Property pFoxNER =
      ResourceFactory.createProperty(ns_fox_ontology.concat("NamedEntityRecognition"));
  public static final Property pFoxRelation =
      ResourceFactory.createProperty(ns_fox_ontology.concat("Relation"));

  // RDF
  public static final Property pRdfSubject =
      ResourceFactory.createProperty(RDF.getURI().concat("subject"));
  public static final Property pRdfPredicate =
      ResourceFactory.createProperty(RDF.getURI().concat("predicate"));
  public static final Property pRdfObject =
      ResourceFactory.createProperty(RDF.getURI().concat("object"));

  // foaf
  public static final Property pFoafName = ResourceFactory.createProperty(ns_foaf.concat("name"));

  // oa
  public static final Property pHasSource =
      ResourceFactory.createProperty(ns_oa.concat("hasSource"));
  public static final Property pHasTarget =
      ResourceFactory.createProperty(ns_oa.concat("hasTarget"));
  public static final Property pSpecificResource =
      ResourceFactory.createProperty(ns_oa.concat("SpecificResource"));
  public static final Property pAnnotation =
      ResourceFactory.createProperty(ns_oa.concat("Annotation"));
}
