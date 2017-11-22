package org.aksw.fox.tools.linking.nl;

import java.io.IOException;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.fox.tools.linking.common.Agdistis;

/**
 * This class uses the Agdistis lib.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisDirectNL extends Agdistis {

  final String file = "agdistisNL.properties";
  NEDAlgo_HITS agdistis = null;

  public AgdistisDirectNL() {
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
