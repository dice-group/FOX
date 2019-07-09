package org.aksw.fox.examples;

import java.io.IOException;

import org.aksw.fox.ui.FoxCLI;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Training {
  public static Logger LOG = LogManager.getLogger(Training.class);

  public static void main(final String[] a) throws IOException {

    // Example
    final String[] args = new String[] {"-l", "en", "-i", "input/2", "-a", "train"};
    FoxCLI.main(args);
  }
}
