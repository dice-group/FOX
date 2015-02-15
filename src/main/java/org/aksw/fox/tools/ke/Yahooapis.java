package org.aksw.fox.tools.ke;

import java.io.InputStream;

import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.PropertyConfigurator;

public class Yahooapis {
    public static void main(String[] a) throws Exception {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);

        String in = "University of Leipzig in Leipzig..";

        String query = "select * from search.termextract where context=";

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("query.yahooapis.com").setPath("/v1/public/yql").setParameter("diagnostics", "false").setParameter("q", query + "'" + in + "'");
        HttpGet httpget = new HttpGet(builder.build());
        System.out.println(httpget.getURI());

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                String myString = IOUtils.toString(instream, "UTF-8");
                System.out.println(myString);
            } finally {
                instream.close();
            }
        }
    }
}
