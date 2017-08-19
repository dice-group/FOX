var
ns_nif = 'http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#',
ns_rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
ns_its = 'http://www.w3.org/2005/11/its/rdf#',
ns_foxo = 'http://ns.aksw.org/fox/ontology#';

var nif = {
    anchorOf : ns_nif + 'anchorOf',
    beginIndex : ns_nif + 'beginIndex',
    endIndex : ns_nif + 'endIndex',
    referenceContext : ns_nif + 'referenceContext',
    isString : ns_nif + 'isString'
};

var its = {
  taClassRef : ns_its + 'taClassRef',
  taIdentRef : ns_its + 'taIdentRef'
};

/**
@prefix dbo:   <http://dbpedia.org/ontology/> .
@prefix foxo:  <http://ns.aksw.org/fox/ontology#> .
@prefix schema: <http://schema.org/> .
@prefix foxr:  <http://ns.aksw.org/fox/resource#> .
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix dbr:   <http://dbpedia.org/resource/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix its:   <http://www.w3.org/2005/11/its/rdf#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .
@prefix prov:  <http://www.w3.org/ns/prov#> .
@prefix foaf:  <http://xmlns.com/foaf/0.1/> .
*/
