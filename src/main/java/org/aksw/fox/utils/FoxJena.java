package org.aksw.fox.utils;

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

// for future: http://www.w3.org/ns/oa#
/**
 * 
 * 
 * @author rspeck
 *
 */
public class FoxJena {

    public static Logger             LOG               = LogManager.getLogger(FoxJena.class);

    public static final List<String> prints            = Arrays.asList(
                                                               Lang.RDFXML.getName(),
                                                               /* FileUtils.langXMLAbbrev,*/
                                                               Lang.TURTLE.getName(),
                                                               Lang.NTRIPLES.getName(),
                                                               // Lang.N3.getName(),
                                                               Lang.RDFJSON.getName(),
                                                               Lang.JSONLD.getName(),
                                                               Lang.TRIG.getName(),
                                                               Lang.NQUADS.getName()
                                                               );
    /*
    public static enum RelationEnum {
        employeeOf("employeeOf"), hasPosition("hasPosition"), hasDegree("hasDegree");

        public final String label;

        RelationEnum(String label) {
            this.label = label;
        }
    };
    */

    /* namespace */
    public static final String       nsDBpediaOwl      = "http://dbpedia.org/ontology/";
    public static final String       nsDBpedia         = "http://dbpedia.org/resource/";

    public static final String       nsAnn             = "http://www.w3.org/2000/10/annotation-ns#";
    public static final String       nsCTag            = "http://commontag.org/ns#";
    public static final String       nsScms            = "http://ns.aksw.org/scms/";
    public static final String       nsScmsann         = "http://ns.aksw.org/scms/annotations/";
    public static final String       nsScmsannStanford = "http://ns.aksw.org/scms/annotations/stanford/";

    public static final String       nsScmssource      = "http://ns.aksw.org/scms/tools/";

    /* properties */
    protected Property               annotation,
                                     beginIndex,
                                     endIndex,
                                     means,
                                     source,
                                     body,
                                     ctagLabel,
                                     ctagMeans,
                                     ctagAutoTag,
                                     relationProperty, relationTypeProperty,
                                     s,
                                     t;

    protected Property[]             scmsRelationLabels;

    /* model */
    protected Model                  graph             = null;

    protected UrlValidator           urlValidator      = new UrlValidator();

    public void initGraph() {

        graph = ModelFactory.createDefaultModel();

        // create namespace prefix
        // graph.setNsPrefix("dbpedia-owl", nsDBpediaOwl);
        graph.setNsPrefix("dbpedia", nsDBpedia);
        graph.setNsPrefix("ann", nsAnn);
        graph.setNsPrefix("scms", nsScms);
        // graph.setNsPrefix("rdf", RDF.getURI());
        // graph.setNsPrefix("ctag", nsCTag);
        graph.setNsPrefix("xsd", XSD.getURI());
        graph.setNsPrefix("scmsann", nsScmsann);
        graph.setNsPrefix("stanford", nsScmsannStanford);
        graph.setNsPrefix("source", nsScmssource);

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
        RelationEnum[] relationEnum = RelationEnum.values();
        scmsRelationLabels = new Property[relationEnum.length];
        for (int i = 0; i < scmsRelationLabels.length; i++)
            scmsRelationLabels[relationEnum[i].ordinal()] = graph.createProperty(nsScms + relationEnum[i].label);
        */
    }

    public void clearGraph() {
        initGraph();
    }

    public Model getGraph() {
        return graph;
    }

    /**
     * Adds entities to graph.
     */
    public void setAnnotations(Set<Entity> set) {

        if (graph == null)
            initGraph();

        if (set == null)
            return;

        for (Entity entity : set)
            if (!urlValidator.isValid(entity.uri))
                LOG.error("uri isn't valid: " + entity.uri);

            else {
                Resource resource = graph.createResource();

                resource.addProperty(RDF.type, annotation);
                resource.addProperty(RDF.type, graph.createProperty(nsScmsann + entity.getType()));

                for (Integer index : entity.getIndices()) {
                    resource.addLiteral(beginIndex, graph.createTypedLiteral(new Integer(index)));
                    resource.addLiteral(endIndex, graph.createTypedLiteral(new Integer(index + entity.getText().length())));
                }

                resource.addProperty(means, graph.createResource(entity.uri));
                resource.addProperty(source, graph.createResource(nsScmssource + entity.getTool()));
                resource.addLiteral(body, entity.getText());
            }
    }

    /*
    public void setRelations2(Set<Relation> relations) {
        if (graph == null)
            initGraph();

        if (relations == null || relations.isEmpty())
            return;

        for (Relation relation : relations) {
            Entity oe = relation.getObjectEntity();
            Entity se = relation.getSubjectEntity();

            Resource roe = null;
            Resource rse = null;

            ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, annotation);
            while (iterEntities.hasNext()) {
                Resource resource = iterEntities.nextResource();

                int index = resource.getProperty(beginIndex).getLiteral().getInt();

                if (oe.getIndices().contains(index)) {
                    roe = resource;
                } else if (se.getIndices().contains(index)) {
                    rse = resource;
                }
            }

            if (roe != null && rse != null) {

                Resource resRel = graph.createResource(rse.getProperty(means).getObject().toString());
                Property proBlank = graph.createProperty("");
                Resource blank = graph.createResource();
                resRel.addProperty(proBlank, blank);
                for (URI uri : relation.getRelation()) {
                    blank.addProperty(relationTypeProperty, graph.createResource(uri.toString()));
                }
                blank.addProperty(relationProperty, roe.getPropertyResourceValue(means));
            }
        }
    }
    */

    /**
     * Adds relations to graph.
     * 
     * @param relations
     */
    public void setRelations(Set<Relation> relations) {
        if (graph == null)
            initGraph();

        if (relations == null || relations.isEmpty())
            return;

        Set<Relation> nofound = new HashSet<>();
        for (Relation relation : relations) {
            Entity oe = relation.getObjectEntity();
            Entity se = relation.getSubjectEntity();

            Resource roe = null;
            Resource rse = null;

            ResIterator iterEntities = graph.listSubjectsWithProperty(RDF.type, annotation);
            while (iterEntities.hasNext()) {
                Resource resource = iterEntities.nextResource();

                for (Statement indicies : resource.listProperties(beginIndex).toSet()) {
                    int index = indicies.getInt();
                    if (oe.getIndices().contains(index)) {
                        roe = resource;
                    } else if (se.getIndices().contains(index)) {
                        rse = resource;
                    }
                }
            }

            if (roe != null && rse != null) {
                Resource resRel = graph.createResource(rse.getProperty(means).getObject().toString());
                for (URI uri : relation.getRelation()) {
                    resRel.addProperty(graph.createProperty(uri.toString()), roe.getPropertyResourceValue(means));
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

    /**
     * Prints the model in a given format.
     * 
     * @param kind
     *            output format
     * @param nif
     *            true to use nif
     * @param wholeText
     *            the used text
     * 
     * @return result in a string
     */
    public String print(String kind, boolean nif, String wholeText) {

        if (graph == null)
            return null;

        StringWriter sw = new StringWriter();
        if (FoxJena.prints.contains(kind)) {
            RDFDataMgr.write(sw, graph, RDFLanguages.nameToLang(kind));
        } else {
            try {
                graph.write(sw, kind);
            } catch (Exception e) {
                LOG.error("\n Output format " + kind + " is not supported.", e);
            }
        }
        return sw.toString();
    }

    public static void main(String args[]) {

        // test data
        DataTestFactory dtf = new DataTestFactory();
        Set<Entity> entities = new ArrayList<Set<Entity>>(dtf.getTestEntities().values()).get(0);
        Set<Relation> relations = dtf.getTestRelations().entrySet().iterator().next().getValue();

        String input = dtf.getTestEntities().entrySet().iterator().next().getKey();

        // test
        FoxJena fj = new FoxJena();

        fj.setAnnotations(entities);
        fj.setRelations(relations);

        String out = fj.print(FoxJena.prints.get(1), false, input);
        System.out.println(out);

    }
    /*
    public static void main(String args[]) {
        // create an empty graph
        Model graph = ModelFactory.createDefaultModel();

        // create the resource
        Resource resource = graph.createResource();

        // add the property
        resource
                .addProperty(RDFS.label, graph.createLiteral("chat", "en"))
                .addProperty(RDFS.label, graph.createLiteral("chat", "fr"))
                .addProperty(RDFS.label, graph.createLiteral("<em>chat</em>", true));

        // write out the graph
        graph.write(new PrintWriter(System.out));
        System.out.println();

        // create an empty graph
        graph = ModelFactory.createDefaultModel();

        // create the resource
        resource = graph.createResource();

        // add the property
        resource
                .addProperty(RDFS.label, "11")
                .addLiteral(RDFS.label, 11);

        // write out the graph
        graph.write(System.out, FoxJena.prints.get(3));
    }
    */
}
