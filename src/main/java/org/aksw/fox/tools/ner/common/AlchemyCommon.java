package org.aksw.fox.tools.ner.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.http.client.fluent.Form;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.renespeck.swissknife.http.Requests;

public abstract class AlchemyCommon extends AbstractNER {
  /**
   *
   * http://www.alchemyapi.com/api/entity/textc.html
   *
   * 1: HTTP POST calls should include the Content-Type header: application/x-www-form-urlencoded
   * <br>
   *
   * 2: Posted text documents can be a maximum of 50 kilobytes. Larger documents will result in a
   * "content-exceeds-size-limit" error response.<br>
   */
  public static final XMLConfiguration CFG = CfgManager.getCfg(AlchemyCommon.class);

  private final String api_key = CFG.getString("apikey");
  private final String url = CFG.getString("url");
  private final String max = CFG.getString("max");
  private final String outputMode = "json";

  /**
   * Sets types.
   */
  public AlchemyCommon() {
    setTypes();
  }

  @Override
  public List<Entity> retrieve(final String input) {
    return retrieveSentences(getSentences(Locale.ENGLISH, input));
  }

  protected List<Entity> retrieveSentences(final List<String> sentences) {

    // _sentences with string of 15k bytes max.
    final List<String> _sentences = new ArrayList<>();
    {
      int ii = 0;
      final Map<Integer, Integer> sentenceSize = new HashMap<>();
      for (final String sentence : sentences) {
        try {
          sentenceSize.put(ii++, sentence.getBytes("UTF-8").length);
        } catch (final UnsupportedEncodingException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
      StringBuilder sb = new StringBuilder();
      int sum = 0;
      for (final Entry<Integer, Integer> entry : sentenceSize.entrySet()) {
        sum += entry.getValue();
        if (sum < 15000) {
          sb.append(sentences.get(entry.getKey()));
        } else {
          _sentences.add(sb.toString());
          sb = new StringBuilder();
          sum = 0;
        }
      }
      _sentences.add(sb.toString());
    }

    final Set<Entity> set = new HashSet<>();
    for (final String sen : _sentences) {
      JSONObject o = null;
      try {
        final String response = Requests.postForm(url,
            Form.form()//
                .add("apikey", api_key)//
                .add("text", sen) //
                .add("outputMode", outputMode)//
                .add("maxRetrieve", max) //
                .add("coreference", "0")//
                .add("linkedData", "0"), //
            ContentType.APPLICATION_JSON);
        o = new JSONObject(response);
      } catch (JSONException | IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      if (o != null) {
        set.addAll(alchemyNERResponseParser(o));
      }
    }
    return new ArrayList<>(set);
  }

  protected List<Entity> alchemyNERResponseParser(final JSONObject o) {
    final List<Entity> list = new ArrayList<>();
    if ((o == null) || (!o.has("entities"))) {
      return list;
    } else {
      final JSONArray entities = o.getJSONArray("entities");
      for (int i = 0; i < entities.length(); i++) {
        final JSONObject entity = entities.getJSONObject(i);
        final String type = entityClasses.get(entity.getString("type"));
        if (type != null) {
          list.add(new Entity(entity.getString("text"), type));
        }
      }
    }
    return list;
  }

  protected void setTypes() {
    entityClasses.put("Organization", EntityClassMap.O);
    entityClasses.put("City", EntityClassMap.L);
    entityClasses.put("Company", EntityClassMap.O);
    entityClasses.put("Continent", EntityClassMap.L);
    entityClasses.put("Country", EntityClassMap.L);
    entityClasses.put("Facility", EntityClassMap.L);
    entityClasses.put("Person", EntityClassMap.P);
    entityClasses.put("StateOrCounty", EntityClassMap.L);
    entityClasses.put("Region", EntityClassMap.L);
    entityClasses.put("MusicGroup", EntityClassMap.O);
    entityClasses.put("GeographicFeature", EntityClassMap.L);

    /**
     * <code>
    Anatomy
    Anniversary
    Automobile
    City
    Company
    Continent
    Country
    Crime
    Degree
    Drug
    EntertainmentAward
    Facility
    FieldTerminology
    FinancialMarketIndex
    GeographicFeature
    HealthCondition
    Holiday
    JobTitle
    Movie
    MusicGroup
    NaturalDisaster
    OperatingSystem
    Organization
    Person
    PrintMedia
    Product
    ProfessionalDegree
    RadioProgram
    RadioStation
    Region
    Sport
    SportingEvent
    StateOrCounty
    Technology
    TelevisionShow
    TelevisionStation
    EmailAddress
    TwitterHandle
    Hashtag
    IPAddress
    Quantity
    Money
     </code>
     */
  };
}
