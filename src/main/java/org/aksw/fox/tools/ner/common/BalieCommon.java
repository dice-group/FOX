package org.aksw.fox.tools.ner.common;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.tools.ner.AbstractNER;

import ca.uottawa.balie.Balie;
import ca.uottawa.balie.DisambiguationRulesNerf;
import ca.uottawa.balie.LexiconOnDisk;
import ca.uottawa.balie.NamedEntityRecognitionNerfEXT;
import ca.uottawa.balie.NamedEntityTypeEnumMappingNerf;
import ca.uottawa.balie.PriorCorrectionNerf;
import ca.uottawa.balie.Token;
import ca.uottawa.balie.TokenList;
import ca.uottawa.balie.Tokenizer;

public abstract class BalieCommon extends AbstractNER {
  // http://github.com/smtlaissezfaire/balie

  protected String lang = null;;

  /**
   * English version.
   */
  public BalieCommon() {
    this(Balie.LANGUAGE_ENGLISH);
  }

  public BalieCommon(final String lang) {
    this.lang = lang;

    entityClasses.put(NamedEntityTypeEnumMappingNerf.organization.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.location.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.person.name(), EntityTypes.P);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.airport.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.celebrity.name(), EntityTypes.P);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.character.name(), EntityTypes.P);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.continent.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.company.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.conference.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.country.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.county.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.company_designator.name(), EntityTypes.P);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.castle.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.cathedral.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.city.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.food_brand.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.first_name.name(), EntityTypes.P);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.facility.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.geological.name(), EntityTypes.L);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.geo_political.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.hospital.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.hotel.name(), EntityTypes.O);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.last_name.name(), EntityTypes.P);
    entityClasses.put(NamedEntityTypeEnumMappingNerf.NOTHING.name(), BILOUEncoding.O);
  }

  public void setLang(final String lang) {
    this.lang = lang;
  }

  @Override
  public List<Entity> retrieve(final String input) {

    final Tokenizer tokenizer = new Tokenizer(lang, true);
    tokenizer.Reset(); // ?
    tokenizer.Tokenize(input);
    final PriorCorrectionNerf pcn = new PriorCorrectionNerf();
    final NamedEntityRecognitionNerfEXT ner = new NamedEntityRecognitionNerfEXT(
        tokenizer.GetTokenList(), new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON),
        DisambiguationRulesNerf.Load(), pcn, NamedEntityTypeEnumMappingNerf.values(),
        LOG.isTraceEnabled());

    ner.RecognizeEntities();
    final TokenList tokenList = ner.GetTokenList();

    final List<Entity> list = new ArrayList<>();
    String lastType = null;
    for (int i = 0; i < tokenList.Size(); i++) {

      final float re = Entity.DEFAULT_RELEVANCE;
      // if ((FoxCfg.get("balieDefaultRelevance") != null)
      // && !Boolean.valueOf(FoxCfg.get("balieDefaultRelevance"))) {
      // final Double like = ner.map.get(i);
      // if (like != null) {
      // re = like.floatValue();
      // }
      // }
      final Token token = tokenList.Get(i);
      String type = token.EntityType().GetLabel(NamedEntityTypeEnumMappingNerf.values());
      if (type != null && !type.equals("nothing")) {
        type = type.toLowerCase();
        LOG.debug(token + ":" + type);
      }

      if (type != null && mapTypeToSupportedType(type) != BILOUEncoding.O && type.equals(lastType)
          && list.size() > 0) {

        list.get(list.size() - 1).addText(token.Raw());

      } else {
        if (mapTypeToSupportedType(type) != BILOUEncoding.O) {
          list.add(new Entity(token.Raw(), mapTypeToSupportedType(type), re, getToolName(),
              token.StartPos()));
        }
        lastType = type;
      }
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace(list);
    }
    return list;
  }
}
