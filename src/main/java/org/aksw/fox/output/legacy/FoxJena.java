package org.aksw.fox.output.legacy;

import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// for future: http://www.w3.org/ns/oa#
/**
 *
 *
 * @author rspeck
 *
 */
@Deprecated
public class FoxJena {

  public static Logger LOG = LogManager.getLogger(FoxJena.class);

  /*
   * public static enum RelationEnum { employeeOf("employeeOf"), hasPosition("hasPosition"),
   * hasDegree("hasDegree");
   *
   * public final String label;
   *
   * RelationEnum(String label) { this.label = label; } };
   */

  /* namespace */
  public static final String nsDBpediaOwl = "http://dbpedia.org/ontology/";
  public static final String nsDBpedia = "http://dbpedia.org/resource/";

  public static final String nsAnn = "http://www.w3.org/2000/10/annotation-ns#";

  public static final String nsCTag = "http://commontag.org/ns#";

  private static final String aksw = "http://ns.aksw.org/";
  public static final String nsScms = aksw + "scms/";
  public static final String nsScmsResource = nsScms + "resource/";
  public static final String nsScmssource = nsScms + "tools/";
  public static final String nsScmsann = nsScms + "annotations/";
  public static final String nsScmsannStanford = nsScmsann + "stanford/";

  /* properties */
  protected Property annotation, beginIndex, endIndex, means, source, body, ctagLabel, ctagMeans,
      ctagAutoTag, relationProperty, relationTypeProperty, s, t;

  protected Property[] scmsRelationLabels;

  /* model */
  protected Model graph = null;

  protected UrlValidator urlValidator = new UrlValidator();

  public void initGraph() {

    graph = ModelFactory.createDefaultModel();

    // create namespace prefix
    graph.setNsPrefix("dbo", nsDBpediaOwl);
    graph.setNsPrefix("dbr", nsDBpedia);
    graph.setNsPrefix("ann", nsAnn);
    graph.setNsPrefix("scms", nsScms);
    // graph.setNsPrefix("rdf", RDF.getURI());
    // graph.setNsPrefix("ctag", nsCTag);
    graph.setNsPrefix("xsd", XSD.getURI());
    graph.setNsPrefix("scmsann", nsScmsann);
    graph.setNsPrefix("stanford", nsScmsannStanford);
    graph.setNsPrefix("source", nsScmssource);
    graph.setNsPrefix("scmsres", nsScmsResource);

    // NER properties
    annotation = graph.createProperty(nsAnn + "Annotation");
    beginIndex = graph.createProperty(nsScms + "beginIndex");
    endIndex = graph.createProperty(nsScms + "endIndex");
    means = graph.createProperty(nsScms + "means");
    source = graph.createProperty(nsScms + "source");
    body = graph.createProperty(nsAnn + "body");
    s = graph.createProperty(nsScms + "s");
    t = graph.createProperty(nsScms + "t");

    // KE properties
    ctagLabel = graph.createProperty(nsCTag + "label");
    ctagMeans = graph.createProperty(nsCTag + "means");
    ctagAutoTag = graph.createProperty(nsCTag + "AutoTag");

    // RE
    relationProperty = graph.createProperty(nsScms + "relation");
    relationTypeProperty = graph.createProperty(nsScms + "relationType");
    /*
     * RelationEnum[] relationEnum = RelationEnum.values(); scmsRelationLabels = new
     * Property[relationEnum.length]; for (int i = 0; i < scmsRelationLabels.length; i++)
     * scmsRelationLabels[relationEnum[i].ordinal()] = graph.createProperty(nsScms +
     * relationEnum[i].label);
     */
  }

  public void clearGraph() {
    initGraph();
  }

  public Model getGraph() {
    return graph;
  }

  /*
   * public void setAutoTag(Keyword keyword){ Resource resource =
   * model.createResource(keyword.uri.id); resource.addLiteral(ctagLabel, keyword.uri.label);
   * resource.addLiteral(ctagMeans, keyword.text); resource.addProperty(scmsTool,
   * model.createResource(ConfigurationUtil.TOOL_PREFIX + keyword.tool)); }
   */

  /**
   * Adds entities to graph.
   */
  public void setAnnotations(final Set<Entity> set) {

    if (graph == null) {
      initGraph();
    }

    if (set == null) {
      return;
    }

    for (final Entity entity : set) {
      if (!urlValidator.isValid(entity.getUri())) {
        LOG.error("uri isn't valid: " + entity.getUri());
      } else {
        final Resource resource = graph.createResource();

        resource.addProperty(RDF.type, annotation);
        resource.addProperty(RDF.type, graph.createProperty(nsScmsann + entity.getType()));

        for (final Integer index : entity.getIndices()) {
          resource.addLiteral(beginIndex, graph.createTypedLiteral(new Integer(index)));
          resource.addLiteral(endIndex,
              graph.createTypedLiteral(new Integer(index + entity.getText().length())));
        }

        resource.addProperty(means, graph.createResource(entity.getUri()));
        resource.addProperty(source, graph.createResource(nsScmssource + entity.getToolName()));
        resource.addLiteral(body, entity.getText());
      }
    }
  }

  /*
   * public void setRelations2(Set<Relation> relations) { if (graph == null) initGraph();
   *
   * if (relations == null || relations.isEmpty()) return;
   *
   * for (Relation relation : relations) { Entity oe = relation.getObjectEntity(); Entity se =
   * relation.getSubjectEntity();
   *
   * Resource roe = null; Resource rse = null;
   *
   * ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, annotation); while
   * (iterEntities.hasNext()) { Resource resource = iterEntities.nextResource();
   *
   * int index = resource.getProperty(beginIndex).getLiteral().getInt();
   *
   * if (oe.getIndices().contains(index)) { roe = resource; } else if
   * (se.getIndices().contains(index)) { rse = resource; } }
   *
   * if (roe != null && rse != null) {
   *
   * Resource resRel = graph.createResource(rse.getProperty(means).getObject().toString()); Property
   * proBlank = graph.createProperty(""); Resource blank = graph.createResource();
   * resRel.addProperty(proBlank, blank); for (URI uri : relation.getRelation()) {
   * blank.addProperty(relationTypeProperty, graph.createResource(uri.toString())); }
   * blank.addProperty(relationProperty, roe.getPropertyResourceValue(means)); } } }
   */

  /**
   * Adds relations to graph.
   *
   * @param relations
   */
  public void setRelations(final Set<Relation> relations) {
    if (graph == null) {
      initGraph();
    }

    if ((relations == null) || relations.isEmpty()) {
      return;
    }

    // we remove relations that are not in the text
    final Set<Relation> nofound = new HashSet<>();

    // for all relations of all used tools
    for (final Relation relation : relations) {
      final Entity oe = relation.getObjectEntity();
      final Entity se = relation.getSubjectEntity();

      Resource roe = null;
      Resource rse = null;

      final ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, annotation);
      while (iterEntities.hasNext()) {
        final Resource resource = iterEntities.nextResource();

        for (final Statement indicies : resource.listProperties(beginIndex).toSet()) {
          final int index = indicies.getInt();
          if (oe.getIndices().contains(index)) {
            roe = resource;
          } else if (se.getIndices().contains(index)) {
            rse = resource;
          }
        }
      }

      if ((roe != null) && (rse != null)) {
        final Resource resRel = graph.createResource(rse.getProperty(means).getObject().toString());
        for (final URI uri : relation.getRelation()) {
          resRel.addProperty(graph.createProperty(uri.toString()),
              roe.getPropertyResourceValue(means));
        }
      } else {
        nofound.add(relation);
        if (LOG.isDebugEnabled()) {
          LOG.debug("not found: ");
          LOG.debug(relation);
        }
      }
    }
    relations.removeAll(nofound);
  }

  // TODO: remove notused argument
  /**
   * Prints the model in a given format.
   *
   * @param kind output format
   * @param nif true to use nif
   * @param wholeText the used text
   *
   * @return result in a string
   */
  public String print(final String kind, final boolean nif, final String notused) {

    if (graph == null) {
      return null;
    }

    final StringWriter sw = new StringWriter();
    RDFDataMgr.write(sw, graph, RDFLanguages.nameToLang(kind));
    return sw.toString();
  }

  /**
   * <code>
   public static void main(final String args[]) {
  
     // test data
     final DataTestFactory dtf = new DataTestFactory();
     final Set<Entity> entities = new ArrayList<Set<Entity>>(dtf.getTestEntities().values()).get(0);
     final Set<Relation> relations = dtf.getTestRelations().entrySet().iterator().next().getValue();
  
     final String input = dtf.getTestEntities().entrySet().iterator().next().getKey();
  
     // test
     final FoxJena fj = new FoxJena();
  
     fj.setAnnotations(entities);
     fj.setRelations(relations);
  
     final String out = fj.print(FoxParameter.Output.TURTLE.name(), false, input);
     System.out.println(out);
  
   }
   </code>
   */

  /*
   * public static void main(String args[]) { // create an empty graph Model graph =
   * ModelFactory.createDefaultModel();
   *
   * // create the resource Resource resource = graph.createResource();
   *
   * // add the property resource .addProperty(RDFS.label, graph.createLiteral("chat", "en"))
   * .addProperty(RDFS.label, graph.createLiteral("chat", "fr")) .addProperty(RDFS.label,
   * graph.createLiteral("<em>chat</em>", true));
   *
   * // write out the graph graph.write(new PrintWriter(System.out)); System.out.println();
   *
   * // create an empty graph graph = ModelFactory.createDefaultModel();
   *
   * // create the resource resource = graph.createResource();
   *
   * // add the property resource .addProperty(RDFS.label, "11") .addLiteral(RDFS.label, 11);
   *
   * // write out the graph graph.write(System.out, FoxJena.prints.get(3)); }
   */
}
