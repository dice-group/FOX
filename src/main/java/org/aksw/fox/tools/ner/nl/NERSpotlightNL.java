package org.aksw.fox.tools.ner.nl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NERSpotlightNL extends AbstractNER {

    public static final String   CFG_KEY_SPOTLIGHTNL_ENDPOINT   = NERSpotlightNL.class.getName().concat(".endpoint");
    public static final String   CFG_KEY_SPOTLIGHTNL_CONFIDENCE = NERSpotlightNL.class.getName().concat(".confidence");
    public static final String   CFG_KEY_SPOTLIGHTNL_SUPPORT    = NERSpotlightNL.class.getName().concat(".support");

    private final static String  API_URL                      = FoxCfg.get(CFG_KEY_SPOTLIGHTNL_ENDPOINT);
    private final static Double  CONFIDENCE                   = Double.valueOf(FoxCfg.get(CFG_KEY_SPOTLIGHTNL_CONFIDENCE));
    private final static Integer SUPPORT                      = Integer.valueOf(FoxCfg.get(CFG_KEY_SPOTLIGHTNL_SUPPORT));
    private final static String  DISAMBIGUATOR                = "Default";
    private final static String  POLICY                       = "whitelist";

    @Override
    public List<Entity> retrieve(String input) {

        LOG.info("retrieve ...");

        String spotlightResponse = null;
        try {
            spotlightResponse = Request.Post(API_URL + "/rest/annotate")
                    .addHeader("Accept", "application/json")
                    .bodyForm(Form.form()
                            .add("text", input)
                            .add("confidence", CONFIDENCE.toString())
                            .add("disambiguator", DISAMBIGUATOR)
                            .add("support", SUPPORT.toString())
                            .add("policy", POLICY)
                            .build())
                    .execute()
                    .returnContent()
                    .asString();
            LOG.debug(spotlightResponse);
        } catch (Exception e) {
            LOG.error("\n", e);
        }

        JSONObject resultJSON = null;
        JSONArray entities = null;

        if (spotlightResponse != null)
            try {
                resultJSON = new JSONObject(spotlightResponse);
                if (resultJSON.has("Resources")) {
                    entities = resultJSON.getJSONArray("Resources");
                } else {
                    LOG.debug("No Resources found.");
                }
            } catch (JSONException e) {
                LOG.error("\nJSON exception ", e);
            }

        List<Entity> list = new ArrayList<>();
        if (entities != null) {
            for (int i = 0; i < entities.length(); i++) {
                try {
                    JSONObject entity = entities.getJSONObject(i);
                    String type = EntityClassMap.spotlight(entity.getString("@types"));

                    if (!type.equals(EntityClassMap.getNullCategory())) {
                        list.add(getEntity(
                                entity.getString("@surfaceForm"),
                                type,
                                Entity.DEFAULT_RELEVANCE,
                                getToolName()
                                ));
                    }
                } catch (JSONException e) {
                    LOG.error("\nJSON exception ", e);
                }
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(list);
        } 
        return list;
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERSpotlightNL().retrieve(FoxConst.NER_NL_EXAMPLE_2))
        {
            LOG.info(e);
        }
        System.out.println("Fertig");
    }
}
