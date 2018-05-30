package org.aksw.fox.output;

import java.net.URI;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.data.Voc;
import org.aksw.fox.utils.DataTestFactory;
import org.apache.jena.rdf.model.Model;
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

  public FoxJenaNew() {
    super();
  }

  public FoxJenaNew(final Model graph) {
    super(graph);
  }

  /**
   * Adds an agent and returns it's uri.
   *
   * @param name
   * @param verion
   * @return it's uri
   */
  protected String addSoftwareAgent(final String toolName, final String version) {
    return graph.createResource(Voc.ns_fox_resource.concat(toolName))//
        .addProperty(RDF.type, Voc.pProvSoftwareAgent)//
        .addProperty(RDF.type, Voc.pSchemaSoftwareAgent)//
        .addLiteral(Voc.pSchemaSoftwareVersion,
            graph.createTypedLiteral(version, XSD.xstring.getURI()))//
        .addLiteral(Voc.pFoafName, graph.createTypedLiteral(toolName, XSD.xstring.getURI()))
        .getURI();
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

    final Resource resource =
        graph.createResource()//
            .addProperty(RDF.type, Voc.pProvActivity)//
            .addProperty(RDF.type, propertyActivity)//
            .addLiteral(Voc.pProvStartedAtTime,
                graph.createTypedLiteral(start, XSD.dateTime.getURI()))//
            .addLiteral(Voc.pProvEndedAtTime, graph.createTypedLiteral(end, XSD.dateTime.getURI()))//
            .addProperty(Voc.pProvUsed, graph.getResource(toolUri));

    uris.forEach(uri -> {
      resource.addProperty(Voc.pProvGenerated, graph.getResource(uri));
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
      addActivity(uris, start, end, Voc.pFoxNER, toolUri);
    }
  }

  /**
   *
   */
  @Override
  public void addRelations(final Set<Relation> relations, final String start, final String end,
      final String toolName, final String version) {

    if ((relations != null) && !relations.isEmpty()) {
      final Set<String> uris = _addRelations(relations, graph);
      final String toolUri = addSoftwareAgent(toolName, version);
      addActivity(uris, start, end, Voc.pFoxRE, toolUri);
    } else {
      LOG.warn("No relations");
    }
  }

  private Set<String> _addEntities(final Set<Entity> entities) {

    final Set<String> uris = new HashSet<>();

    for (final Entity entity : entities) {

      if (entity.getText().trim().isEmpty()) {
        // TODO: why the light version has empty once here?
        continue;
      }

      if (!urlValidator.isValid(entity.getUri())) {
        LOG.error("URI isn't valid: " + entity.getUri());
      } else {

        for (final Integer index : entity.getIndices()) {

          final String docuri = createDocUri(baseuri, index, index + entity.getText().length());
          final Resource resource = graph.createResource(docuri)//
              .addProperty(RDF.type, Voc.pNifPhrase)//
              .addLiteral(Voc.pNifBegin, //
                  graph.createTypedLiteral(new Integer(index), XSD.nonNegativeInteger.getURI()))//
              .addLiteral(Voc.pNifEnd, //
                  graph.createTypedLiteral(new Integer(index + entity.getText().length()),
                      XSD.nonNegativeInteger.getURI()))//
              .addProperty(Voc.pItsrdfTaIdentRef, //
                  graph.createResource(entity.getUri()))//
              .addProperty(Voc.pItsrdfTaClassRef, //
                  graph.createResource(Voc.ns_fox_ontology + entity.getType()))//
              .addLiteral(Voc.pNifAnchorOf, //
                  graph.createTypedLiteral(new String(entity.getText()), XSD.xstring.getURI()))//
              .addProperty(Voc.pNifreferenceContext, inputResource);

          for (final String s : typesmap.get(entity.getType())) {
            resource.addProperty(Voc.pItsrdfTaClassRef, graph.createResource(s));
          }
          uris.add(resource.getURI());
        }
      }
    }
    return uris;
  }

  /**
   * Expects NE resources used in relations been already added to the graph.
   *
   * @param relations
   * @return
   */
  public Set<String> _addRelations(final Set<Relation> relations, final Model graph) {
    final Set<String> uris = new HashSet<>();

    final Set<Relation> nofound = new HashSet<>();

    for (final Relation relation : relations) {
      // find NE resources in graph
      final Entity oe = relation.getObjectEntity();
      final Entity se = relation.getSubjectEntity();

      Resource roe = null;
      Resource rse = null;

      String docUri = "";

      final ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, Voc.pNifPhrase);
      while (iterEntities.hasNext()) {
        final Resource entity = iterEntities.nextResource();

        for (final Statement indicies : entity.listProperties(Voc.pNifBegin).toSet()) {
          final int index = indicies.getInt();
          if (oe.getIndices().contains(index)) {
            roe = entity;
          } else if (se.getIndices().contains(index)) {
            rse = entity;
          }

          if ((roe != null) && (rse != null)) {
            break;
          }
        }
        if ((roe != null) && (rse != null)) {
          String rseDocUri = "";
          docUri = roe.getProperty(Voc.pNifreferenceContext).getResource().getURI().toString();
          rseDocUri = rse.getProperty(Voc.pNifreferenceContext).getResource().getURI().toString();

          if (!rseDocUri.isEmpty() && docUri.equals(rseDocUri)) {

          } else {
            roe = null;
            rse = null;
            LOG.info("should never be the case");
          }
          break;
        }
      }

      // in case NE resources are in the graph add relation to model
      if ((roe != null) && (rse != null)) {
        // TODO: find a better solution
        // number of milliseconds since January 1, 1970
        try {
          Thread.sleep(2000);
        } catch (final InterruptedException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
        final String time = getTime();

        // String uri = rse.getProperty(pItsrdfTaIdentRef).getResource().getLocalName();
        // uri = ns_fox_resource.concat(uri).concat("_").concat(time);
        final String uri = Voc.ns_fox_resource.concat(time);

        final Resource hasTarget = graph.createResource()//
            .addProperty(RDF.type, Voc.pSpecificResource)//
            .addProperty(Voc.pHasSource, graph.createResource(docUri))//
        ;

        final Resource resource = graph.createResource(uri)//
            .addProperty(RDF.type, Voc.pFoxRelation)//
            .addProperty(RDF.type, RDF.Statement)//
            .addProperty(RDF.type, Voc.pAnnotation)//
            .addProperty(Voc.pRdfSubject, rse.getProperty(Voc.pItsrdfTaIdentRef).getResource())//
            .addProperty(Voc.pRdfObject, roe.getPropertyResourceValue(Voc.pItsrdfTaIdentRef))//
            .addProperty(Voc.pHasTarget, hasTarget)//
        ;

        for (final URI i : relation.getRelation()) {
          resource.addProperty(Voc.pRdfPredicate, graph.createProperty(i.toString()));
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

  // number of milliseconds since January 1, 1970
  private String getTime() {
    return String.valueOf(//
        DatatypeConverter//
            .parseDate(DatatypeConverter.printDateTime(new GregorianCalendar()))//
            .getTime().getTime());
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
            .addProperty(RDF.type, Voc.pNifContext)//
            .addProperty(RDF.type, Voc.pNifCString)//
            .addLiteral(Voc.pNifBegin,
                graph.createTypedLiteral(new Integer(0), XSD.nonNegativeInteger.getURI()))//
            .addLiteral(Voc.pNifEnd,
                graph.createTypedLiteral(new Integer(input.length()),
                    XSD.nonNegativeInteger.getURI()))//
            .addLiteral(Voc.pNifIsString, input);
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
