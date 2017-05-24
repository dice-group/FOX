package org.aksw.fox.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * FileHelper with static methods.
 *
 * @author rspeck
 *
 */
public class FileUtil {

  public static final Logger LOG = LogManager.getLogger(FileUtil.class);

  public static BufferedReader gunzipIt(final Path zipPath) {
    return gunzipIt(zipPath.toAbsolutePath().toString());
  }

  public static List<String> gunzipItToList(final String pathToFile) {
    return _read(gunzipIt(pathToFile), "");
  }

  public static BufferedReader gunzipIt(final String zipFile) {
    final byte[] buffer = new byte[1024];
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      final GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(zipFile));
      int len;
      while ((len = gzis.read(buffer)) > 0) {
        baos.write(buffer, 0, len);
      }
      gzis.close();
      baos.close();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return new BufferedReader(new StringReader(baos.toString()));
  }

  /**
   * Opens a BufferedReader to read a file.
   *
   * @param pathToFile path to the file
   * @return BufferedReader
   */
  public static BufferedReader openFileToRead(final String pathToFile) {
    return openFileToRead(pathToFile, "UTF-8");
  }

  /**
   * Opens a BufferedReader to read a file.
   *
   * @param pathToFile path to the file
   * @param encoding used encoding (e.g.,"UTF-8")
   * @return BufferedReader
   */
  public static BufferedReader openFileToRead(final String pathToFile, final String encoding) {
    try {
      return Files.newBufferedReader(new File(pathToFile).toPath(), Charset.forName(encoding));
    } catch (final Exception e) {
      LOG.error("\n", e);
      return null;
    }
  }

  /**
   *
   * @param file
   * @return
   */
  public static BufferedReader openBZip2File(final String file) {
    String s = "";
    try {
      final FileInputStream in = new FileInputStream(file);
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
      final byte[] buffer = new byte[1028];
      int n = 0;
      while (-1 != (n = bzIn.read(buffer))) {
        out.write(buffer, 0, n);
      }
      out.close();
      bzIn.close();
      s = out.toString();
    } catch (final Exception e) {
      LOG.error("\n", e);
      return null;
    }
    return new BufferedReader(new StringReader(s));
  }

  /**
   * Opens a BufferedWriter to write a file with encoding utf-8.
   *
   * @param pathToFile path to the file
   * @return BufferedWriter
   */
  public static BufferedWriter openFileToWrite(final String pathToFile) {
    return openFileToWrite(pathToFile, "UTF-8");
  }

  /**
   * Opens a BufferedWriter to write a file.
   *
   * @param pathToFile path to the file
   * @param encoding used encoding (e.g.,"UTF-8")
   * @return BufferedWriter
   */
  public static BufferedWriter openFileToWrite(final String pathToFile, final String encoding) {
    try {
      return Files.newBufferedWriter(new File(pathToFile).toPath(), Charset.forName(encoding));
    } catch (final IOException e) {
      LOG.error("\n", e);
      return null;
    }
  }

  /**
   * Reads a file to List.
   *
   * @param pathToFile path to the file
   * @param commentSymbol a line in the given file starting with the commentSymbole will be ignored
   * @return list of lines
   */
  public static List<String> fileToList(final String pathToFile, final String commentSymbol) {
    return fileToList(pathToFile, "UTF-8", commentSymbol);
  }

  public static List<String> bzip2ToList(final String pathToFile) {
    return bzip2ToList(pathToFile, "");
  }

  public static List<String> bzip2ToList(final String pathToFile, final String commentSymbol) {
    return _read(openBZip2File(pathToFile), commentSymbol);
  }

  /**
   * Reads a file to List.
   *
   * @param pathToFile path to the used file
   * @return list of lines
   */
  public static List<String> fileToList(final String pathToFile) {
    return fileToList(pathToFile, "UTF-8", "");
  }

  /**
   * Reads a file to List.
   *
   * @param pathToFile path to the used file
   * @param encoding used encoding (e.g.,"UTF-8")
   * @param commentSymbol a line in the given file starting with the commentSymbole will be ignored
   * @return list of lines
   */
  public static List<String> fileToList(final String pathToFile, final String encoding,
      final String commentSymbol) {
    return _read(openFileToRead(pathToFile, encoding), commentSymbol);
  }

  private static List<String> _read(final BufferedReader br, final String commentSymbol) {
    final List<String> results = new ArrayList<String>();
    try {
      String line;
      while ((line = br.readLine()) != null) {
        if (!commentSymbol.isEmpty() && line.startsWith(commentSymbol)) {

        } else {
          results.add(line);
        }
      }
      br.close();
    } catch (final IOException e) {
      LOG.error("\n", e);
    }
    return results;

  }

  /**
   *
   * Downloads and copies to file.
   *
   * @param url source to download and copy
   * @param file path to the file
   */
  public static void download(final URL url, final String file) {
    download(url, new File(file));
  }

  /**
   *
   * Downloads and copies to file.
   *
   * @param url source to download and copy
   * @param file path to the file
   */
  public static void download(final URL url, final File file) {
    if (!fileExists(file)) {
      try {
        FileUtils.copyURLToFile(url, file);
      } catch (final IOException e) {
        final String msg = "" + "\n Error while downloading " + url.toString() + " and copying to "
            + file.toString();
        LOG.error(msg, e);
      }
    }
  }

  /**
   * Checks if a file exists.
   *
   * @param file
   * @return true if the file exists.
   */
  public static boolean fileExists(final String file) {
    return fileExists(new File(file));
  }

  /**
   * Checks if a file exists.
   *
   * @param file
   * @return true if the file exists.
   */
  public static boolean fileExists(final File file) {
    if (file.exists() && !file.isDirectory()) {
      LOG.debug("File " + file.toString() + " exists.");
      return true;
    }
    return false;
  }
}
