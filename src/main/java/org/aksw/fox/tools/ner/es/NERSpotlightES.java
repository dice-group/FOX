package org.aksw.fox.tools.ner.es;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NERSpotlightES extends AbstractNER {

    public static Logger                 LOG                  = LogManager.getLogger(NERSpotlight.class);
    public static final XMLConfiguration CFG                  = CfgManager.getCfg(NERSpotlight.class);

    private final static String          SPOTLIGHT_URL        = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_URL);
    private final static String          SPOTLIGHT_CONFIDENCE = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_CONFIDENCE);
    private final static String          SPOTLIGHT_SUPPORT    = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_SUPPORT);
    private final static String          SPOTLIGHT_TYPES      = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_TYPES);
    private final static String          SPOTLIGHT_SPARQL     = CFG.getString(FoxConst.CFG_KEY_SPOTLIGHT_SPARQL);
    public static final Charset          UTF_8                = Charset.forName("UTF-8");

    public static void main(String[] a) {

        NERSpotlight s = new NERSpotlight();
        LOG.info(s.retrieve(FoxConst.NER_GER_EXAMPLE_1));
    }

    /**
     * 
     */
    public NERSpotlight() {
        // TODO: Ping endpoints and use an online one.
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

        // TRACE
        if (LOG.isTraceEnabled()) {
            LOG.trace(list);
        } // TRACE*/
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
        LOG.debug(httpResponse.getStatusLine());

        HttpEntity entry = httpResponse.getEntity();
        String spotlightResponse = IOUtils.toString(entry.getContent(), UTF_8);
        LOG.debug(spotlightResponse);

        EntityUtils.consume(entry);
        return spotlightResponse;
    }

}