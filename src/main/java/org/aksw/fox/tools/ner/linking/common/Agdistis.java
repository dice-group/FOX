package org.aksw.fox.tools.ner.linking.common;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.agdistis.datatypes.Document;
import org.aksw.agdistis.datatypes.DocumentText;
import org.aksw.agdistis.datatypes.NamedEntitiesInText;
import org.aksw.agdistis.datatypes.NamedEntityInText;
import org.aksw.fox.data.Entity;
import org.aksw.fox.output.AFoxJenaNew;
import org.aksw.fox.tools.ner.linking.AbstractLinking;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Agdistis extends AbstractLinking {

  public static final String CFG_KEY_AGDISTIS_ENDPOINT = "agdistis.endpoint";

  // maps AGDISTIS index to real index
  protected Map<Integer, Entity> indexMap = new HashMap<>();
  protected String endpoint;

  public Agdistis() {}

  public Agdistis(final XMLConfiguration cfg) {
    endpoint = cfg.getString(CFG_KEY_AGDISTIS_ENDPOINT);
  }

  @Override
  public void setUris(final Set<Entity> entities, final String input) {
    LOG.info("AGDISTISLookup ...");

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
    entities.forEach(entity -> entity.getIndices().forEach(i -> indexEntityMap.put(i, entity)));

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

  protected String send(final String agdistis_input) throws Exception {

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

  protected void addURItoEntities(final String json, final Set<Entity> entities) {
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

                uri = new URI(AFoxJenaNew.ns_fox_resource + entity.getText().replaceAll(" ", "_"));
                entity.uri = uri.toASCIIString(); // TODO: why?
              } catch (final URISyntaxException e) {
                entity.uri = AFoxJenaNew.ns_fox_resource + entity.getText();
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

  protected String urlencode(final String disambiguatedURL) {
    try {
      final String encode = URLEncoder.encode(disambiguatedURL
          .substring(disambiguatedURL.lastIndexOf('/') + 1, disambiguatedURL.length()), "UTF-8");
      return disambiguatedURL.substring(0, disambiguatedURL.lastIndexOf('/') + 1).concat(encode);

    } catch (final Exception e) {
      LOG.error(disambiguatedURL + "\n", e);
      return disambiguatedURL;
    }
  }

  // new
  public String standardAG(final String text, final NEDAlgo_HITS agdistis) {
    final JSONArray arr = new JSONArray();

    final Document d = textToDocument(text);
    agdistis.run(d, null);

    for (final NamedEntityInText namedEntity : d.getNamedEntitiesInText()) {
      if (!namedEntity.getNamedEntityUri().contains("http")) {
        namedEntity.setNamedEntity(AFoxJenaNew.akswNotInWiki + namedEntity.getSingleWordLabel());
      }
      final JSONObject obj = new JSONObject();
      obj.put("namedEntity", namedEntity.getLabel());
      obj.put("start", namedEntity.getStartPos());
      obj.put("offset", namedEntity.getLength());
      obj.put("disambiguatedURL", namedEntity.getNamedEntityUri());
      arr.add(obj);
    }
    return arr.toString();

  }

  public Document textToDocument(final String preAnnotatedText) {
    final Document document = new Document();
    final ArrayList<NamedEntityInText> list = new ArrayList<NamedEntityInText>();
    int startpos = 0, endpos = 0;
    final StringBuilder sb = new StringBuilder();
    startpos = preAnnotatedText.indexOf("<entity>", startpos);
    while (startpos >= 0) {
      sb.append(preAnnotatedText.substring(endpos, startpos));
      startpos += 8;
      endpos = preAnnotatedText.indexOf("</entity>", startpos);
      final int newStartPos = sb.length();
      final String entityLabel = preAnnotatedText.substring(startpos, endpos);
      list.add(new NamedEntityInText(newStartPos, entityLabel.length(), entityLabel, ""));
      sb.append(entityLabel);
      endpos += 9;
      startpos = preAnnotatedText.indexOf("<entity>", startpos);
    }

    final NamedEntitiesInText nes = new NamedEntitiesInText(list);
    final DocumentText text =
        new DocumentText(preAnnotatedText.replaceAll("<entity>", "").replaceAll("</entity>", ""));

    document.addText(text);
    document.addNamedEntitiesInText(nes);
    return document;
  }
}
