package org.aksw.fox.tools.ke;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

@Deprecated
public class Yahooapis {
  public static void main(final String[] a) throws Exception {

    final String in = "University of Leipzig in Leipzig..";

    final String query = "select * from search.termextract where context=";

    final URIBuilder builder = new URIBuilder();
    builder.setScheme("http").setHost("query.yahooapis.com").setPath("/v1/public/yql")
        .setParameter("diagnostics", "false").setParameter("q", query + "'" + in + "'");
    final HttpGet httpget = new HttpGet(builder.build());
    System.out.println(httpget.getURI());

    @SuppressWarnings("resource")
    final HttpClient httpclient = new DefaultHttpClient();
    final HttpResponse response = httpclient.execute(httpget);
    final HttpEntity entity = response.getEntity();
    if (entity != null) {
      final InputStream instream = entity.getContent();
      try {
        final String myString = IOUtils.toString(instream, "UTF-8");
        System.out.println(myString);
      } finally {
        instream.close();
      }
    }
  }
}
