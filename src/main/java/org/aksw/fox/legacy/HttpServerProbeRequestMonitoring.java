package org.aksw.fox.legacy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.HttpServerProbe;
import org.glassfish.grizzly.http.server.Response;

@Deprecated
public class HttpServerProbeRequestMonitoring extends HttpServerProbe.Adapter {

  public final static Logger LOG = LogManager.getLogger(HttpServerProbeRequestMonitoring.class);

  Set<String> allowedpaths = null;

  public HttpServerProbeRequestMonitoring(final Set<String> allowedpaths) {
    this.allowedpaths = allowedpaths;
  }

  @Override
  public void onRequestCompleteEvent(final HttpServerFilter arg0,
      @SuppressWarnings("rawtypes") final Connection arg1, final Response arg2) {

    String path = "";
    try {
      path = new URL(arg2.getRequest().getRequestURL().toString()).getPath();
    } catch (final MalformedURLException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    path = path.replaceAll("/", " ").trim();
    path = path.split(" ")[0];
    if (allowedpaths.contains(path)) {
      // TODO: store user infos
    }
  }
}
