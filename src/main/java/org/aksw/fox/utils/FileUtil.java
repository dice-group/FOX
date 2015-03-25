package org.aksw.fox.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Opens a BufferedReader to read a file.
     * 
     * @param pathToFile
     *            path to the file
     * @return BufferedReader
     */
    public static BufferedReader openFileToRead(String pathToFile) {
        return openFileToRead(pathToFile, "UTF-8");
    }

    /**
     * Opens a BufferedReader to read a file.
     * 
     * @param pathToFile
     *            path to the file
     * @param encoding
     *            used encoding (e.g.,"UTF-8")
     * @return BufferedReader
     */
    public static BufferedReader openFileToRead(String pathToFile, String encoding) {
        try {
            return Files.newBufferedReader(
                    new File(pathToFile).toPath(),
                    Charset.forName(encoding));
        } catch (IOException e) {
            LOG.error("\n", e);
            return null;
        }
    }

    /**
     * Opens a BufferedWriter to write a file.
     * 
     * @param pathToFile
     *            path to the file
     * @return BufferedWriter
     */
    public static BufferedWriter openFileToWrite(String pathToFile) {
        return openFileToWrite(pathToFile, "UTF-8");
    }

    /**
     * Opens a BufferedWriter to write a file.
     * 
     * @param pathToFile
     *            path to the file
     * @param encoding
     *            used encoding (e.g.,"UTF-8")
     * @return BufferedWriter
     */
    public static BufferedWriter openFileToWrite(String pathToFile, String encoding) {
        try {
            return Files.newBufferedWriter(
                    new File(pathToFile).toPath(),
                    Charset.forName(encoding));
        } catch (IOException e) {
            LOG.error("\n", e);
            return null;
        }
    }

    /**
     * Reads a file to List.
     * 
     * @param pathToFile
     *            path to the file
     * @param commentSymbol
     *            a line in the given file starting with the commentSymbole will
     *            be ignored
     * @return list of lines
     */
    public static List<String> fileToList(String pathToFile, String commentSymbol) {
        return fileToList(pathToFile, "UTF-8", commentSymbol);
    }

    /**
     * Reads a file to List.
     * 
     * @param pathToFile
     *            path to the used file
     * @return list of lines
     */
    public static List<String> fileToList(String pathToFile) {
        return fileToList(pathToFile, "UTF-8", "");
    }

    /**
     * Reads a file to List.
     * 
     * @param pathToFile
     *            path to the used file
     * @param encoding
     *            used encoding (e.g.,"UTF-8")
     * @param commentSymbol
     *            a line in the given file starting with the commentSymbole will
     *            be ignored
     * @return list of lines
     */
    public static List<String> fileToList(String pathToFile, String encoding, String commentSymbol) {
        BufferedReader br = openFileToRead(pathToFile, encoding);
        List<String> results = new ArrayList<String>();
        try {
            String line;
            while ((line = br.readLine()) != null)
                if (!commentSymbol.isEmpty() && !line.startsWith(commentSymbol))
                    results.add(line);
            br.close();
        } catch (IOException e) {
            LOG.error("\n", e);
        }
        return results;
    }

    /**
     * 
     * Downloads and copies to file.
     * 
     * @param url
     *            source to download and copy
     * @param file
     *            path to the file
     */
    public static void download(URL url, String file) {
        download(url, new File(file));
    }

    /**
     * 
     * Downloads and copies to file.
     * 
     * @param url
     *            source to download and copy
     * @param file
     *            path to the file
     */
    public static void download(URL url, File file) {
        if (!fileExists(file)) {
            try {
                org.apache.commons.io.FileUtils.copyURLToFile(url, file);
            } catch (IOException e) {
                String msg = "" +
                        "\n Error while downloading "
                        + url.toString()
                        + " and copying to "
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
    public static boolean fileExists(String file) {
        return fileExists(new File(file));
    }

    /**
     * Checks if a file exists.
     * 
     * @param file
     * @return true if the file exists.
     */
    public static boolean fileExists(File file) {
        if (file.exists() && !file.isDirectory()) {
            LOG.debug("File " + file.toString() + " exists.");
            return true;
        }
        return false;
    }
}
