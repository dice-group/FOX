package org.aksw.fox;

import gnu.getopt.Getopt;

import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.Server;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Starts FOX web service.
 * 
 * @author rspeck
 * 
 */
public class MainServer {

    static {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    }

    public static Logger LOG = LogManager.getLogger(MainServer.class);

    /**
     * Starts FOX web service.
     * 
     * @param args
     *            <p>
     *            -p port, default is 8080
     *            </p>
     */
    public static void main(String[] args) {

        final Getopt getopt = new Getopt("Fox", args, "p:x");

        int arg, port = 8080;
        while ((arg = getopt.getopt()) != -1) {
            switch (arg) {
            case 'p':
                port = Integer.valueOf(getopt.getOptarg());
            }
        }

        if (!FoxServerUtil.isPortAvailable(port))
            LOG.error("Port " + port + " in use or wrong argument, try another one!");
        else if (FoxCfg.loadFile(FoxCfg.CFG_FILE))
            new Server(port).start();
    }
}
