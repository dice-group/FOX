package org.aksw.fox.tools.linking.fr;

import java.io.IOException;

import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.fox.exception.LoadingNotPossibleException;
import org.aksw.fox.tools.linking.common.Agdistis;
import org.aksw.fox.utils.FoxCfg;

/**
 * This class uses the Agdistis lib.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisDirectFR extends Agdistis {

  final String file = "agdistisFR.properties";
  NEDAlgo_HITS agdistis = null;

  public AgdistisDirectFR() {
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

  public static void main(final String[] a) throws LoadingNotPossibleException {
    FoxCfg.getClass("org.aksw.fox.tools.linking.fr.AgdistisDirectFR");
  }
}
