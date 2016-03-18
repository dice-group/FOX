package org.aksw.fox.tools.ner.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    // http://www.alchemyapi.com/api/entity/textc.html
    JSONObject o = null;
    try {
      final String response = Requests.postForm(url,
          Form.form()//
              .add("apikey", api_key)//
              .add("text", input) //
              .add("outputMode", outputMode)//
              .add("maxRetrieve", max) //
              .add("coreference", "0")//
              .add("linkedData", "0"), //
          ContentType.APPLICATION_JSON);
      o = new JSONObject(response);
    } catch (JSONException | IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return alchemyNERResponseParser(o);
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
