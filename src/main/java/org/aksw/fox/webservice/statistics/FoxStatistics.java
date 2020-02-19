package org.aksw.fox.webservice.statistics;

import java.util.HashMap;
import java.util.Map;

import org.aksw.fox.data.FoxParameter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FoxStatistics {
  public static Logger LOG = LogManager.getLogger(FoxStatistics.class);

  /**
   * Stores client informations in the log file.
   *
   * @param ip
   * @param parameterMap
   */
  public void client(final String _ip, final Map<String, String> parameterMap) {
    String ip = _ip;
    // remove data we do not want in the log
    final Map<String, String> copy = new HashMap<>(parameterMap);
    copy.keySet().retainAll(FoxParameter.allowedHeaderFields());
    // copy.remove(FoxParameter.Parameter.INPUT.toString().toLowerCase());
    copy.remove(FoxParameter.Parameter.NIF.toString().toLowerCase());

    // add data to the log file
    if (ip != null && ip.length() > 1) {
      ip = ip.substring(0, ip.length() / 2);
    }

    LOG.info("Client IP: ".concat(ip).concat("; parameter: ").concat(copy.toString()));
  }
}
