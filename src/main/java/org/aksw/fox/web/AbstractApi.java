package org.aksw.fox.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
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
public abstract class AbstractApi extends HttpHandler {

    public static Logger logger = Logger.getLogger(AbstractApi.class);

    /**
     * 
     * @param request
     * @param response
     */
    protected abstract void service(Request request, Response response, Map<String, String> parameter);

    /**
     * 
     * @param request
     * @param response
     */
    @Override
    public void service(Request request, Response response) throws Exception {
        logger.info("service ...");
        if (request.getMethod().getMethodString().equalsIgnoreCase("POST")) {
            logger.info("service post ...");
            Map<String, String> parameter = getPostParameter(request);
            if (checkPostParameter(parameter)) {

                service(request, response, parameter);

            } else {
                setResponse(response, "Wrong parameter.", HttpURLConnection.HTTP_BAD_REQUEST, "text/plain");
            }
        } else {
            setResponse(response, "Please use HTTP POST method.", HttpURLConnection.HTTP_BAD_METHOD, "text/plain");
        }
    }

    /**
     * Checks POST parameter.
     * 
     * type: url | text
     * 
     * task: ke | ner | keandner | re | all
     * 
     * output: rdf | turtle | html
     * 
     * nif: true : false
     * 
     * input : plain text | url
     */
    protected boolean checkPostParameter(Map<String, String> formData) {

        logger.info("checking form parameter ...");

        String type = formData.get("type");
        if (type == null || !(type.equalsIgnoreCase("url") || type.equalsIgnoreCase("text")))
            return false;

        String text = formData.get("input");
        if (text == null || text.trim().isEmpty())
            return false;

        String task = formData.get("task");
        if (task == null || !(task.equalsIgnoreCase("ke") || task.equalsIgnoreCase("ner") || task.equalsIgnoreCase("keandner") || task.equalsIgnoreCase("re") || task.equalsIgnoreCase("all")))
            return false;

        String output = formData.get("output");

        if (!output.equalsIgnoreCase("JSONLD") && !output.equalsIgnoreCase("RDF/JSON") && !output.equalsIgnoreCase("RDF/XML") && !output.equalsIgnoreCase("RDF/XML-ABBREV") && !output.equalsIgnoreCase("TURTLE") && !output.equalsIgnoreCase("N-TRIPLE") && !output.equalsIgnoreCase("N3"))
            return false;

        String nif = formData.get("nif");
        if (nif == null || !nif.equalsIgnoreCase("true"))
            formData.put("nif", "false");
        else
            formData.put("nif", "true");

        String foxlight = formData.get("foxlight");
        if (foxlight == null || !foxlight.equalsIgnoreCase("true"))
            formData.put("foxlight", "false");
        else
            formData.put("foxlight", "true");

        logger.info("ok.");
        return true;
    }

    /**
     * Get request POST parameter to formMap. The Map key holds the parameter
     * name and the Map value the parameter value.
     * 
     */
    protected Map<String, String> getPostParameter(Request request) {

        Map<String, String> formMap = new HashMap<String, String>();
        if (request.getContentType().contains("application/json")) {
            logger.info("application/json ...");

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

            logger.info("query:" + query);
            logger.info(data.length == contentLength);

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
                            logger.error("\n", e);
                        }
                    }
                }
            } else {
                logger.error("query is empty!");
            }

        }

        else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
            logger.info("application/x-www-form-urlencoded ...");
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap != null && parameterMap.size() > 0) {

                for (Entry<String, String[]> entry : parameterMap.entrySet()) {
                    if (entry.getValue().length > 0)
                        try {
                            // formMap.put(entry.getKey().toLowerCase(),
                            // URLDecoder.decode(entry.getValue()[0],"UTF-8"));
                            formMap.put(entry.getKey().toLowerCase(), entry.getValue()[0]);

                        } catch (Exception e) {
                            logger.error("\n", e);
                        }
                }
            }
        } else {
            logger.error("Header Content-Type not supported: " + request.getContentType());
        }
        logger.info(formMap);
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
            logger.error("\n", e);
        }
        response.finish();
    }
}
