package org.aksw.fox.tools.ner.it;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class TagMeIT extends AbstractNER {

    public static final XMLConfiguration CFG                     = CfgManager.getCfg(TagMeIT.class);
    public static final Charset          UTF_8                   = Charset.forName("UTF-8");

    public static final String           CFG_KEY_TAGME_KEY       = "tagMe.key";
    public static final String           CFG_KEY_SPARQL_ENDPOINT = "tagMe.sparqlEndpoint";
    public static final String           CFG_KEY_RDFS_PREFIX     = "tagMe.rdfsPrefix";
    public static final String           CFG_KEY_RDF_PREFIX      = "tagMe.rdfPrefix";
    public static final String           CFG_KEY_MIN_WEIGHT      = "tagMe.minWeight";
    public static final String           CFG_KEY_ENDPOINT        = "tagMe.url";
    public static final String           CFG_KEY_LANG            = "tagMe.lang";

    public static final String           TAGME_KEY               = CFG.getString(CFG_KEY_TAGME_KEY);
    public static final String           SPARQL_ENDPOINT         = CFG.getString(CFG_KEY_SPARQL_ENDPOINT);
    public static final String           RDFS_PREFIX             = CFG.getString(CFG_KEY_RDFS_PREFIX);
    public static final String           RDF_PREFIX              = CFG.getString(CFG_KEY_RDF_PREFIX);
    public static final double           MIN_WEIGHT              = CFG.getDouble(CFG_KEY_MIN_WEIGHT);
    public static final String           ENDPOINT                = CFG.getString(CFG_KEY_ENDPOINT);
    public static final String           LANG                    = CFG.getString(CFG_KEY_LANG);

    @Override
    public List<Entity> retrieve(String input) {
        entityList = new ArrayList<>();
        BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(new Locale(LANG));
        sentenceIterator.setText(input);

        int start = sentenceIterator.first();
        int end = sentenceIterator.next();
        while (end != BreakIterator.DONE) {
            String sentence = input.substring(start, end);
            LOG.info("sentence: " + sentence);
            List<Entity> list = retrieveSentence(sentence);
            if (list != null && !list.isEmpty())
                entityList.addAll(list);
            start = end;
            end = sentenceIterator.next();
        }
        return entityList;
    }

    public String sendToSTring(String input) throws ClientProtocolException, IOException {
        Response response = Request
                .Post(ENDPOINT)
                .addHeader("Accept", "application/json;charset=".concat(UTF_8.name()))
                .addHeader("Accept-Charset", UTF_8.name())
                .bodyForm(Form.form()
                        .add("key", TAGME_KEY)
                        .add("text", input)
                        .add("lang", LANG)
                        .add("include_categories", "true")
                        .build())
                .execute();

        HttpResponse httpResponse = response.returnResponse();
        HttpEntity entry = httpResponse.getEntity();

        String r = IOUtils.toString(entry.getContent(), UTF_8);
        EntityUtils.consume(entry);
        return r;
    }

    public List<Entity> retrieveSentence(String input) {

        List<Entity> retval = new ArrayList<Entity>();

        String answer = null;
        try {
            answer = sendToSTring(input);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (answer != null) {
            LOG.debug(answer);
            List<String> title = new ArrayList<>();
            List<String> spot = new ArrayList<>();
            List<String> rho = new ArrayList<>();
            List<List<String>> dpbedia = new ArrayList<>();

            JSONObject jo = new JSONObject(answer);
            if (jo.has("annotations")) {
                JSONArray annotations = jo.getJSONArray("annotations");
                for (int i = 0; i < annotations.length(); i++) {
                    title.add(annotations.getJSONObject(i).getString("title"));
                    spot.add(annotations.getJSONObject(i).getString("spot"));
                    rho.add(annotations.getJSONObject(i).getString("rho"));

                    JSONArray dbpedia_categories = annotations.getJSONObject(i).getJSONArray("dbpedia_categories");
                    List<String> l = new ArrayList<>();
                    for (int ii = 0; ii < dbpedia_categories.length(); ii++) {
                        l.add(dbpedia_categories.getString(ii));
                    }
                    dpbedia.add(l);
                }
            }

            Iterator<String> it_label = title.iterator();
            Iterator<String> it_token = spot.iterator();
            Iterator<String> it_weight = rho.iterator();
            Iterator<List<String>> it_dbpedia = dpbedia.iterator();

            Pattern p_loc = Pattern.compile(".*(/[Ll][Oo][Cc][Aa][Tt][Ii][Oo][Nn]).*");
            Pattern p_per = Pattern.compile(".*(/[Pp][Ee][Rr][Ss][Oo][Nn]).*");
            Pattern p_org = Pattern.compile(".*(/[Oo][Rr][Gg][Aa][Nn][Ii][SsZz][Aa][Tt][Ii][Oo][Nn]).*");

            String current_results = null;

            while (it_weight.hasNext()) {
                String current_token = it_token.next();
                List<String> dbp = it_dbpedia.next();
                dbp.add(0, it_label.next());

                Iterator<String> it_dbp = dbp.iterator();

                if (Double.parseDouble(it_weight.next()) >= MIN_WEIGHT) {
                    String type = null;
                    while (it_dbp.hasNext() && type == null) {
                        String current_label = it_dbp.next();
                        String q = "PREFIX  rdfs: <" + RDFS_PREFIX + "> "
                                + "PREFIX rdf: 	<" + RDF_PREFIX + "> "
                                + "SELECT ?y WHERE {"
                                + "?x rdfs:label \"" + current_label + "\"@" + LANG + "."
                                + "?x rdf:type ?y."
                                + "}";

                        try {
                            Query query = QueryFactory.create(q);
                            QueryExecution qExe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
                            ResultSet results = qExe.execSelect();
                            current_results = ResultSetFormatter.asText(results);
                            LOG.debug(current_results);
                            qExe.close();
                        } catch (Exception e) {
                            LOG.info(e + " while sending query for " + current_label);
                            current_results = "";
                        }

                        if (p_loc.matcher(current_results).find())
                            type = EntityClassMap.L;
                        if (p_per.matcher(current_results).find())
                            type = EntityClassMap.P;
                        if (p_org.matcher(current_results).find())
                            type = EntityClassMap.O;
                        if (type != null)
                            retval.add(new Entity(current_token, type, 1, getToolName()));
                    }
                    LOG.debug(retval);
                }
            }
        }
        return retval;
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new TagMeIT().retrieve(FoxConst.NER_IT_EXAMPLE_1));
    }
}
