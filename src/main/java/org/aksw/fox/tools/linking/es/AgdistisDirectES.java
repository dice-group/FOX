package org.aksw.fox.tools.linking.es;

import java.io.IOException;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.fox.tools.linking.common.Agdistis;

/**
 * This class uses the Agdistis lib.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisDirectES extends Agdistis {

  final String file = "agdistisES.properties";
  NEDAlgo_HITS agdistis = null;

  public AgdistisDirectES() {
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
}
