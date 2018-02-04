package org.aksw.fox.legacy.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.data.FoxParameter.Langs;
import org.aksw.fox.utils.FoxLanguageDetector;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentCreator;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Deprecated
public class ApiUtil {

  public static Logger LOG = LogManager.getLogger(ApiUtil.class);

  protected final FoxLanguageDetector languageDetector = new FoxLanguageDetector();

  protected final TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
  protected final TurtleNIFDocumentCreator creator = new TurtleNIFDocumentCreator();
  protected final TurtleNIFParser turtleNIFParser = new TurtleNIFParser();
  protected final NIFWriter turtleNIFWriter = new TurtleNIFWriter();

  public static void main(final String[] args) throws IOException {
    final ApiUtil a = new ApiUtil();
    final String file = "/home/rspeck/Desktop/task1.ttl";
    final List<String> lines = Files.readAllLines(Paths.get(file));
    final String c = String.join("\n", lines);
    a.parseNIF(c).forEach(LOG::info);;
  }

  public Langs detectLanguage(final String text) {
    return languageDetector.detect(text);
  }

  public String writeNIF(final List<Document> docs) {
    return turtleNIFWriter.writeNIF(docs);
  }

  public List<Document> parseNIF(final String nif) {
    return turtleNIFParser.parseNIF(nif);
  }

  public List<Document> parseNIF(final InputStream in) {
    return turtleNIFParser.parseNIF(in);
  }

  /**
   *
   * @param formData
   * @return
   */
  protected boolean checkParameter(final Map<String, String> formData) {

    LOG.info("checking form parameter ...");

    final String type = formData.get(FoxParameter.Parameter.TYPE.toString());
    if ((type == null) || !(type.equalsIgnoreCase(FoxParameter.Type.URL.toString())
        || type.equalsIgnoreCase(FoxParameter.Type.TEXT.toString()))) {
      return false;
    }

    final String text = formData.get(FoxParameter.Parameter.INPUT.toString());
    if ((text == null) || text.trim().isEmpty()) {
      return false;
    }

    final String task = formData.get(FoxParameter.Parameter.TASK.toString());
    if ((task == null) || !(task.equalsIgnoreCase(FoxParameter.Task.KE.toString())
        || task.equalsIgnoreCase(FoxParameter.Task.NER.toString())
        || task.equalsIgnoreCase(FoxParameter.Task.RE.toString())
        || task.equalsIgnoreCase(FoxParameter.Task.ALL.toString()))) {
      return false;
    }

    final String output = formData.get(FoxParameter.Parameter.OUTPUT.toString());
    if (FoxParameter.Output.fromString(output) == null) {
      LOG.warn("Not found output parameter:" + output);
      return false;
    }

    final String foxlight = formData.get(FoxParameter.Parameter.FOXLIGHT.toString());
    if ((foxlight == null) || foxlight.equalsIgnoreCase("off")) {
      formData.put(FoxParameter.Parameter.FOXLIGHT.toString(), "OFF");
    }

    LOG.info("ok.");
    return true;
  }
}
