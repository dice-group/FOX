package org.aksw.fox.web.feedback;

import java.net.HttpURLConnection;
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
        String input = parameter.get("input");

        insert(input);

        setResponse(response, "ok", HttpURLConnection.HTTP_OK, "text/plain");
    }

    /**
     * Checks access and parameter.
     * 
     * <p>
     * name: the name in fox properties <br>
     * password: the password in fox properties <br>
     * input: a feedback text to save
     * </p>
     */
    @Override
    protected boolean checkParameter(Map<String, String> formData) {
        logger.info("checking form parameter ...");

        String name = formData.get("name");
        if (name == null || !name.equalsIgnoreCase(FoxCfg.get("name")))
            return false;

        String pass = formData.get("password");
        if (pass == null || !pass.equalsIgnoreCase(FoxCfg.get("password")))
            return false;

        String input = formData.get("input");
        if (input == null || input.trim().isEmpty())
            return false;

        logger.info("ok.");
        return true;
    }

    /**
     * Inserts some text into the store.
     * 
     * @param in
     */
    protected void insert(String in) {
        this.feedbackStore.insert(in);
    }
}
