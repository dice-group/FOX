package org.aksw.fox.nertools;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxConst;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class NERSpotlight extends AbstractNER {
	
	// Web service and installed tool show different quality
	//private final static String API_URL = "http://localhost:2222/";
	//private final static String API_URL = "http://spotlight.dbpedia.org/";
	private final static String API_URL = "http://spotlight.sztaki.hu:2222/";
	private final static String CONFIDENCE = "0.2";
	private final static String SUPPORT = "2";

	@Override
	public List<Entity> retrieve(String input) {
		 
		logger.info("retrieve ...");
		
		String spotlightResponse = null;
		
		// Get the information from server-side.
		try {	
			spotlightResponse = Request.Post(API_URL + "rest/annotate")
				.addHeader("Accept", "application/json")
				.bodyForm(Form.form().add("confidence", CONFIDENCE).add("support", SUPPORT).add("text", input)
				.build())
				.execute().returnContent().toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("all: " + spotlightResponse);
		
		JSONObject resultJSON = null;
		JSONArray entities = null;
		
		// Check which requests held new information.
		try {
			resultJSON = new JSONObject(spotlightResponse);
			if (resultJSON.has("Resources")) {
				entities = resultJSON.getJSONArray("Resources");
			}
		} catch (JSONException e) {
            logger.error("JSON exception "+e);
        }
		
		// Put the gathered information into a list of FOX Entities.
		List<Entity> list = new ArrayList<Entity>();
			
		// In case entities were returned...
		if (entities != null) {
			logger.debug(entities.toString());
			// ...iterate through each of them...
			for(int i = 0; i < entities.length(); i++) {
				try {
					// ...and add a new FOX-entity to the total entity list.
					JSONObject entity = entities.getJSONObject(i);
					list.add(getEntity(entity.getString("@surfaceForm"), EntityClassMap.spotlight(entity.getString("@types")), Entity.DEFAULT_RELEVANCE, "Spotlight"));
					// Test Output
					/*if (entity.getString("@types").contains(":Person,")) {
						logger.info("JAP!! Person!: " + entity.getString("@surfaceForm") + " " + EntityClassMap.spotlight(entity.getString("@types")));
					} else if (entity.getString("@types").contains(":Place,")) {
						logger.info("JAP!! Location!: " + entity.getString("@surfaceForm") + " " + EntityClassMap.spotlight(entity.getString("@types")));
					} else if (entity.getString("@types").contains(":Organisation,")) {
						logger.info("JAP!! Organisation!: " + entity.getString("@surfaceForm") + " " + EntityClassMap.spotlight(entity.getString("@types")));
					} else {
						logger.info("nope...");
					}*/
				} catch (JSONException e) {
		           	logger.error("JSON exception "+e);
		       	}
			}
		}
		
		// Test output
		//logger.info("Entitäten: " + list.size());
		
		// TRACE
        if (logger.isTraceEnabled()) {
            logger.trace(list);
        } // TRACE*/
		return list;
	}
	
	/*
	 *  Test Method
	 */
	public static void main(String[] a) {
		NERSpotlight ner = new NERSpotlight();
		 String text = "President Obama called Wednesday on Congress " +
				"to extend a tax break for students included in last year's economic stimulus " +
				"package, arguing that the policy provides more generous assistance.";
		
		text = FoxConst.EXAMPLE_1;
		ner.retrieve(text);	
	}
	
}
