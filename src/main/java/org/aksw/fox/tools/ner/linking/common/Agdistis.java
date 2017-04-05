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
import org.aksw.fox.utils.FoxJena;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Agdistis extends AbstractLinking {

  public static final Logger LOG = LogManager.getLogger(Agdistis.class);
  public static final String CFG_KEY_AGDISTIS_ENDPOINT = "agdistis.endpoint";

  // maps AGDISTIS index to real index
  protected Map<Integer, Entity> indexMap = new HashMap<>();
  protected String endpoint;

  public Agdistis(final XMLConfiguration cfg) {
    endpoint = cfg.getString(CFG_KEY_AGDISTIS_ENDPOINT);
  }

  @Override
  public void setUris(final Set<Entity> entities, final String input) {
    LOG.info("AGDISTISLookup ...");
    if (LOG.isDebugEnabled()) {
      LOG.debug("makeInput ...");
    }

    String agdistis_input = makeInput(entities, input);

    if (LOG.isDebugEnabled()) {
      LOG.debug(indexMap);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("send ...");
    }
    LOG.info("AGDISTISLookup sending...");
    String agdistis_output = "";
    try {
      agdistis_output = send(agdistis_input);
      agdistis_input = null;
    } catch (final Exception e) {
      LOG.error("\n", e);
    }
    LOG.info("AGDISTISLookup sending done.");
    if (LOG.isDebugEnabled()) {
      LOG.debug(agdistis_output);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("addURItoEntities ...");
    }

    addURItoEntities(agdistis_output, entities);

    if (LOG.isDebugEnabled()) {
      LOG.debug("done.");
    }

    LOG.info("AGDISTISLookup done..");
    indexMap.clear();

    this.entities = entities;
  }

  private String makeInput(final Set<Entity> entities, final String input) {

    final Map<Integer, Entity> indexEntityMap = new HashMap<>();
    for (final Entity entity : entities) {

      final Set<Integer> startIndices = entity.getIndices();
      if (startIndices == null) {
        throw new NullPointerException("Entity without indexices.");
      } else {
        for (final Integer startIndex : startIndices) {
          // TODO : check contains
          indexEntityMap.put(startIndex, entity);
        }
      }
    }

    final Set<Integer> startIndices = new TreeSet<>(indexEntityMap.keySet());
    String agdistis_input = "";
    int last = 0;
    for (final Integer index : startIndices) {
      final Entity entity = indexEntityMap.get(index);

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

  private String send(final String agdistis_input) throws Exception {

    // String data = parameter + agdistis_input;
    final String urlParameters =
        "text=" + URLEncoder.encode(agdistis_input, "UTF-8") + "&type=agdistis&heuristic=false";
    final URL url = new URL(endpoint);

    final HttpURLConnection http = (HttpURLConnection) url.openConnection();

    http.setRequestMethod("POST");
    http.setDoInput(true);
    http.setDoOutput(true);
    http.setUseCaches(false);
    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    http.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
    // http.setRequestProperty("Content-Length",
    // String.valueOf(data.length()));

    final OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
    writer.write(urlParameters);
    writer.flush();

    return IOUtils.toString(http.getInputStream(), "UTF-8");
  }

  private void addURItoEntities(final String json, final Set<Entity> entities) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("addURItoEntities ...");
    }

    if ((json != null) && (json.length() > 0)) {

      final JSONArray array = (JSONArray) JSONValue.parse(json);
      if (array != null) {
        for (int i = 0; i < array.size(); i++) {

          final Integer start = ((Long) ((JSONObject) array.get(i)).get("start")).intValue();
          final String disambiguatedURL =
              (String) ((JSONObject) array.get(i)).get("disambiguatedURL");

          if ((start != null) && (start > -1)) {
            final Entity entity = indexMap.get(start);

            if (disambiguatedURL == null) {
              URI uri;
              try {

                uri = new URI(FoxJena.nsScmsResource + entity.getText().replaceAll(" ", "_"));
                entity.uri = uri.toASCIIString(); // TODO: why?
              } catch (final URISyntaxException e) {
                entity.uri = FoxJena.nsScmsResource + entity.getText();
                LOG.error(entity.uri + "\n", e);
              }

            } else {
              // TODO: ?
              entity.uri = urlencode(disambiguatedURL);
            }
          }
        }
      }
    }
  }

  private String urlencode(final String disambiguatedURL) {
    try {
      final String encode = URLEncoder.encode(disambiguatedURL
          .substring(disambiguatedURL.lastIndexOf('/') + 1, disambiguatedURL.length()), "UTF-8");
      return disambiguatedURL.substring(0, disambiguatedURL.lastIndexOf('/') + 1).concat(encode);

    } catch (final Exception e) {
      LOG.error(disambiguatedURL + "\n", e);
      return disambiguatedURL;
    }
  }
}
