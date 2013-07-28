package org.aksw.fox.uri;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AGDISTISLookup implements InterfaceURI {

    private final String parameter = "text=";
    private final int indexOffset = parameter.length();

    // index mapper
    // maps AGDISTIS index to real index
    Map<Integer, Entity> indexMap = new HashMap<>();

    public static final String endpoint = "http://139.18.2.164:8080/AGDISTIS/";

    public static Logger logger = Logger.getLogger(AGDISTISLookup.class);

    @Override
    public void setUris(Set<Entity> entities, String input) {

        if (logger.isDebugEnabled())
            logger.debug("makeInput ...");

        String agdistis_input = makeInput(entities, input);

        if (logger.isDebugEnabled())
            logger.debug(indexMap);

        if (logger.isDebugEnabled())
            logger.debug("send ...");

        String agdistis_output = "";
        try {
            agdistis_output = send(agdistis_input);
            agdistis_input = null;
        } catch (Exception e) {
            logger.error("\n", e);
        }
        if (logger.isDebugEnabled())
            logger.debug(agdistis_output);

        if (logger.isDebugEnabled())
            logger.debug("addURItoEntities ...");

        addURItoEntities(agdistis_output, entities);

        if (logger.isDebugEnabled())
            logger.debug("done.");

        indexMap.clear();
    }

    private String makeInput(Set<Entity> entities, String input) {

        Map<Integer, Entity> indexEntityMap = new HashMap<>();
        for (Entity entity : entities) {
            for (Integer startIndex : entity.getIndices()) {
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
            int fakeindex = agdistis_input.length() + "<entity>".length();

            agdistis_input += "<entity>" + entity.getText() + "</entity>";

            last = index + entity.getText().length();
            indexMap.put(fakeindex + indexOffset, entity);
        }
        agdistis_input += input.substring(last);

        return agdistis_input;
    }

    private String send(String agdistis_input) throws Exception {

        String data = parameter + agdistis_input;

        URL url = new URL(endpoint);

        HttpURLConnection http = (HttpURLConnection) url.openConnection();

        http.setRequestMethod("POST");
        http.setDoInput(true);
        http.setDoOutput(true);
        http.setUseCaches(false);
        http.setRequestProperty("Content-Length", String.valueOf(data.length()));

        OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
        writer.write(data);
        writer.flush();

        return IOUtils.toString(http.getInputStream(), "UTF-8");
    }

    private void addURItoEntities(String json, Set<Entity> entities) {
        if (logger.isDebugEnabled())
            logger.debug("addURItoEntities ...");

        if (json != null && json.length() > 0) {

            JSONArray array = (JSONArray) JSONValue.parse(json);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {

                    Integer start = ((Long) ((JSONObject) array.get(i)).get("start")).intValue();
                    String disambiguatedURL = (String) ((JSONObject) array.get(i)).get("disambiguatedURL");

                    if (start != null && start > -1) {
                        Entity entity = indexMap.get(start);

                        if (disambiguatedURL == null) {
                            // TODO?
                            try {
                                entity.uri = "http://scms.eu/" + URLEncoder.encode(entity.getText(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                entity.uri = "http://scms.eu/" + entity.getText();
                                logger.error(entity.uri + "\n", e);
                            }
                        } else {

                            if (entity.uri != null && !entity.uri.isEmpty()) {

                                if (entity.uri.equals(urlencode(disambiguatedURL))) {
                                    logger.debug("we have this uri.");
                                } else {
                                    // TODO
                                    // do we really reach this line?
                                    // make new entity with the current index
                                    // and uri
                                    logger.error("disambiguation faild: " + entity.uri + " : " + urlencode(disambiguatedURL));
                                }
                            } else {
                                entity.uri = urlencode(disambiguatedURL);
                            }
                        }
                    }
                }
            }
        }
    }

    private String urlencode(String disambiguatedURL) {

        String encode = "";
        try {
            encode = URLEncoder.encode(disambiguatedURL.substring(disambiguatedURL.lastIndexOf('/') + 1, disambiguatedURL.length()), "UTF-8");
        } catch (Exception e) {
            encode = disambiguatedURL;

            logger.error(encode + "\n", e);
        }
        return "http://dbpedia.org/resource/" + encode;
    }

    // public static void main(String[] a) {
    //
    // AGDISTISLookup aa = new AGDISTISLookup();
    // String in = null;
    // try {
    // in =
    // aa.send("<entity>Barack Obama</entity>  meets <entity>Angela Merkel</entity>  in <entity>Berlin</entity>  to discuss a <entity>new world order</entity>");
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // if (in != null) {
    // JSONArray array = (JSONArray) JSONValue.parse(in);
    // for (int i = 0; i < array.size(); i++) {
    // System.out.println(((JSONObject) array.get(i)).get("namedEntity"));
    // System.out.println(((JSONObject) array.get(i)).get("disambiguatedURL"));
    // System.out.println(((JSONObject) array.get(i)).get("start"));
    // }
    // }
    // }
}