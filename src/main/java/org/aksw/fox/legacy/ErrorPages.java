package org.aksw.fox.legacy;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

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
@Deprecated
public class ErrorPages extends DefaultErrorPageGenerator {

  public final static Logger LOG = Logger.getLogger(ErrorPages.class);
  public final static Map<Integer, String> pages = new HashMap<>();
  static {
    pages.put(400, null);
    pages.put(404, null);
    pages.put(405, null);
  }

  @Override
  public String generate(final Request request, final int status, final String reasonPhrase,
      final String description, final Throwable exception) {

    if (LOG.isDebugEnabled()) {
      LOG.debug("HTTP status: " + status);
    }

    if (pages.get(status) == null) {
      if (pages.keySet().contains(status)) {
        pages.put(status, read(String.valueOf(status).concat(".html")));
      } else {
        return super.generate(request, status, reasonPhrase, description, exception);
      }
    }
    return pages.get(status);
  }

  protected String read(final String page) {
    final StringWriter writer = new StringWriter();
    try {
      IOUtils.copy(ErrorPages.class.getResource(page).openStream(), writer, "UTF-8");
    } catch (final IOException e) {
      LOG.error("\n", e);
    }
    return writer.toString();
  }
}
