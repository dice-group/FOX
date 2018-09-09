var voc = {
  ns_dbo  :  'http://dbpedia.org/ontology/',
  ns_dbr  :  'http://dbpedia.org/resource/',
  ns_foaf  :  'http://xmlns.com/foaf/0.1/',
  ns_foxo  :  'http://ns.aksw.org/fox/ontology#',
  ns_foxr  :  'http://ns.aksw.org/fox/resource#',
  ns_its  :  'http://www.w3.org/2005/11/its/rdf#',
  ns_nif  :  'http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#',
  ns_prov  :  'http://www.w3.org/ns/prov#',
  ns_rdf  :  'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
  ns_rdfs  :  'http://www.w3.org/2000/01/rdf-schema#',
  ns_schema  :  'http://schema.org/',
  ns_xsd  :  'http://www.w3.org/2001/XMLSchema#',
  ns_oa : 'http://www.w3.org/ns/oa#'
};

voc.oa = {
  hasTarget : voc.ns_oa + 'hasTarget',
  hasSource : voc.ns_oa + 'hasSource'
};

voc.foxo = {
  RelationExtraction : voc.ns_foxo + 'RelationExtraction',
  subjectphrase : voc.ns_foxo + 'subjectPhrase',
  objectphrase : voc.ns_foxo + 'objectPhrase'
};

voc.nif = {
  Context: voc.ns_nif + 'Context',
  Phrase: voc.ns_nif + 'Phrase',
  anchorOf : voc.ns_nif + 'anchorOf',
  beginIndex : voc.ns_nif + 'beginIndex',
  endIndex : voc.ns_nif + 'endIndex',
  referenceContext : voc.ns_nif + 'referenceContext',
  isString : voc.ns_nif + 'isString'
};

voc.its = {
  taClassRef : voc.ns_its + 'taClassRef',
  taIdentRef : voc.ns_its + 'taIdentRef'
};

voc.prov = {
  used : voc.ns_prov + 'used',
  generated  : voc.ns_prov + 'generated'
};

voc.rdf = {
  type:  voc.ns_rdf + 'type',
  subject : voc.ns_rdf + 'subject',
  predicate  : voc.ns_rdf + 'predicate',
  object  : voc.ns_rdf + 'object'
};
