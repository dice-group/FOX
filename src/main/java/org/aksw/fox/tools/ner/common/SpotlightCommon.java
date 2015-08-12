package org.aksw.fox.tools.ner.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.tools.ner.de.SpotlightDE;
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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpotlightCommon extends AbstractNER {

    public static Logger             LOG   = LogManager.getLogger(SpotlightCommon.class);
    public static final Charset      UTF_8 = Charset.forName("UTF-8");

    protected final XMLConfiguration CFG;
    protected String                 SPOTLIGHT_URL;
    protected String                 SPOTLIGHT_CONFIDENCE;
    protected String                 SPOTLIGHT_SUPPORT;
    protected String                 SPOTLIGHT_TYPES;
    protected String                 SPOTLIGHT_SPARQL;

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new SpotlightDE().retrieve(FoxConst.NER_GER_EXAMPLE_1));
    }

    public SpotlightCommon() {
        CFG = CfgManager.getCfg(this.getClass());

        SPOTLIGHT_URL = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_URL);
        SPOTLIGHT_CONFIDENCE = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_CONFIDENCE);
        SPOTLIGHT_SUPPORT = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_SUPPORT);
        SPOTLIGHT_TYPES = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_TYPES);
        SPOTLIGHT_SPARQL = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_SPARQL);
    }

    @Override
    public List<Entity> retrieve(String input) {
        List<Entity> list = new ArrayList<>();
        String spotlightResponse = null;
        try {
            spotlightResponse = sendToSTring(input);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
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

        if (entities != null) {
            for (int i = 0; i < entities.length(); i++) {
                try {
                    JSONObject entity = entities.getJSONObject(i);
                    String type = spotlight(entity.getString("@types"));

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
        if (LOG.isTraceEnabled())
            LOG.trace(list);
        return list;
    }

    public String sendToSTring(String input) throws ClientProtocolException, IOException {
        Response response = Request
                .Post(SPOTLIGHT_URL)
                .addHeader("Accept", "application/json;charset=".concat(UTF_8.name()))
                .addHeader("Accept-Charset", UTF_8.name())
                .bodyForm(Form.form()
                        .add("confidence", SPOTLIGHT_CONFIDENCE)
                        .add("support", SPOTLIGHT_SUPPORT)
                        .add("types", SPOTLIGHT_TYPES)
                        .add("sparql", SPOTLIGHT_SPARQL)
                        .add("text", input)
                        .build())
                .execute();

        HttpResponse httpResponse = response.returnResponse();
        HttpEntity entry = httpResponse.getEntity();

        String spotlightResponse = IOUtils.toString(entry.getContent(), UTF_8);

        EntityUtils.consume(entry);
        return spotlightResponse;
    }

    /**
     * Gets the entity class for a spotlight entity type/class.
     */
    protected String spotlight(String spotlightTag) {

        if (spotlightTag == null || spotlightTag.trim().isEmpty())
            return EntityClassMap.getNullCategory();

        String t = EntityClassMap.getNullCategory();
        if (spotlightTag.toLowerCase().contains("person")) {
            t = EntityClassMap.P;
        } else if (spotlightTag.toLowerCase().contains("organisation")) {
            t = EntityClassMap.O;
        } else if (spotlightTag.toLowerCase().contains("place")) {
            t = EntityClassMap.L;
        }

        return t;
    }
}
