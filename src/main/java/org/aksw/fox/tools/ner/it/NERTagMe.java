package org.aksw.fox.tools.ner.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class NERTagMe extends AbstractNER {

    public static final String  CFG_KEY_TAGME_KEY       = NERTagMe.class.getName().concat(".key");
    public static final String  CFG_KEY_SPARQL_ENDPOINT = NERTagMe.class.getName().concat(".sparqlEndpoint");
    public static final String  CFG_KEY_RDFS_PREFIX     = NERTagMe.class.getName().concat(".rdfsPrefix");
    public static final String  CFG_KEY_RDF_PREFIX      = NERTagMe.class.getName().concat(".rdfPrefix");
    public static final String  CFG_KEY_MIN_WEIGHT      = NERTagMe.class.getName().concat(".minWeight");

    private static final String TAGME_KEY               = FoxCfg.get(CFG_KEY_TAGME_KEY);
    private static final String SPARQL_ENDPOINT         = FoxCfg.get(CFG_KEY_SPARQL_ENDPOINT);
    private static final String RDFS_PREFIX             = FoxCfg.get(CFG_KEY_RDFS_PREFIX);
    private static final String RDF_PREFIX              = FoxCfg.get(CFG_KEY_RDF_PREFIX);
    private static final double MIN_WEIGHT              = Double.valueOf(FoxCfg.get(CFG_KEY_MIN_WEIGHT));

    public List<Entity> retrieve(String input)
    {
        List<Entity> entity_list = new ArrayList<Entity>();

        LOG.info("breaking input into single sentences");

        BreakIterator sentenceIterator =
                BreakIterator.getSentenceInstance(new Locale("it"));

        sentenceIterator.setText(input);
        int start = sentenceIterator.first();
        int end = sentenceIterator.next();

        LOG.info("retrieving ...");

        while (end != BreakIterator.DONE) {
            String sentence = input.substring(start, end);
            if (Character.isLetterOrDigit(sentence.charAt(0))) {
                entity_list.addAll(this.retrieveSentence(sentence));
            }
            start = end;
            end = sentenceIterator.next();
        }

        return entity_list;
    }

    public List<Entity> retrieveSentence(String input)
    {
        ArrayList<Entity> retval = new ArrayList<Entity>();

        /*
         * sending query to TagMe online service 
         */

        String request = null;

        try {
            request = "http://tagme.di.unipi.it/tag?"
                    + "key=" + TAGME_KEY + "&"
                    + "text=" + URLEncoder.encode(input, "UTF-8") + "&"
                    + "lang=it";
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }

        URL url = null;
        try {
            url = new URL(request);
        } catch (MalformedURLException e) {
            LOG.error(e);
        }

        StringBuffer stringBuffer = new StringBuffer();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;)
            {
                stringBuffer.append(line);
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        }

        String answer = stringBuffer.toString();

        /*
         * filtering tokens and labels from TagMe answer
         */

        Pattern label_pattern = Pattern.compile("\"title\":\"(.*?)\",\"start\"");
        Pattern token_pattern = Pattern.compile("\"spot\":\"(.*?)\"}");
        Pattern weight_pattern = Pattern.compile("\"rho\":\"(.*?)\",\"end\"");
        Matcher label_matcher = label_pattern.matcher(answer);
        Matcher token_matcher = token_pattern.matcher(answer);
        Matcher weight_matcher = weight_pattern.matcher(answer);
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<String> tokens = new ArrayList<String>();
        ArrayList<String> weights = new ArrayList<String>();

        while (label_matcher.find())
        {
            labels.add(label_matcher.group(1));
        }

        while (token_matcher.find())
        {
            tokens.add(token_matcher.group(1));
        }

        while (weight_matcher.find())
        {
            weights.add(weight_matcher.group(1));
        }

        /*
         * sending SPARQL queries to determine label types
         */

        Iterator<String> it_label = labels.iterator();
        Iterator<String> it_token = tokens.iterator();
        Iterator<String> it_weight = weights.iterator();
        Pattern p_loc = Pattern.compile(".*(/[Ll][Oo][Cc][Aa][Tt][Ii][Oo][Nn]).*");
        Pattern p_per = Pattern.compile(".*(/[Pp][Ee][Rr][Ss][Oo][Nn]).*");
        Pattern p_org = Pattern.compile(".*(/[Oo][Rr][Gg][Aa][Nn][Ii][SsZz][Aa][Tt][Ii][Oo][Nn]).*");

        String current_type = null, current_results = null;
        Integer nTypes = 0;

        while (it_label.hasNext() && it_token.hasNext() && it_weight.hasNext())
        {
            if (Double.parseDouble(it_weight.next()) >= MIN_WEIGHT)
            {
                String current_label = it_label.next();
                String current_token = it_token.next();

                String q = "PREFIX  rdfs: <" + RDFS_PREFIX + ">\n"
                        + "PREFIX rdf: 	<" + RDF_PREFIX + ">\n"
                        + "SELECT ?y WHERE {"
                        + "?x rdfs:label \"" + current_label + "\"@it."
                        + "?x rdf:type ?y."
                        + "}";

                try
                {
                    Query query = QueryFactory.create(q);
                    QueryExecution qExe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
                    ResultSet results = qExe.execSelect();
                    current_results = ResultSetFormatter.asText(results);
                    qExe.close();
                } catch (Exception e) {
                    LOG.info(e + " while sending query for " + current_label);
                    current_results = "";
                }

                Matcher m_loc = p_loc.matcher(current_results);
                Matcher m_per = p_per.matcher(current_results);
                Matcher m_org = p_org.matcher(current_results);

                if (m_loc.find())
                {
                    current_type = "LOCATION";
                    nTypes++;
                }

                if (m_per.find())
                {
                    current_type = "PERSON";
                    nTypes++;
                }

                if (m_org.find())
                {
                    current_type = "ORGANIZATION";
                    nTypes++;
                }

                if (nTypes == 1)
                {
                    retval.add(new Entity(current_token, current_type, 1, "NERTagMe"));
                }

                nTypes = 0;
            }
        }

        return retval;
    }

    public static void main(String[] a)
    {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        try
        {
            for (Entity e : new NERTagMe().retrieve(FoxConst.NER_IT_EXAMPLE_1))
                LOG.info(e);
        } catch (NullPointerException e) {
            LOG.info("no entities found");
        }
    }
}
