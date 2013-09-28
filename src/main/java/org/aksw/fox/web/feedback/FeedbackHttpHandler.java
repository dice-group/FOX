package org.aksw.fox.web.feedback;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.web.AbstractFoxHttpHandler;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class FeedbackHttpHandler extends AbstractFoxHttpHandler {

    private static Logger logger = Logger.getLogger(FeedbackHttpHandler.class);

    protected FeedbackStore feedbackStore = null;

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
    public FeedbackHttpHandler(FeedbackStore feedbackStore) {
        this.feedbackStore = feedbackStore;
    }

    @Override
    protected void postService(Request request, Response response, Map<String, String> parameter) {

        insert(parameter);

        setResponse(response, "ok", HttpURLConnection.HTTP_OK, "text/plain");
    }

    /**
     * Checks access and parameter.
     * 
     * <p>
     * password: the password in fox properties <br>
     * ...
     * </p>
     */
    @Override
    protected boolean checkParameter(Map<String, String> formData) {
        logger.info("checking form parameter ...");

        for (String parameter : Arrays.asList(new String[] {
                "password", "text", "entityUri", "surfaceForm", "offset", "feedback", "context", "feedbackType", "system", "annotation"
        })) {
            if (formData.get(parameter) == null || formData.get(parameter).trim().isEmpty())
                return false;
            formData.put(parameter, formData.get(parameter).trim());
        }

        if (!formData.get("password").equals(FoxCfg.get("password")))
            return false;

        logger.info("ok.");
        return true;
    }

    /**
     * Inserts some text into the store.
     * 
     * @param in
     */
    protected void insert(Map<String, String> parameter) {
        this.feedbackStore.insert(parameter);
    }

    @Override
    public List<String> getMappings() {
        List<String> l = new ArrayList<>();
        l.add("/api/ner/feedback");
        return l;
    }
}
