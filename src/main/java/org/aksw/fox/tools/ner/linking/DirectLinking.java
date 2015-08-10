package org.aksw.fox.tools.ner.linking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.apache.log4j.Logger;

import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

/**
 * 
 * @author ngonga, rspeck
 * 
 */
public class DirectLinking extends AbstractLinking {

    public static double    SIM_THRESHOLD             = 0.9;

    public static String    dbpedia_resource          = "http://dbpedia.org/resource/";
    public static String    dbpedia_sparql            = "http://live.dbpedia.org/sparql";

    protected static String dbpedia_aksw_solr         = "http://dbpedia.aksw.org:8080/solr/dbpedia_resources/select/?q=";
    protected static String dbpedia_aksw_solr_postUrl = "&version=2.2&indent=on&start=0&sort=score+desc,pagerank+desc&rows=";

    public static Logger    logger                    = Logger.getLogger(DirectLinking.class);

    @Override
    public void setUris(Set<Entity> entities, String input) {
        for (Entity e : entities) {
            e.uri = lookup(e.getText(), e.getType(), "");
        }
        this.entities = entities;
    }

    /**
     * Implements the lookup
     * 
     * @param label
     *            Label of the entity
     * @param type
     *            Entity type
     * @param inputText
     *            Context information
     * @return A uri
     */
    protected String lookup(String label, String type, String inputText) {

        label = label.replaceAll("\\s+", " ").trim();

        // ping endpoints
        Socket socket = null;
        boolean reachable = false;
        try {
            try {
                socket = new Socket(InetAddress.getByName("dbpedia.aksw.org"), 8080);
                reachable = true;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
        }

        ArrayList<String> possibleLabels = possibleLabels(label);

        if (reachable)
            for (String possibleLabel : possibleLabels)
                if (checkUri(dbpedia_resource + possibleLabel, type))
                    return dbpedia_resource + possibleLabel;

        // 2. if that does not work, check whether it is some redirect uri by
        // looking in up in dbpedia
        logger.info("2. Checking for redirected URIs");
        for (String possibleLabel : possibleLabels) {
            String uri = getUriFromEndpoint(possibleLabel);
            if (uri != null)
                return uri;

        }

        // 3. if that does not work, look for label in surface form table
        if (reachable) {
            // 4.if that does not work lookup label
            String indexResult = getUriFromIndex(label, type, inputText);
            if (indexResult != null)
                return indexResult;

        }
        return "http://scms.eu/" + label.replaceAll(" ", "_");
    }

    /**
     * Simply checks if a uri is in the index
     * 
     * @param uri
     *            Uri to check
     * @param type
     *            Type information
     * @return false if uri not in the index, else true
     */
    private boolean checkUri(String uri, String type) {

        type = geURItoType(type);
        if (logger.isDebugEnabled())
            logger.debug("Looking for <" + uri + ", " + type + ">");

        uri = uri.replaceAll(" ", "_");

        try {
            URL url = new URL(dbpedia_aksw_solr + "uri:\"" + uri + "\"" + dbpedia_aksw_solr_postUrl + "1");
            if (logger.isDebugEnabled())
                logger.debug("Sending query " + url.getQuery());
            URLConnection conn = url.openConnection();
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String uriTag = "<str name=\"uri\">";
            String line;
            while ((line = rd.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(uriTag)) {
                    String result = line.substring(uriTag.length(), line.indexOf("</str>"));
                    return result.equals(uri);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String geURItoType(String type) {
        type = type.toLowerCase();

        if (type.startsWith("per"))
            return "http://dbpedia.org/ontology/Person";

        if (type.startsWith("loc"))
            return "http://dbpedia.org/ontology/Place";

        if (type.startsWith("org"))
            return "http://dbpedia.org/ontology/Organisation";

        return "http://dbpedia.org";
    }

    protected HashMap<String, Double> getUri(String label, String type, int numberOfEntries) {

        HashMap<String, Double> result = queryIndex(label, type, numberOfEntries);

        if (result.size() == 0) {
            logger.info("Nothing found in DBpedia. Generating URI.");

            String uri = "http://scms.eu/" + label.replaceAll(" ", "_");
            if (uri != null)
                result.put(uri, new Double(1.0));
        }
        return result;
    }

    /**
     * Queries a label index
     * 
     * @param label
     *            NE label
     * @param type
     *            Classification (PER, LOC, ORG, MICS)
     * @param numberOfEntries
     * @return Best entity that follow the score restrictions
     */
    protected HashMap<String, Double> queryIndex(String label, String type, int numberOfEntries) {

        type = geURItoType(type);

        if (logger.isDebugEnabled())
            logger.debug("Looking for <" + label + ", " + type + ">");
        HashMap<String, Double> result = new HashMap<String, Double>();
        if (label.length() == 0 || label == null) {
            return result;
        } else {
            label = label.replaceAll(" ", "+");
        }
        try {
            URL url = new URL(dbpedia_aksw_solr + "label:" + label + dbpedia_aksw_solr_postUrl + numberOfEntries);
            logger.info("Sending query " + url.getQuery());

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));

            String line = null, uri = null, uriTag = "<str name=\"uri\">", scoreTag = "<int name=\"pagerank\">", docTag = "<doc>";

            double score = 0;
            boolean typeCheck = false;
            while ((line = in.readLine()) != null) {
                // if (logger.isDebugEnabled())
                // logger.debug(line);
                line = line.trim();
                if (line.startsWith(uriTag)) {
                    uri = line.substring(16, line.indexOf("</str>"));
                } else if (line.startsWith(scoreTag)) {
                    score = (Double.parseDouble(line.substring(scoreTag.length(), line.indexOf("</int>"))));
                } else if (line.contains("http://dbpedia.org/ontology/")) {
                    if (logger.isDebugEnabled())
                        logger.debug("Type line " + line);
                    if (line.contains(type)) {
                        if (logger.isDebugEnabled())
                            logger.debug("Found " + type + " in " + line);
                        typeCheck = true;
                    }
                } else if (line.contains(docTag)) {
                    if (uri != null && typeCheck) {
                        if (logger.isDebugEnabled())
                            logger.debug("->Putting " + uri + " -> " + score);
                        result.put(uri, score);
                        if (logger.isDebugEnabled())
                            logger.debug("=>" + result);
                        break;
                    }
                    uri = null;
                    score = 0;
                    typeCheck = false;
                }
            }
            result.put(uri, score);
            in.close();
        } catch (Exception e) {
            logger.error("ERROR: " + e.getMessage());
        }
        logger.info("Result = " + result);
        return result;
    }

    /**
     * Generates different possible looks for an URI from a label.
     * 
     * @param label
     *            Input
     * @return List of uris
     */
    protected ArrayList<String> possibleLabels(String label) {

        ArrayList<String> result = new ArrayList<String>();
        String upperCaseLabel = label.substring(0, 1).toUpperCase() + label.substring(1);
        upperCaseLabel = upperCaseLabel.replaceAll(" ", "_");
        result.add(upperCaseLabel);

        if (label.contains(" ")) {
            String label2 = "";
            String split[] = label.split(" ");

            for (int i = 0; i < split.length - 1; i++)
                label2 = label2 + split[i].substring(0, 1).toUpperCase() + split[i].substring(1) + "_";
            result.add(label2 + split[split.length - 1].substring(0, 1).toUpperCase() + split[split.length - 1].substring(1));
        }

        if (label.toLowerCase().startsWith("the ") || label.toLowerCase().startsWith("a "))
            for (String uri : possibleLabels(label.substring(label.indexOf(" ") + 1)))
                result.add(uri);

        if (logger.isDebugEnabled())
            logger.debug("List of normalized uris = " + result);

        return result;
    }

    private String getUriFromIndex(String label, String type, String inputText) {
        QGramsDistance q = new QGramsDistance();

        HashMap<String, Double> map = getUri(label, type, 10);
        if (map.size() == 0) {
            return null;
        }
        String result = null;
        double max = SIM_THRESHOLD, sim = 0;
        try {
            for (String fetchedUri : map.keySet()) {
                label = label.replaceAll(" ", "_").toLowerCase();
                sim = q.getSimilarity(fetchedUri.toLowerCase().substring(fetchedUri.lastIndexOf("/")), type);
                if (sim > max) {
                    result = fetchedUri;
                    max = sim;
                }
            }
        } catch (Exception e) {
            logger.error("Java Exception: " + e.getMessage());
        }
        return result;
    }

    /**
     * Gets the basis URI for a given label
     * 
     * @param label
     *            String label, e.g., EU
     * @return URI, e.g., http://dbpedia.org/resource/European_Union or null
     */
    private String getUriFromEndpoint(String label) {
        String query = "SELECT ?s where {<" + dbpedia_resource + label + "> <http://dbpedia.org/ontology/wikiPageRedirects> ?s}";
        if (logger.isDebugEnabled())
            logger.debug(query);
        try {

            ResultSet results = QueryExecutionFactory.sparqlService(dbpedia_sparql, QueryFactory.create(query)).execSelect();

            if (results.hasNext())
                return results.next().get("s").toString();

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug("Did not work!");
        }
        if (logger.isDebugEnabled())
            logger.debug("sparqlService finished");
        return null;
    }

}
