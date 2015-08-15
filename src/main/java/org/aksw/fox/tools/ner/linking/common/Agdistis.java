package org.aksw.fox.tools.ner.linking.common;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.linking.AbstractLinking;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Agdistis extends AbstractLinking {

    public static final Logger     LOG                       = LogManager.getLogger(Agdistis.class);
    public static final String     CFG_KEY_AGDISTIS_ENDPOINT = "agdistis.endpoint";

    // maps AGDISTIS index to real index
    protected Map<Integer, Entity> indexMap                  = new HashMap<>();
    protected String               endpoint;

    @SuppressWarnings("unused")
    private Agdistis() {
    }

    public Agdistis(XMLConfiguration cfg) {
        endpoint = cfg.getString(CFG_KEY_AGDISTIS_ENDPOINT);
    }

    @Override
    public void setUris(Set<Entity> entities, String input) {
        LOG.info("AGDISTISLookup ...");
        if (LOG.isDebugEnabled())
            LOG.debug("makeInput ...");

        String agdistis_input = makeInput(entities, input);

        if (LOG.isDebugEnabled())
            LOG.debug(indexMap);

        if (LOG.isDebugEnabled())
            LOG.debug("send ...");
        LOG.info("AGDISTISLookup sending...");
        String agdistis_output = "";
        try {
            agdistis_output = send(agdistis_input);
            agdistis_input = null;
        } catch (Exception e) {
            LOG.error("\n", e);
        }
        LOG.info("AGDISTISLookup sending done.");
        if (LOG.isDebugEnabled())
            LOG.debug(agdistis_output);

        if (LOG.isDebugEnabled())
            LOG.debug("addURItoEntities ...");

        addURItoEntities(agdistis_output, entities);

        if (LOG.isDebugEnabled())
            LOG.debug("done.");

        LOG.info("AGDISTISLookup done..");
        indexMap.clear();

        this.entities = entities;
    }

    private String makeInput(Set<Entity> entities, String input) {

        Map<Integer, Entity> indexEntityMap = new HashMap<>();
        for (Entity entity : entities) {

            Set<Integer> startIndices = entity.getIndices();
            if (startIndices == null) {
                throw new NullPointerException("Entity without indexices.");
            } else
                for (Integer startIndex : startIndices) {
                    // TODO : check contains
                    indexEntityMap.put(startIndex, entity);
                }
        }

        Set<Integer> startIndices = new TreeSet<>(indexEntityMap.keySet());
        String agdistis_input = "";
        int last = 0;
        for (Integer index : startIndices) {
            Entity entity = indexEntityMap.get(index);

            agdistis_input += input.substring(last, index);
            // int fakeindex = agdistis_input.length() + "<entity>".length();

            agdistis_input += "<entity>" + entity.getText() + "</entity>";

            last = index + entity.getText().length();
            // indexMap.put(fakeindex + indexOffset, entity);
            indexMap.put(index, entity);
        }
        agdistis_input += input.substring(last);

        return agdistis_input;
    }

    private String send(String agdistis_input) throws Exception {

        // String data = parameter + agdistis_input;
        String urlParameters = "text=" + URLEncoder.encode(agdistis_input, "UTF-8") + "&type=agdistis&heuristic=false";
        URL url = new URL(endpoint);

        HttpURLConnection http = (HttpURLConnection) url.openConnection();

        http.setRequestMethod("POST");
        http.setDoInput(true);
        http.setDoOutput(true);
        http.setUseCaches(false);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        http.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
        // http.setRequestProperty("Content-Length",
        // String.valueOf(data.length()));

        OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
        writer.write(urlParameters);
        writer.flush();

        return IOUtils.toString(http.getInputStream(), "UTF-8");
    }

    private void addURItoEntities(String json, Set<Entity> entities) {
        if (LOG.isDebugEnabled())
            LOG.debug("addURItoEntities ...");

        if (json != null && json.length() > 0) {

            JSONArray array = (JSONArray) JSONValue.parse(json);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {

                    Integer start = ((Long) ((JSONObject) array.get(i)).get("start")).intValue();
                    String disambiguatedURL = (String) ((JSONObject) array.get(i)).get("disambiguatedURL");

                    if (start != null && start > -1) {
                        Entity entity = indexMap.get(start);

                        if (disambiguatedURL == null) {
                            URI uri;
                            try {
                                uri = new URI(
                                        "http",
                                        "scms.eu",
                                        "/" + entity.getText().replaceAll(" ", "_"),
                                        null);
                                entity.uri = uri.toASCIIString();
                            } catch (URISyntaxException e) {
                                entity.uri = "http://scms.eu/" + entity.getText();
                                LOG.error(entity.uri + "\n", e);
                            }

                        } else {
                            entity.uri = urlencode(disambiguatedURL);
                        }
                    }
                }
            }
        }
    }

    private String urlencode(String disambiguatedURL) {
        try {
            String encode = URLEncoder.encode(
                    disambiguatedURL.substring(disambiguatedURL.lastIndexOf('/') + 1, disambiguatedURL.length())
                    , "UTF-8");
            return disambiguatedURL.substring(0, disambiguatedURL.lastIndexOf('/') + 1).concat(encode);

        } catch (Exception e) {
            LOG.error(disambiguatedURL + "\n", e);
            return disambiguatedURL;
        }
    }
}