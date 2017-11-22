package org.aksw.fox.tools.linking.de;

import java.io.IOException;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.fox.tools.linking.common.Agdistis;

public class AgdistisDirectDE extends Agdistis {

  final String file = "agdistisDE.properties";
  NEDAlgo_HITS agdistis = null;

  public AgdistisDirectDE() {
    try {
      agdistis = new NEDAlgo_HITS(file);
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected String send(final String text) throws Exception {
    return standardAG(text, agdistis);
  }

  /**
   * <code>
    public static void main(final String[] a) throws IOException {

      final String file = "agdistisDE.properties";
      final NEDAlgo_HITS agdistis = new NEDAlgo_HITS(file);
      final String preAnnotatedText =
          "<entity>Angela Dorothea Merkel</entity> ist eine deutsche Politikerin (CDU) in der <entity>Bundesrepublik Deutschland</entity>.";

      String result = "";
      final String text = preAnnotatedText;

      result = standardAG(text, agdistis);

      LOG.info(result);
    }</code>
   */

}
