package org.aksw.fox.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class FoxJena {

    public static Logger logger = Logger.getLogger(FoxJena.class);

    public static final List<String> prints = new ArrayList<>();
    static {

        prints.add(FileUtils.langXML);
        prints.add(FileUtils.langXMLAbbrev);
        prints.add(FileUtils.langTurtle);
        prints.add(FileUtils.langNTriple);
        prints.add(FileUtils.langN3);
        prints.add(RDFFormat.RDFJSON.toString());
        // prints.add("JSONLD");
    }

    public static enum RelationEnum {
        employeeOf("employeeOf"), hasPosition("hasPosition"), hasDegree("hasDegree");

        public final String label;

        RelationEnum(String label) {
            this.label = label;
        }
    };

    /* namespace */
    public static final String nsAnn = "http://www.w3.org/2000/10/annotation-ns#";
    public static final String nsCTag = "http://commontag.org/ns#";
    public static final String nsScms = "http://ns.aksw.org/scms/";
    public static final String nsScmsann = "http://ns.aksw.org/scms/annotations/";
    public static final String nsScmssource = "http://ns.aksw.org/scms/tools/";

    /* properties */
    protected Property beginIndex, endIndex, means, source, body, ctagLabel, ctagMeans, ctagAutoTag, relation;
    protected Property[] scmsRelationLabels;

    /* model */
    protected Model graph = null;

    public void initGraph() {

        graph = ModelFactory.createDefaultModel();
        // create namespace prefix
        graph.setNsPrefix("ann", nsAnn);
        graph.setNsPrefix("scms", nsScms);
        graph.setNsPrefix("rdf", RDF.getURI());
        graph.setNsPrefix("ctag", nsCTag);
        graph.setNsPrefix("xsd", XSD.getURI());
        graph.setNsPrefix("scmsann", nsScmsann);

        // NER properties
        beginIndex = graph.createProperty(nsScms + "beginIndex");
        endIndex = graph.createProperty(nsScms + "endIndex");
        means = graph.createProperty(nsScms + "means");
        source = graph.createProperty(nsScms + "source");
        body = graph.createProperty(nsAnn + "body");

        // KE properties
        ctagLabel = graph.createProperty(nsCTag + "label");
        ctagMeans = graph.createProperty(nsCTag + "means");
        ctagAutoTag = graph.createProperty(nsCTag + "AutoTag");

        // RE
        relation = graph.createProperty(nsScms + "relation");

        RelationEnum[] relationEnum = RelationEnum.values();
        scmsRelationLabels = new Property[relationEnum.length];
        for (int i = 0; i < scmsRelationLabels.length; i++)
            scmsRelationLabels[relationEnum[i].ordinal()] = graph.createProperty(nsScms + relationEnum[i].label);
    }

    public void clearGraph() {
        initGraph();
    }

    public Model getGraph() {
        return graph;
    }

    /**
     * Prints the model in a given format.
     * 
     * @param kind
     *            it's {@link NLPBox#printRDF} or {@link NLPBox#printTURTLE}
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

        if (FoxJena.prints.contains(kind)) {

            StringWriter sw = new StringWriter();
            graph.write(sw, kind);
            return sw.toString();

        } else if (kind.equals("JSONLD")) {

            Object json = null;
            try {
                json = JSONLD.fromRDF(graph, new JenaRDFParser());
            } catch (JSONLDProcessingError e) {
                logger.error("\n", e);

                StringWriter sw = new StringWriter();
                graph.write(sw, kind);
                return sw.toString();
            }
            return JSONUtils.toPrettyString(json);
        } else {
            return null;
        }
    }

    /**
     * Adds entities to graph.
     */
    public void setAnnotations(Set<Entity> set) {

        if (graph == null)
            initGraph();

        if (set == null || set.isEmpty())
            return;

        UrlValidator urlValidator = new UrlValidator();
        for (Entity entity : set) {

            if (!urlValidator.isValid(entity.uri)) {
                logger.error("uri isn't valid: " + entity.uri);
            } else {

                Resource annotation = graph.createResource();

                annotation.addProperty(RDF.type, graph.createProperty(nsAnn + "Annotation"));
                annotation.addProperty(RDF.type, graph.createProperty(nsScmsann + entity.getType()));

                for (Integer index : entity.getIndices()) {
                    annotation.addLiteral(beginIndex, graph.createTypedLiteral(new Integer(index)));
                    annotation.addLiteral(endIndex, graph.createTypedLiteral(new Integer(index + entity.getText().length())));
                }

                annotation.addProperty(means, graph.createResource(entity.uri));
                annotation.addProperty(source, graph.createResource(nsScmssource + entity.getTool()));
                annotation.addLiteral(body, entity.getText());
            }
        }
    }

    public static void main(String args[]) {
        // create an empty graph
        Model graph = ModelFactory.createDefaultModel();

        // create the resource
        Resource r = graph.createResource();

        // add the property
        r.addProperty(RDFS.label, graph.createLiteral("chat", "en")).addProperty(RDFS.label, graph.createLiteral("chat", "fr")).addProperty(RDFS.label, graph.createLiteral("<em>chat</em>", true));

        // write out the graph
        graph.write(new PrintWriter(System.out));
        System.out.println();

        // create an empty graph
        graph = ModelFactory.createDefaultModel();

        // create the resource
        r = graph.createResource();

        // add the property
        r.addProperty(RDFS.label, "11").addLiteral(RDFS.label, 11);

        // write out the graph
        graph.write(System.out, "N-TRIPLE");
    }
}
