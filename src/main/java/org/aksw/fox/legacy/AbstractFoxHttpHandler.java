package org.aksw.fox.legacy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public abstract class AbstractFoxHttpHandler extends HttpHandler {

  public static Logger LOG = Logger.getLogger(AbstractFoxHttpHandler.class);

  abstract public List<String> getMappings();

  /**
   *
   * @param request
   * @param response
   */
  @Override
  public void service(final Request request, final Response response) throws Exception {
    LOG.info("service ...");

    // log RemoteAddr
    {
      final String ra = request.getRemoteAddr();
      final int index = ra.lastIndexOf(".");
      if ((index > 0) && (ra.length() > index)) {
        LOG.info("remote addr.: " + ra.substring(0, index));
      }
    }

    // log path
    if (LOG.isDebugEnabled()) {
      LOG.debug("mapping list: " + getMappings());
      LOG.debug("context path: " + request.getContextPath());
    }

    // service
    if (getMappings().contains(request.getContextPath())) {

      if (request.getMethod().getMethodString().equalsIgnoreCase("POST")) {
        LOG.info("service post ...");
        final Map<String, String> parameter = getPostParameter(request);

        if (parameter.size() == 0) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP_BAD_REQUEST (400)");
          }
          sendError(response, HttpURLConnection.HTTP_BAD_REQUEST);

        } else {

          if (checkParameter(parameter)) {
            postService(request, response, parameter);
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("HTTP_BAD_REQUEST (400)");
            }
            sendError(response, HttpURLConnection.HTTP_BAD_REQUEST);
          }
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("HTTP_BAD_METHOD (405)");
        }
        sendError(response, HttpURLConnection.HTTP_BAD_METHOD);
      }
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("HTTP_NOT_FOUND (404)");
      }
      sendError(response, HttpURLConnection.HTTP_NOT_FOUND);
    }
  }

  /**
   * Checks POST parameter.
   *
   */
  abstract protected boolean checkParameter(Map<String, String> formData);

  /**
   *
   * @param request
   * @param response
   */
  protected abstract void postService(Request request, Response response,
      Map<String, String> parameter);

  /**
   * Gets request POST parameters. The Map key holds the parameter name and the Map value the
   * parameter value.
   *
   */
  protected Map<String, String> getPostParameter(final Request request) {

    final Map<String, String> formMap = new HashMap<String, String>();
    if (request.getContentType().contains("application/json")) {
      LOG.info("application/json ...");

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
        LOG.error(e.getLocalizedMessage(), e);
      }

      String query = "";
      try {
        query = new String(data, "UTF-8");
      } catch (final UnsupportedEncodingException e) {
        LOG.error(e.getLocalizedMessage(), e);
        query = "";
      }

      LOG.info("query:" + query);
      LOG.info(data.length == contentLength);

      if (!query.isEmpty()) {

        final JsonParser parser = new JsonParser();

        final JsonObject o = (JsonObject) parser.parse(query);

        String para = o.toString();
        para = para.substring(1, para.length() - 1);

        final String[] pairs = para.split(",");
        for (final String pair : pairs) {
          if (pair.contains(":")) {
            String key = pair.substring(0, pair.indexOf(":")).trim();
            String value = pair.substring(pair.indexOf(":") + 1).trim();

            if (key.startsWith("\"")) {
              key = key.substring(1, key.length() - 1);
            }

            if (value.startsWith("\"")) {
              value = value.substring(1, value.length() - 1);
            }

            try {
              formMap.put(key, URLDecoder.decode(value, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
              LOG.error(e.getLocalizedMessage(), e);
            }
          }
        }
      } else {
        LOG.error("query is empty!");
      }

    } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
      LOG.info("application/x-www-form-urlencoded ...");
      final Map<String, String[]> parameterMap = request.getParameterMap();
      if ((parameterMap != null) && (parameterMap.size() > 0)) {

        for (final Entry<String, String[]> entry : parameterMap.entrySet()) {
          if (entry.getValue().length > 0) {
            try {
              // formMap.put(entry.getKey().toLowerCase(),
              // URLDecoder.decode(entry.getValue()[0],"UTF-8"));
              formMap.put(entry.getKey().toLowerCase(), entry.getValue()[0]);

            } catch (final Exception e) {
              LOG.error(e.getLocalizedMessage(), e);
            }
          }
        }
      }
    } else {
      LOG.error("Header Content-Type not supported: " + request.getContentType());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(formMap);
    }

    return formMap;
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
      LOG.error(e.getLocalizedMessage(), e);
    }
    response.finish();
  }

  private void sendError(final Response response, final int status) {
    try {
      response.sendError(status);
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    response.finish();
  }
}
