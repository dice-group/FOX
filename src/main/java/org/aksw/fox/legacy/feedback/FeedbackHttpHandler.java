package org.aksw.fox.legacy.feedback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Deprecated
public class FeedbackHttpHandler extends HttpHandler {

  public static final String CFG_KEY_API_KEY =
      FeedbackHttpHandler.class.getName().concat(".apikey");

  private static Logger LOG = LogManager.getLogger(FeedbackHttpHandler.class);

  public static List<String> PARAMETER = new ArrayList<>();
  public static List<String> PARAMETER_OPTIONAL = new ArrayList<>();
  static {
    Collections.addAll(PARAMETER, "key", "text", "entity_uri", "surface_form", "offset", "feedback",
        "systems", "manual", "annotation");

    Collections.addAll(PARAMETER_OPTIONAL, "url", "gender", "language");
  }

  // TODO: errorMessage
  String errorMessage = "";
  protected FeedbackStore feedbackStore = null;
  private final UrlValidator urlValidator = new UrlValidator();

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
  public FeedbackHttpHandler(final FeedbackStore feedbackStore) {
    this.feedbackStore = feedbackStore;
  }

  @Override
  public void service(final Request request, final Response response) throws Exception {
    LOG.info("service ...");
    if (LOG.isDebugEnabled()) {
      LOG.debug("mapping list: " + getMappings());
      LOG.debug("context path: " + request.getContextPath());
    }
    if (getMappings().contains(request.getContextPath())) {
      if (request.getMethod().getMethodString().equalsIgnoreCase("POST")) {
        LOG.info("service post ...");
        boolean done = false;
        if (request.getContentType().contains("application/json")) {
          // json request
          LOG.info("application/json ...");
          done = insertJson(request, response);

        } else if (request.getContentType().contains("application/xml")) {
          // xml request
          LOG.info("application/xml ...");
          done = insertXML(request, response);
        } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
          // form
          LOG.info("application/x-www-form-urlencoded ...");
          done = insertForm(request, response);
        } else {
          LOG.info("HTTP_UNSUPPORTED_TYPE (415)");
          setResponse(response, HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
        }
        if (done) {
          setResponse(response, "ok", HttpURLConnection.HTTP_OK, "text/plain");
        } else {
          LOG.info("HTTP_BAD_REQUEST (400)");
          setResponse(response, HttpURLConnection.HTTP_BAD_REQUEST);
        }

      } else {
        LOG.info("HTTP_BAD_METHOD (405)");
        setResponse(response, HttpURLConnection.HTTP_BAD_METHOD);
      }
    } else {
      LOG.info("HTTP_NOT_FOUND (404)");
      setResponse(response, HttpURLConnection.HTTP_NOT_FOUND);
    }
  }

  protected String getQuery(final Request request) {
    final int contentLength = request.getContentLength();
    final byte[] data = new byte[contentLength];
    final InputStream is = new BufferedInputStream(request.getInputStream());
    int offset = 0;
    try {
      while (offset < contentLength) {
        final int pointer = is.read(data, offset, contentLength - offset);
        if (pointer == -1) {
          break;
        }
        offset += pointer;
      }
    } catch (final Exception e) {
      LOG.error("\n", e);
    }

    String query = "";
    try {
      query = new String(data, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      LOG.error("\n", e);
      query = "";
    }
    return query;
  }

  public boolean checkKey(final String key) {
    if (key.equals(FoxCfg.get(CFG_KEY_API_KEY))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean insertJson(final Request request, final Response response) {
    boolean rtn = false;
    final String query = getQuery(request);
    LOG.info("query:" + query);

    if (!query.isEmpty()) {
      final JsonObject json = (JsonObject) new JsonParser().parse(query);

      final TextEntry textEntry = new TextEntry();
      LOG.info(json);

      String key = "";
      if (json.get("key") != null) {
        key = json.get("key").getAsString();
      }
      if (checkKey(key)) {
        // required
        final List<FeedbackEntry> feedbackEntries = new ArrayList<>();
        try {
          textEntry.text = json.get("text").getAsString();

          for (final JsonElement entity : json.get("entities").getAsJsonArray()) {
            final JsonObject e = entity.getAsJsonObject();

            final FeedbackEntry fe = new FeedbackEntry();
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
              tmp_surface_form =
                  textEntry.text.substring(fe.offset, fe.offset + fe.surface_form.length());
            } catch (final Exception ee) {
              found = false;
            }
            if (!found || !tmp_surface_form.equals(fe.surface_form)) {
              setErrorMessage(
                  "Can't find surface form in text with the given offset: " + fe.entity_uri);
              break;
            }

            // check uri
            if (isValidUrl(fe.entity_uri)) {
              feedbackEntries.add(fe);
            } else {
              setErrorMessage("Isn't a valid url: " + fe.entity_uri);
              break;
            }
          }
          rtn = true;

        } catch (final Exception e) {
          LOG.error("\n", e);
          errorMessage = "Couldn't read data, check required parameters.";
        }

        // optional
        textEntry.url = (json.get("url") != null) ? json.get("url").toString() : "";
        textEntry.gender = (json.get("gender") != null) ? json.get("gender").toString() : "";
        textEntry.language = (json.get("language") != null) ? json.get("language").toString() : "";

        // insert data
        if (rtn) {
          rtn = insert(textEntry, feedbackEntries);

        }
      } else {
        setErrorMessage("Wrong feedback api key.");
      }
    } else {
      setErrorMessage("Empty query.");
    }
    return rtn;
  }

  public boolean insertXML(final Request request, final Response response) {
    errorMessage = "XML not supported yet.";
    return false;
  }

  public boolean insertForm(final Request request, final Response response) {
    boolean rtn = true;
    boolean apiKeyValid = false;
    TextEntry textEntry = null;
    final FeedbackEntry fe = new FeedbackEntry();
    final List<FeedbackEntry> feedbackEntries = new ArrayList<>();
    final Map<String, String[]> parameterMap = request.getParameterMap();
    if ((parameterMap != null) && (parameterMap.size() > 0)) {
      textEntry = new TextEntry();

      for (final Entry<String, String[]> entry : parameterMap.entrySet()) {
        if (rtn && (entry.getValue() != null) && (entry.getValue().length > 0)) {
          try {
            final String key = entry.getKey();
            if ((key != null) && (entry.getValue().length > 0)) {
              final String v = entry.getValue()[0];
              switch (key.toLowerCase()) {

                case "key":
                  final String apiKey = v;
                  if ((apiKey != null) && checkKey(apiKey)) {
                    apiKeyValid = true;
                  }
                  break;

                case "text":
                  textEntry.text = URLDecoder.decode(v, "UTF-8");
                  break;

                case "entity_uri":
                  fe.entity_uri = URLDecoder.decode(v, "UTF-8");
                  break;

                case "surface_form":
                  fe.surface_form = URLDecoder.decode(v, "UTF-8");
                  break;

                case "offset":
                  fe.offset = Integer.valueOf(v);
                  break;

                case "feedback":
                  fe.feedback = v;
                  break;

                case "systems":
                  fe.systems = v;
                  break;

                case "manual":
                  fe.manual = v;
                  break;
                case "annotation":
                  fe.annotation = v;
                  break;
                case "gender":
                  textEntry.gender = v;
                  break;

                case "url":
                  textEntry.url = v;
                  break;

                case "language":
                  textEntry.language = v;
                  break;
              }
            } else {
              rtn = false;
              errorMessage = "Parameter missing or no value given.";
              break;
            }
          } catch (final Exception e) {
            rtn = false;
            errorMessage = "Exception while reading parameters and values.";
            LOG.error("\n", e);
            break;
          }
        }
      }
    } else {
      errorMessage = "Parameters missing.";
      rtn = false;
    }

    // check given offset
    boolean found = true;
    String tmp_surface_form = "";
    try {
      tmp_surface_form = textEntry.text.substring(fe.offset, fe.offset + fe.surface_form.length());
    } catch (final Exception ee) {
      found = false;
    }
    if (!found || !tmp_surface_form.equals(fe.surface_form)) {
      rtn = false;
      setErrorMessage("Can't find surface form in text with the given offset: " + fe.entity_uri);
    }

    // check uri
    if (!isValidUrl(fe.entity_uri)) {
      rtn = false;
      setErrorMessage("Isn't a valid url: " + fe.entity_uri);
    }

    // check api key
    if (rtn) {
      feedbackEntries.add(fe);
      if (!apiKeyValid) {
        errorMessage = "Wrong feedback api key.";
        rtn = false;
      } else if ((textEntry != null) && (feedbackEntries.size() > 0)) {
        rtn = insert(textEntry, feedbackEntries);
      } else {
        errorMessage = "Nothing found.";
        rtn = false;
      }
    }
    return rtn;
  }

  private boolean isValidUrl(final String url) {
    if (!urlValidator.isValid(url)) {
      LOG.error("uri isn't valid: " + url);
      return false;
    }
    return true;
  }

  /**
   * Inserts some text into the store.
   * 
   * @param in
   */
  protected boolean insert(final TextEntry textEntry, final List<FeedbackEntry> feedbackEntries) {
    final boolean rtn = feedbackStore.insert(textEntry, feedbackEntries);
    if (!rtn) {
      errorMessage = feedbackStore.errorMessage;
    }
    return rtn;
  }

  public List<String> getMappings() {
    return Arrays.asList("/api/ner/feedback");
  }

  /**
   * Writes data to response.
   * 
   * @param response
   * @param text
   * @param status
   * @param contentType
   */
  protected void setResponse(final Response response, final String data, final int status,
      final String contentType) {

    response.setContentType(contentType);
    response.setCharacterEncoding("utf-8");
    response.setStatus(status);

    final byte[] bytes = data.getBytes();
    try {
      response.setContentLength(bytes.length);
      response.getWriter().write(data);
    } catch (final IOException e) {
      LOG.error("\n", e);
    }
    response.finish();
  }

  protected void setResponse(final Response response, final int status) {
    try {
      response.sendError(status);
    } catch (final IOException e) {
      LOG.error("\n", e);
    }
    response.finish();
  }

  private void setErrorMessage(final String e) {
    LOG.error(e);
    errorMessage = e;
  }
}
