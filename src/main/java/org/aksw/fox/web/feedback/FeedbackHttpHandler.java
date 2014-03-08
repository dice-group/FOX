package org.aksw.fox.web.feedback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FeedbackHttpHandler extends HttpHandler {

    private static Logger logger = Logger.getLogger(FeedbackHttpHandler.class);

    public static List<String> PARAMETER = new ArrayList<>();
    public static List<String> PARAMETER_OPTIONAL = new ArrayList<>();
    static {
        Collections.addAll(PARAMETER,
                "key", "text", "entity_uri", "surface_form", "offset", "feedback", "systems", "manual", "annotation"
                );

        Collections.addAll(PARAMETER_OPTIONAL,
                "url", "gender", "language"
                );
    }

    protected FeedbackStore feedbackStore = null;
    private UrlValidator urlValidator = new UrlValidator();

    private String errorMessage = "";

    /**
     * Handles HTTP POST requests to store feedback.
     */
    public FeedbackHttpHandler() {
        this(new FeedbackStore());
    }

    /**
     * Handles HTTP POST requests to store feedback.
     * 
     * @param feedbackStore
     */
    public FeedbackHttpHandler(FeedbackStore feedbackStore) {
        this.feedbackStore = feedbackStore;
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        logger.info("service ...");

        if (getMappings().contains(request.getContextPath() + request.getHttpHandlerPath())) {
            if (request.getMethod().getMethodString().equalsIgnoreCase("POST")) {
                logger.info("service post ...");
                boolean done = false;
                if (request.getContentType().contains("application/json")) {
                    // json request
                    logger.info("application/json ...");
                    done = insertJson(request, response);

                } else if (request.getContentType().contains("application/xml")) {
                    // xml request
                    logger.info("application/xml ...");
                    done = insertXML(request, response);
                } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
                    // form
                    logger.info("application/x-www-form-urlencoded ...");
                    done = insertForm(request, response);
                } else {
                    errorMessage = "Couldn't find a supported Content-Type.";
                }

                setResponse(response, done ? "ok" : errorMessage, done ? HttpURLConnection.HTTP_OK : HttpURLConnection.HTTP_BAD_REQUEST, "text/plain");

            } else {
                setResponse(response, "Please use HTTP POST method.", HttpURLConnection.HTTP_BAD_METHOD, "text/plain");
            }
        } else {
            setResponse(response, "404", HttpURLConnection.HTTP_NOT_FOUND, "text/plain");
        }
    }

    protected String getQuery(Request request) {
        int contentLength = request.getContentLength();
        byte[] data = new byte[contentLength];
        InputStream is = new BufferedInputStream(request.getInputStream());
        int offset = 0;
        try {
            while (offset < contentLength) {
                final int pointer = is.read(data, offset, contentLength - offset);
                if (pointer == -1)
                    break;
                offset += pointer;
            }
        } catch (Exception e) {
            logger.error("\n", e);
        }

        String query = "";
        try {
            query = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("\n", e);
            query = "";
        }
        return query;
    }

    public boolean checkKey(String key) {
        if (key.equals(FoxCfg.get("api_key")))
            return true;
        else
            return false;
    }

    public boolean insertJson(Request request, Response response) {
        boolean rtn = true;
        String query = getQuery(request);
        logger.info("query:" + query);

        if (!query.isEmpty()) {
            JsonObject json = (JsonObject) new JsonParser().parse(query);

            TextEntry textEntry = new TextEntry();
            logger.info(json);

            String key = "";
            if (json.get("key") != null) {
                key = json.get("key").getAsString();
            }
            if (checkKey(key)) {
                // required
                List<FeedbackEntry> feedbackEntries = new ArrayList<>();
                try {
                    textEntry.text = json.get("text").getAsString();

                    for (JsonElement entity : json.get("entities").getAsJsonArray()) {
                        JsonObject e = entity.getAsJsonObject();

                        FeedbackEntry fe = new FeedbackEntry();
                        fe.entity_uri = e.get("entity_uri").getAsString();
                        fe.surface_form = e.get("surface_form").getAsString();
                        fe.offset = e.get("offset").getAsInt();
                        fe.feedback = e.get("feedback").getAsString();
                        fe.systems = e.get("systems").getAsString();
                        fe.manual = e.get("manual").getAsString();
                        fe.annotation = e.get("annotation").getAsString();

                        // TODO: check all values

                        // check given offset
                        boolean found = true;
                        String tmp_surface_form = "";
                        try {
                            tmp_surface_form = textEntry.text.substring(fe.offset, fe.offset + fe.surface_form.length());
                        } catch (Exception ee) {
                            found = false;
                        }
                        if (!found || !tmp_surface_form.equals(fe.surface_form)) {
                            rtn = false;
                            logger.error("Can't find surface form in text with the given offset: " + fe.entity_uri);
                            errorMessage = "Can't find surface form in text with the given offset: " + fe.entity_uri;
                            break;
                        }

                        // check uri
                        if (isValidUrl(fe.entity_uri)) {
                            feedbackEntries.add(fe);
                        } else {
                            rtn = false;
                            logger.error("Isn't a valid url: " + fe.entity_uri);
                            errorMessage = "Isn't a valid url: " + fe.entity_uri;
                            break;
                        }
                    }

                } catch (Exception e) {
                    logger.error("\n", e);
                    errorMessage = "Couldn't read data, check required parameters.";
                    rtn = false;
                }

                // optional
                textEntry.url = (json.get("url") != null) ? json.get("url").toString() : "";
                textEntry.gender = (json.get("gender") != null) ? json.get("gender").toString() : "";
                textEntry.language = (json.get("language") != null) ? json.get("language").toString() : "";

                // insert data
                if (rtn)
                    rtn = insert(textEntry, feedbackEntries);

            } else {
                errorMessage = "Wrong feedback api key.";
                rtn = false;
            }
        } else {
            errorMessage = "Empty query.";
            rtn = false;
        }
        return rtn;
    }

    public boolean insertXML(Request request, Response response) {
        errorMessage = "XML not supported yet.";
        return false;
    }

    public boolean insertForm(Request request, Response response) {
        boolean rtn = true;
        boolean apiKeyValid = false;
        TextEntry textEntry = null;
        FeedbackEntry fe = new FeedbackEntry();
        List<FeedbackEntry> feedbackEntries = new ArrayList<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null && parameterMap.size() > 0) {
            textEntry = new TextEntry();

            for (Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (rtn && entry.getValue() != null && entry.getValue().length > 0)
                    try {
                        String key = entry.getKey();
                        if (key != null && entry.getValue().length > 0) {

                            switch (key.toLowerCase()) {

                            case "key":
                                String apiKey = entry.getValue()[0];
                                if (apiKey != null && checkKey(apiKey)) {
                                    apiKeyValid = true;
                                }
                                break;

                            case "text":
                                textEntry.text = URLDecoder.decode(entry.getValue()[0], "UTF-8");
                                break;

                            case "entity_uri":
                                fe.entity_uri = URLDecoder.decode(entry.getValue()[0], "UTF-8");
                                break;

                            case "surface_form":
                                fe.surface_form = URLDecoder.decode(entry.getValue()[0], "UTF-8");
                                break;

                            case "offset":
                                fe.offset = Integer.valueOf(entry.getValue()[0]);
                                break;

                            case "feedback":
                                fe.feedback = entry.getValue()[0];
                                break;

                            case "systems":
                                fe.systems = entry.getValue()[0];
                                break;

                            case "manual":
                                fe.manual = entry.getValue()[0];
                                break;

                            case "gender":
                                textEntry.gender = entry.getValue()[0];
                                break;

                            case "url":
                                textEntry.url = entry.getValue()[0];
                                break;

                            case "language":
                                textEntry.language = entry.getValue()[0];
                                break;
                            }
                        } else {
                            rtn = false;
                            errorMessage = "Parameter missing or no value given.";
                            break;
                        }
                    } catch (Exception e) {
                        rtn = false;
                        errorMessage = "Exception while reading parameters and values.";
                        logger.error("\n", e);
                        break;
                    }
            }
            feedbackEntries.add(fe);
        } else {
            errorMessage = "Parameters missing.";
            rtn = false;
        }

        // check given offset
        boolean found = true;
        String tmp_surface_form = "";
        try {
            tmp_surface_form = textEntry.text.substring(fe.offset, fe.offset + fe.surface_form.length());
        } catch (Exception ee) {
            found = false;
        }
        if (!found || !tmp_surface_form.equals(fe.surface_form)) {
            rtn = false;
            logger.error("Can't find surface form in text with the given offset: " + fe.entity_uri);
            errorMessage = "Can't find surface form in text with the given offset: " + fe.entity_uri;

        }

        // check uri
        if (isValidUrl(fe.entity_uri)) {
            feedbackEntries.add(fe);
        } else {
            rtn = false;
            logger.error("Isn't a valid url: " + fe.entity_uri);
            errorMessage = "Isn't a valid url: " + fe.entity_uri;
        }

        // check api key
        if (rtn) {
            if (!apiKeyValid) {
                errorMessage = "Wrong feedback api key.";
                rtn = false;
            } else if (textEntry != null && feedbackEntries.size() > 0) {
                rtn = insert(textEntry, feedbackEntries);
            } else {
                errorMessage = "Nothing found.";
                rtn = false;
            }
        }
        return rtn;
    }

    private boolean isValidUrl(String url) {
        if (!urlValidator.isValid(url)) {
            logger.error("uri isn't valid: " + url);
            return false;
        }
        return true;
    }

    /**
     * Inserts some text into the store.
     * 
     * @param in
     */
    protected boolean insert(TextEntry textEntry, List<FeedbackEntry> feedbackEntries) {
        return this.feedbackStore.insert(textEntry, feedbackEntries);
    }

    public List<String> getMappings() {
        List<String> l = new ArrayList<>();
        l.add("/api/ner/feedback");
        return l;
    }

    /**
     * Writes data to response.
     * 
     * @param response
     * @param text
     * @param status
     * @param contentType
     */
    protected void setResponse(Response response, String data, int status, String contentType) {

        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        response.setStatus(status);

        byte[] bytes = data.getBytes();
        try {
            response.setContentLength(bytes.length);
            response.getWriter().write(data);
        } catch (IOException e) {
            logger.error("\n", e);
        }
        response.finish();
    }
}
