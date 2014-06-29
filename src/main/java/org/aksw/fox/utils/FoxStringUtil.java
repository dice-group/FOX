package org.aksw.fox.utils;

import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class FoxStringUtil {
    public final static Logger logger = Logger.getLogger(FoxStringUtil.class);

    public static String encodeURLComponent(String in) {
        return encodeURLComponent(in, "UTF-8");
    }

    public static String encodeURLComponent(String in, String charset) {
        try {
            return URLEncoder.encode(in, charset).
                    replaceAll("\\+", "%20").
                    replaceAll("\\%21", "!").
                    replaceAll("\\%27", "'").
                    replaceAll("\\%28", "(").
                    replaceAll("\\%29", ")").
                    replaceAll("\\%7E", "~");
        } catch (Exception e) {
            logger.error("\n", e);
            return "";
        }
    }
}
