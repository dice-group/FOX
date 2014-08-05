package org.aksw.fox.web;

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
 * @author rspeck
 * 
 */
public abstract class AbstractFoxHttpHandler extends HttpHandler {

    public static Logger LOG = Logger.getLogger(AbstractFoxHttpHandler.class);

    abstract public List<String> getMappings();

    /**
     * 
     * @param request
     * @param response
     */
    @Override
    public void service(Request request, Response response) throws Exception {
        LOG.info("service ...");

        // log RemoteAddr
        {
            String ra = request.getRemoteAddr();
            int index = ra.lastIndexOf(".");
            if (index > 0 && ra.length() > index)
                LOG.info("remote addr.: " + ra.substring(0, index));
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
                Map<String, String> parameter = getPostParameter(request);

                if (parameter.size() == 0) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("HTTP_BAD_REQUEST (400)");
                    setResponse(response, HttpURLConnection.HTTP_BAD_REQUEST);

                } else {

                    if (checkParameter(parameter)) {
                        postService(request, response, parameter);
                    } else {
                        if (LOG.isDebugEnabled())
                            LOG.debug("HTTP_BAD_REQUEST (400)");
                        setResponse(response, HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                }
            } else {
                if (LOG.isDebugEnabled())
                    LOG.debug("HTTP_BAD_METHOD (405)");
                setResponse(response, HttpURLConnection.HTTP_BAD_METHOD);
            }
        } else {
            if (LOG.isDebugEnabled())
                LOG.debug("HTTP_NOT_FOUND (404)");
            setResponse(response, HttpURLConnection.HTTP_NOT_FOUND);
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
    protected abstract void postService(Request request, Response response, Map<String, String> parameter);

    /**
     * Gets request POST parameters. The Map key holds the parameter name and
     * the Map value the parameter value.
     * 
     */
    protected Map<String, String> getPostParameter(Request request) {

        Map<String, String> formMap = new HashMap<String, String>();
        if (request.getContentType().contains("application/json")) {
            LOG.info("application/json ...");

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
                LOG.error("\n", e);
            }

            String query = "";
            try {
                query = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("\n", e);
                query = "";
            }

            LOG.info("query:" + query);
            LOG.info(data.length == contentLength);

            if (!query.isEmpty()) {

                JsonParser parser = new JsonParser();

                JsonObject o = (JsonObject) parser.parse(query);

                String para = o.toString();
                para = para.substring(1, para.length() - 1);

                String[] pairs = para.split(",");
                for (String pair : pairs) {
                    if (pair.contains(":")) {
                        String key = pair.substring(0, pair.indexOf(":")).trim();
                        String value = pair.substring(pair.indexOf(":") + 1).trim();

                        if (key.startsWith("\""))
                            key = key.substring(1, key.length() - 1);

                        if (value.startsWith("\""))
                            value = value.substring(1, value.length() - 1);

                        try {
                            formMap.put(key, URLDecoder.decode(value, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            LOG.error("\n", e);
                        }
                    }
                }
            } else {
                LOG.error("query is empty!");
            }

        } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
            LOG.info("application/x-www-form-urlencoded ...");
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap != null && parameterMap.size() > 0) {

                for (Entry<String, String[]> entry : parameterMap.entrySet()) {
                    if (entry.getValue().length > 0)
                        try {
                            // formMap.put(entry.getKey().toLowerCase(),
                            // URLDecoder.decode(entry.getValue()[0],"UTF-8"));
                            formMap.put(entry.getKey().toLowerCase(), entry.getValue()[0]);

                        } catch (Exception e) {
                            LOG.error("\n", e);
                        }
                }
            }
        } else {
            LOG.error("Header Content-Type not supported: " + request.getContentType());
        }

        if (LOG.isDebugEnabled())
            LOG.debug(formMap);

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
    protected void setResponse(Response response, String data, int status, String contentType) {

        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        response.setStatus(status);

        byte[] bytes = data.getBytes();
        try {
            response.setContentLength(bytes.length);
            response.getWriter().write(data);
        } catch (IOException e) {
            LOG.error("\n", e);
        }
        response.finish();
    }

    protected void setResponse(Response response, int status) {
        try {
            response.sendError(status);
        } catch (IOException e) {
            LOG.error("\n", e);
        }
        response.finish();
    }
}
