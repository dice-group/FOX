package org.aksw.fox.output;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
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
   * @return
   */
  protected String addSoftwareAgent(final String toolName, final String version) {

    LOG.info("Adds SoftwareAgent");

    final Resource resource = graph.createResource(ns_fox_resource.concat(toolName))//
        .addProperty(RDF.type, pProvSoftwareAgent)//
        .addProperty(RDF.type, pSchemaSoftwareAgent)//
        .addLiteral(pSchemaSoftwareVersion, graph.createTypedLiteral(version, XSD.xstring.getURI()))//
        .addLiteral(pFoafName, graph.createTypedLiteral(toolName, XSD.xstring.getURI()));

    return resource.getURI();
  }

  protected void addActivity(final Set<String> uris, final String start, final String end,
      final Property propertyActivity, final String toolUri) {

    LOG.info("Add activity ");

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

    LOG.info("Add entities.");

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

        final Date date = DatatypeConverter
            .parseDate(DatatypeConverter.printDateTime(new GregorianCalendar())).getTime();
        final long time = (date.getTime());

        String urir = rse.getProperty(pItsrdfTaIdentRef).getResource().getURI().concat("_")
            .concat(String.valueOf(time));

        urir =
            ns_fox_resource.concat(rse.getProperty(pItsrdfTaIdentRef).getResource().getLocalName());
        urir = urir.concat("_").concat(String.valueOf(time));

        final Resource resource = graph.createResource(urir);
        uris.add(urir);

        resource.addProperty(RDF.type, pRelationRelation);

        resource.addProperty(pRelationDomain, rse.getProperty(pItsrdfTaIdentRef).getResource());

        resource.addProperty(pRelationRange, roe.getPropertyResourceValue(pItsrdfTaIdentRef));
        for (final URI uri : relation.getRelation()) {
          resource.addProperty(pRelationrelation, graph.createProperty(uri.toString()));
        }
        /**
         * <code>
         final Resource resRel =
             graph.createResource(rse.getProperty(pItsrdfTaIdentRef).getObject().toString());

         for (final URI uri : relation.getRelation()) {
           resRel.addProperty(graph.createProperty(uri.toString()),
               roe.getPropertyResourceValue(pItsrdfTaIdentRef));
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
  public void addInput(final String input, final String uri) {
    documentCounter++;
    baseuri = uri;

    if (baseuri == null) {
      baseuri = getDefaultDocumentURI();
    }

    final String currecntUri = createDocUri(baseuri, 0, (input.length()));

    inputResource =
        graph.createResource(currecntUri)//
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
