package org.aksw.fox.output;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.IData;
import org.aksw.fox.data.Relation;
import org.aksw.fox.utils.DataTestFactory;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxJenaNew extends AFoxJenaNew implements IFoxJena {

  Resource inputResource = null;
  String baseuri = null;

  public static void main(final String args[]) {

    final IFoxJena foxJena = new FoxJenaNew();

    // final Calendar i = Calendar.getInstance();
    // i.setTime("");
    // i.setTime(new Date("2017-05-02T17:35:02.057Z"));
    // LOG.info(i.getTimeInMillis());

    final DataTestFactory dtf = new DataTestFactory();
    final Set<Entity> entities = new ArrayList<Set<Entity>>(dtf.getTestEntities().values()).get(0);
    final Set<Relation> relations = dtf.getTestRelations().entrySet().iterator().next().getValue();

    final String input = dtf.getTestEntities().entrySet().iterator().next().getKey();
    final String start = DatatypeConverter.printDateTime(new GregorianCalendar());
    final String end = DatatypeConverter.printDateTime(new GregorianCalendar());

    foxJena.addInput(input, "http://example_uri.com");
    foxJena.addEntities(entities, start, end);
    foxJena.addRelations(relations, start, end);

    LOG.info("Jena model: \n\n" + foxJena.print());
  }

  /**
   * Adds an agent and returns it's uri.
   *
   * @param name
   * @param verion
   * @return
   */
  protected String addSoftwareAgent(final String toolName, final String version) {
    LOG.info("Adds sSoftwareAgent");
    final Resource resource = graph.createResource(ns_fox_resource.concat(toolName));

    resource.addProperty(RDF.type, propertyProvSoftwareAgent);
    resource.addProperty(RDF.type, propertySchemaSoftwareAgent);

    resource.addLiteral(propertySchemaSoftwareVersion, graph.createTypedLiteral(//
        new String(version), XSD.xstring.getURI()));

    resource.addLiteral(propertyFoafName, graph.createTypedLiteral(//
        new String(toolName), XSD.xstring.getURI()));

    return resource.getURI();
  }

  protected void addActivity(final Set<String> uris, final String start, final String end,
      final Property propertyActivity, final String toolUri) {

    LOG.info("Add activity ");

    final Resource resource = graph.createResource();

    resource.addProperty(RDF.type, propertyProvActivity);
    resource.addProperty(RDF.type, propertyActivity);

    resource.addLiteral(propertyProvStartedAtTime, graph.createTypedLiteral(start));
    resource.addLiteral(propertyProvEndedAtTime, graph.createTypedLiteral(end));
    uris.forEach(uri -> {
      resource.addProperty(propertyProvGenerated, graph.getResource(uri));
    });

    resource.addProperty(propertyProvUsed, graph.getResource(toolUri));
  }

  /**
   * Entities from an activity. All entities are expected to be created from one tool.
   *
   */
  @Override
  public void addEntities(final Set<Entity> entities, final String start, final String end) {

    if ((entities != null) && !entities.isEmpty()) {
      final Set<String> uris = _addEntities(entities);

      final String toolName = getToolname(entities);
      LOG.info(toolName);
      final String toolUri = addSoftwareAgent(toolName, "TODO");

      addActivity(uris, start, end, propertyActivityNamedEntityRecognition, toolUri);
    }
  }

  @Override
  public void addRelations(final Set<Relation> relations, final String start, final String end) {
    LOG.info("Add relations.");

    if ((relations != null) && !relations.isEmpty()) {
      final Set<String> uris = _addRelations(relations);
      final String toolName = getToolname(relations);
      final String toolUri = addSoftwareAgent(toolName, "TODO");

      addActivity(uris, start, end, propertyActivityRelationExtraction, toolUri);
    }
  }

  /**
   * Gives other named entity types.
   *
   * @param foxET
   * @return
   */

  private Set<String> otherET(final String foxET) {

    final String DOLCE_PERSON_TYPE_URI =
        "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person";
    final String DOLCE_LOCATION_TYPE_URI =
        "http://www.ontologydesignpatterns.org/ont/d0.owl#Location";
    final String DOLCE_ORGANIZATION_TYPE_URI =
        "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Organization";

    final Set<String> types = new HashSet<>();

    if (foxET.equals(EntityClassMap.P.toString())) {
      types.add(DOLCE_PERSON_TYPE_URI);
      types.add("http://schema.org/Person");
      types.add("http://dbpedia.org/ontology/Person");
    } else if (foxET.equals(EntityClassMap.L.toString())) {

      types.add(DOLCE_LOCATION_TYPE_URI);
      types.add("http://schema.org/Place");
      types.add("http://schema.org/Location");
      types.add("http://dbpedia.org/ontology/Place");
    } else if (foxET.equals(EntityClassMap.O.toString())) {
      types.add(DOLCE_ORGANIZATION_TYPE_URI);
      types.add("http://schema.org/Organisation");
      types.add("http://dbpedia.org/ontology/Organisation");
    }

    return types;

  }

  private Set<String> _addEntities(final Set<Entity> entities) {
    LOG.info("Add entities.");
    final Set<String> uris = new HashSet<>();

    for (final Entity entity : entities) {
      if (!urlValidator.isValid(entity.uri)) {
        LOG.error("URI isn't valid: " + entity.uri);
      } else {

        for (final Integer index : entity.getIndices()) {

          final Resource resource = graph.createResource(//
              createDocUri(baseuri, index, index + entity.getText().length())//
          );

          uris.add(resource.getURI());

          resource.addProperty(RDF.type, propertyNifPhrase);

          // beginIndex
          resource.addLiteral(propertyNifBeginIndex, graph.createTypedLiteral(//
              new Integer(index), XSD.nonNegativeInteger.getURI()));

          // endIndex
          resource.addLiteral(propertyNifEndIndex, graph.createTypedLiteral(//
              new Integer(index + entity.getText().length()), XSD.nonNegativeInteger.getURI()));

          // taIdentRef
          resource.addProperty(propertyItsrdfTaIdentRef, graph.createResource(entity.uri));

          // class
          resource.addProperty(propertyItsrdfTaClassRef,
              graph.createResource(ns_fox_ontology + entity.getType()));

          final Set<String> others = otherET(entity.getType());

          for (final String s : others) {
            resource.addProperty(propertyItsrdfTaClassRef, graph.createResource(s));
          }

          // anchorOf
          resource.addLiteral(propertyNifAnchorOf, graph.createTypedLiteral(//
              new String(entity.getText()), XSD.xstring.getURI()));

          // referenceContext
          resource.addProperty(propertyNifreferenceContext, inputResource);
        }
      }
    }
    return uris;
  }

  private Set<String> _addRelations(final Set<Relation> relations) {
    final Set<String> uris = new HashSet<>();

    final Set<Relation> nofound = new HashSet<>();

    for (final Relation relation : relations) {
      final Entity oe = relation.getObjectEntity();
      final Entity se = relation.getSubjectEntity();

      Resource roe = null;
      Resource rse = null;

      final ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, propertyNifPhrase);
      while (iterEntities.hasNext()) {
        final Resource resource = iterEntities.nextResource();

        for (final Statement indicies : resource.listProperties(propertyNifBeginIndex).toSet()) {
          final int index = indicies.getInt();
          if (oe.getIndices().contains(index)) {
            roe = resource;
          } else if (se.getIndices().contains(index)) {
            rse = resource;
          }
        }
      }

      // Add to model
      if ((roe != null) && (rse != null)) {

        final Date date = DatatypeConverter
            .parseDate(DatatypeConverter.printDateTime(new GregorianCalendar())).getTime();
        final long time = (date.getTime());

        String urir = rse.getProperty(propertyItsrdfTaIdentRef).getResource().getURI().concat("_")
            .concat(String.valueOf(time));

        urir = ns_fox_resource
            .concat(rse.getProperty(propertyItsrdfTaIdentRef).getResource().getLocalName());
        urir = urir.concat("_").concat(String.valueOf(time));

        final Resource resource = graph.createResource(urir);
        uris.add(urir);

        resource.addProperty(RDF.type, propertyRelationRelation);

        resource.addProperty(propertyRelationDomain,
            rse.getProperty(propertyItsrdfTaIdentRef).getResource());

        resource.addProperty(propertyRelationRange,
            roe.getPropertyResourceValue(propertyItsrdfTaIdentRef));
        for (final URI uri : relation.getRelation()) {
          resource.addProperty(propertyRelationrelation, graph.createProperty(uri.toString()));
        }
        /**
         * <code>
         final Resource resRel =
             graph.createResource(rse.getProperty(propertyItsrdfTaIdentRef).getObject().toString());

         for (final URI uri : relation.getRelation()) {
           resRel.addProperty(graph.createProperty(uri.toString()),
               roe.getPropertyResourceValue(propertyItsrdfTaIdentRef));
         }
         </code>
         */

      } else {
        nofound.add(relation);
        if (LOG.isDebugEnabled()) {
          LOG.debug("not found: ");
          LOG.debug(relation);
        }
      }

    }

    relations.removeAll(nofound);

    return uris;
  }

  @Override
  public void addInput(final String input, String uri) {
    document++;
    baseuri = uri;

    if (baseuri == null) {
      uri = getDefaultDocumentURI();
    }

    final String currecntUri = createDocUri(baseuri, 0, (input.length()));

    inputResource = graph.createResource(currecntUri);
    inputResource.addProperty(RDF.type, propertyNifContext);

    // endIndex
    inputResource.addLiteral(propertyNifBeginIndex, graph.createTypedLiteral(//
        new Integer(0), XSD.nonNegativeInteger.getURI()));

    // endIndex
    inputResource.addLiteral(propertyNifEndIndex, graph.createTypedLiteral(//
        new Integer(input.length()), XSD.nonNegativeInteger.getURI()));

    // isString
    inputResource.addLiteral(propertyNifIsString, input);
  }

  public String createDocUri(final String baseuri, final int start, final int end) {
    return baseuri.concat("#char").concat(String.valueOf(start)).concat(",")
        .concat(String.valueOf(end));
  }

  public String getDefaultDocumentURI() {
    return "http://ns.aksw.org/fox/".concat("demo/").concat("document-")
        .concat(String.valueOf(document));
  }

  private String getToolname(final Set<? extends IData> data) {
    String toolname = "";
    for (final IData e : data) {
      if (toolname.isEmpty()) {
        toolname = e.getToolName();
      } else if (!toolname.equals(e.getToolName())) {
        LOG.warn("All entities are expected to be created from one tool.");
        LOG.warn("but we found: " + toolname + " and " + e.getToolName());
      }
    }
    return toolname;
  }
}
