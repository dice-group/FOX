package org.aksw.fox.data;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface IData {
  Logger LOG = LogManager.getLogger(IData.class);

  String getToolName();
}
