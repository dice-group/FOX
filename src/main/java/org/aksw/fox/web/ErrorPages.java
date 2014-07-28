package org.aksw.fox.web;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.DefaultErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;

/**
 * Custom error pages (400,404,405).
 * 
 * @author rspeck
 * 
 */
public class ErrorPages extends DefaultErrorPageGenerator {
    public final static Logger LOG = Logger.getLogger(ErrorPages.class);

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {

        String defaultResponse = super.generate(request, status, reasonPhrase, description, exception);

        LOG.debug("HTTP status: " + status);
        String page = "";
        switch (status) {
        case 404: {
            page = "404.html";
            break;
        }
        case 405: {
            page = "405.html";
            break;
        }
        case 400: {
            page = "400.html";
            break;
        }
        default:
            return defaultResponse;
        }

        //
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(ErrorPages.class.getResource(page).openStream(), writer, "UTF-8");
        } catch (IOException e) {
            LOG.error("\n", e);
            return defaultResponse;
        }
        return writer.toString();
    }
}
