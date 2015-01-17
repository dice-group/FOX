package org.aksw.fox.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

/**
 * Static class to provide general 'server' functionality.
 * 
 * @author rspeck
 * 
 */
public class FoxServerUtil {

    public static Logger logger = Logger.getLogger(FoxServerUtil.class);

    private FoxServerUtil() {
    }

    /**
     * Checks if the port is in use.
     * 
     * @param port
     *            e.g.: 8080
     * @return true if port is free else false
     */
    public static synchronized boolean isPortAvailable(int port) {

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            // logger.error("\n", e);
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    // logger.error("\n", e);
                }
            }
        }
        return false;
    }

    /**
     * Gives the applications process id.
     * 
     * @return applications process id
     */
    public static synchronized String getProcessId() {

        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        if (index < 1)
            return null;
        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Writes a system depended file to shut down the application with kill cmd
     * and process id.
     * 
     * @return true if the file was written
     */
    public static synchronized boolean writeShutDownFile(String fileName) {

        // get process Id
        String id = FoxServerUtil.getProcessId();
        if (id == null)
            return false;

        String cmd = "";
        String fileExtension = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            cmd = "taskkill /F /PID " + id + System.getProperty("line.separator") + "DEL " + fileName + ".bat";
            fileExtension = "bat";
        } else {
            cmd = "kill " + id + System.getProperty("line.separator") + "rm " + fileName + ".sh";
            fileExtension = "sh";
        }
        logger.info(fileName + "." + fileExtension);

        File file = new File(fileName + "." + fileExtension);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(cmd);
            out.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        file.setExecutable(true, false);
        file.deleteOnExit();
        return true;
    }
}
