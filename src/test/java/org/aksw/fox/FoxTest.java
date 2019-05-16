package org.aksw.fox;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.fox.data.FoxParameter;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.jena.riot.Lang;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;

/**
 *
 * This test works only with a good trained model.
 */
@Deprecated
public class FoxTest {

  public final static Logger LOG = LogManager.getLogger(ServerTest.class);

  public static Map<String, String> getParameter(final String lang) {
    final Map<String, String> parameter = new HashMap<>();
    parameter.put(FoxParameter.Parameter.TYPE.toString(), FoxParameter.Type.TEXT.toString());
    parameter.put(FoxParameter.Parameter.LANG.toString(), lang);
    parameter.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
    parameter.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
    return parameter;
  }

  public void test() {
    String text = "But, Obama was born in Hawaii. ";
    text += text;
    text += text.trim();

    // FOX
    String response = send(text);

    // TEST
    Assert.assertNotNull(response);

    // removes input from response
    response = response.replaceAll(text, "");
    // removes linking
    response = response.replaceAll("Obama\\> ", "");
    response = response.replaceAll("Hawaii\\> ", "");

    LOG.info(response);
    // TEST
    {
      final Pattern pattern = Pattern.compile("Obama");
      final Matcher matcher = pattern.matcher(response);
      int count = 0;
      while (matcher.find()) {
        count++;
      }
      Assert.assertTrue(count == 4);
    }
    {
      final Pattern pattern = Pattern.compile("Hawaii");
      final Matcher matcher = pattern.matcher(response);
      int count = 0;
      while (matcher.find()) {
        count++;
      }
      Assert.assertTrue(count == 4);
    }
  }

  public String send(final String text) {

    PropertiesLoader.setPropertiesFile("fox.properties");

    final String lang = FoxParameter.Langs.EN.toString();
    final Map<String, String> parameter = getParameter(lang);
    final IFox fox = new Fox(lang);
    parameter.put(FoxParameter.Parameter.INPUT.toString(), text);

    fox.setParameter(parameter);
    fox.run();

    return fox.getResultsAndClean();
  }
}
