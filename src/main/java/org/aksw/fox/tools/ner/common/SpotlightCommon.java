package org.aksw.fox.tools.ner.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxConst;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.http.client.fluent.Form;
import org.apache.http.entity.ContentType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.renespeck.swissknife.http.Requests;

public abstract class SpotlightCommon extends AbstractNER {

  public static Logger LOG = LogManager.getLogger(SpotlightCommon.class);

  protected final XMLConfiguration CFG;
  protected String SPOTLIGHT_URL;
  protected String SPOTLIGHT_CONFIDENCE;
  protected String SPOTLIGHT_SUPPORT;
  protected String SPOTLIGHT_TYPES;
  protected String SPOTLIGHT_SPARQL;

  Locale lang;

  public SpotlightCommon(final Locale lang) {
    CFG = CfgManager.getCfg(this.getClass());

    SPOTLIGHT_URL = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_URL);
    SPOTLIGHT_CONFIDENCE = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_CONFIDENCE);
    SPOTLIGHT_SUPPORT = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_SUPPORT);
    SPOTLIGHT_TYPES = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_TYPES);
    SPOTLIGHT_SPARQL = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_SPARQL);

    this.lang = lang;
  }

  @Override
  public List<Entity> retrieve(String input) {

    final List<String> sentences = getSentences(lang, input);
    LOG.info("sentences: " + sentences.size());

    final int concatSentences = 10;
    int counter = 1;
    input = "";
    entityList = new ArrayList<>();
    for (final String sentence : sentences) {
      input += sentence;
      if ((counter % concatSentences) != 0) {
        counter++;
        if (!sentences.get(sentences.size() - 1).equals(sentence)) {
          continue;
        }
      }
      counter = 1;
      String spotlightResponse = null;
      if (input.trim().isEmpty()) {
        LOG.info("Empty input!");
      } else {
        try {
          spotlightResponse = Requests.postForm(//
              SPOTLIGHT_URL,
              Form.form()//
                  .add("confidence", SPOTLIGHT_CONFIDENCE)//
                  .add("support", SPOTLIGHT_SUPPORT)//
                  .add("types", SPOTLIGHT_TYPES)//
                  .add("sparql", SPOTLIGHT_SPARQL)//
                  .add("text", input)//
                  .add("types", "Person,Organisation,Location,Place"), //
              ContentType.APPLICATION_JSON);
          LOG.debug("spotlightResponse: " + spotlightResponse);
        } catch (final Exception e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
      JSONObject resultJSON = null;
      JSONArray entities = null;
      if (spotlightResponse != null) {
        try {
          resultJSON = new JSONObject(spotlightResponse);
          if (resultJSON.has("Resources")) {
            entities = resultJSON.getJSONArray("Resources");
          } else {
            LOG.debug("No Resources found in spotlight response.");
          }
        } catch (final JSONException e) {
          LOG.error("JSON exception, spotlight response.", e);
        }
      }

      if (entities != null) {
        for (int i = 0; i < entities.length(); i++) {
          try {
            final JSONObject entity = entities.getJSONObject(i);
            LOG.debug(entity.toString(2));
            final String type = spotlight(entity.getString("@types"));
            if (!type.equals(EntityClassMap.getNullCategory())) {
              entityList.add(getEntity(entity.getString("@surfaceForm"), type,
                  Entity.DEFAULT_RELEVANCE, getToolName()));
            }
          } catch (final JSONException e) {
            LOG.error("\nJSON exception ", e);
          }
        }
      }
      if (counter == 1) {
        input = "";
      }
    } // sentences
    if (LOG.isTraceEnabled()) {
      LOG.trace(entityList);
    }
    return entityList;
  }

  /**
   * Gets the entity class for a spotlight entity type/class.
   */
  protected String spotlight(final String spotlightTag) {
    String t = EntityClassMap.getNullCategory();
    if ((spotlightTag == null) || spotlightTag.trim().isEmpty()) {
      return t;
    }
    if (spotlightTag.toLowerCase().contains("person")) {
      t = EntityClassMap.P;
    } else if (spotlightTag.toLowerCase().contains("organisation")) {
      t = EntityClassMap.O;
    } else if (spotlightTag.toLowerCase().contains("place")) {
      t = EntityClassMap.L;
    } else if (spotlightTag.toLowerCase().contains("location")) {
      t = EntityClassMap.L;
    }
    return t;
  }
}
