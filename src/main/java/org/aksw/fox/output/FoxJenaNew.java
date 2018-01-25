package org.aksw.fox.output;

import java.net.URI;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.utils.DataTestFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxJenaNew extends AFoxJenaNew implements IFoxJena {

  Resource inputResource = null;
  String baseuri = null;
  static String defaultDocumentURIbase = "http://ns.aksw.org/fox/";

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

    foxJena.addInput(input, "");
    foxJena.addEntities(entities, start, end, "na", "na");
    foxJena.addRelations(relations, start, end, "na", "na");

    LOG.info("Jena model: \n\n" + foxJena.print());
  }

  /**
   * Adds an agent and returns it's uri.
   *
   * @param name
   * @param verion
   * @return it's uri
   */
  protected String addSoftwareAgent(final String toolName, final String version) {
    return graph.createResource(ns_fox_resource.concat(toolName))//
        .addProperty(RDF.type, pProvSoftwareAgent)//
        .addProperty(RDF.type, pSchemaSoftwareAgent)//
        .addLiteral(pSchemaSoftwareVersion, graph.createTypedLiteral(version, XSD.xstring.getURI()))//
        .addLiteral(pFoafName, graph.createTypedLiteral(toolName, XSD.xstring.getURI())).getURI();
  }

  /**
   * Adds an activity.
   *
   * @param uris URIs the activity generated
   * @param start time the activity started
   * @param end time the activity ended
   * @param propertyActivity the Property for the activity
   * @param toolUri URI the activity used
   */
  protected void addActivity(final Set<String> uris, final String start, final String end,
      final Property propertyActivity, final String toolUri) {

    final Resource resource = graph.createResource()//
        .addProperty(RDF.type, pProvActivity)//
        .addProperty(RDF.type, propertyActivity)//
        .addLiteral(pProvStartedAtTime, graph.createTypedLiteral(start))//
        .addLiteral(pProvEndedAtTime, graph.createTypedLiteral(end))//
        .addProperty(pProvUsed, graph.getResource(toolUri));

    uris.forEach(uri -> {
      resource.addProperty(pProvGenerated, graph.getResource(uri));
    });
  }

  /**
   * Entities from an activity. All entities are expected to be created from one tool.
   *
   */
  @Override
  public void addEntities(final Set<Entity> entities, final String start, final String end,
      final String toolName, final String version) {

    if ((entities != null) && !entities.isEmpty()) {
      final Set<String> uris = _addEntities(entities);
      final String toolUri = addSoftwareAgent(toolName, version);
      addActivity(uris, start, end, pActivityNER, toolUri);
    }
  }

  /**
   *
   */
  @Override
  public void addRelations(final Set<Relation> relations, final String start, final String end,
      final String toolName, final String version) {

    if ((relations != null) && !relations.isEmpty()) {
      final Set<String> uris = _addRelations(relations);
      final String toolUri = addSoftwareAgent(toolName, version);
      addActivity(uris, start, end, pActivityRE, toolUri);
    }
  }

  private Set<String> _addEntities(final Set<Entity> entities) {

    final Set<String> uris = new HashSet<>();

    for (final Entity entity : entities) {

      if (entity.getText().trim().isEmpty()) {
        // TODO: why the light version has empty once here?
        continue;
      }

      if (!urlValidator.isValid(entity.uri)) {
        LOG.error("URI isn't valid: " + entity.uri);
      } else {

        for (final Integer index : entity.getIndices()) {

          final String docuri = createDocUri(baseuri, index, index + entity.getText().length());
          final Resource resource = graph.createResource(docuri)//
              .addProperty(RDF.type, pNifPhrase)//
              .addLiteral(pNifBegin, //
                  graph.createTypedLiteral(new Integer(index), XSD.nonNegativeInteger.getURI()))//
              .addLiteral(pNifEnd, //
                  graph.createTypedLiteral(new Integer(index + entity.getText().length()),
                      XSD.nonNegativeInteger.getURI()))//
              .addProperty(pItsrdfTaIdentRef, //
                  graph.createResource(entity.uri))//
              .addProperty(pItsrdfTaClassRef, //
                  graph.createResource(ns_fox_ontology + entity.getType()))//
              .addLiteral(pNifAnchorOf, //
                  graph.createTypedLiteral(new String(entity.getText()), XSD.xstring.getURI()))//
              .addProperty(pNifreferenceContext, inputResource);

          for (final String s : typesmap.get(entity.getType())) {
            resource.addProperty(pItsrdfTaClassRef, graph.createResource(s));
          }
          uris.add(resource.getURI());
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

      final ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, pNifPhrase);
      while (iterEntities.hasNext()) {
        final Resource resource = iterEntities.nextResource();

        for (final Statement indicies : resource.listProperties(pNifBegin).toSet()) {
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

        String uri = rse.getProperty(pItsrdfTaIdentRef).getResource().getLocalName();
        final String time = String.valueOf(
            DatatypeConverter.parseDate(DatatypeConverter.printDateTime(new GregorianCalendar()))
                .getTime().getTime());
        uri = ns_fox_resource.concat(uri).concat("_").concat(time);

        final Resource resource = graph.createResource(uri)//
            .addProperty(RDF.type, pRelationRelation)//
            .addProperty(pRelationDomain, rse.getProperty(pItsrdfTaIdentRef).getResource())//
            .addProperty(pRelationRange, roe.getPropertyResourceValue(pItsrdfTaIdentRef));

        for (final URI i : relation.getRelation()) {
          resource.addProperty(pRelationrelation, graph.createProperty(i.toString()));
        }
        uris.add(uri);

      } else {
        nofound.add(relation);
        LOG.debug("relation not found: " + relation);
      }
    }
    relations.removeAll(nofound);
    return uris;
  }

  /**
   * Creates document uri {@link FoxJenaNew#createDocUri(String, int, int)} for the input and adds a
   * resource with the uri and input to the graph.
   */
  @Override
  public void addInput(final String input, final String uri) {
    documentCounter++;
    baseuri = uri;

    if (baseuri == null) {
      baseuri = getDefaultDocumentURI();
    }

    final String currentURI = createDocUri(baseuri, 0, (input.length()));

    inputResource =
        graph.createResource(currentURI)//
            .addProperty(RDF.type, pNifContext)//
            .addLiteral(pNifBegin,
                graph.createTypedLiteral(new Integer(0), XSD.nonNegativeInteger.getURI()))//
            .addLiteral(pNifEnd,
                graph.createTypedLiteral(new Integer(input.length()),
                    XSD.nonNegativeInteger.getURI()))//
            .addLiteral(pNifIsString, input);
  }

  public String createDocUri(final String baseuri, final int start, final int end) {
    return baseuri.concat("#char").concat(String.valueOf(start)).concat(",")
        .concat(String.valueOf(end));
  }

  public String getDefaultDocumentURI() {
    return defaultDocumentURIbase.concat("demo/").concat("document-")
        .concat(String.valueOf(documentCounter));
  }
}
