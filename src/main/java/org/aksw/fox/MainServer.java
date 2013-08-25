package org.aksw.fox;

import gnu.getopt.Getopt;

import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.Server;
import org.apache.log4j.PropertyConfigurator;

/**
 * A class with a main method to start FOX web service.
 * 
 * @author rspeck
 * 
 */
public class MainServer {

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * The main method.
     * 
     * @param args
     */
    public static void main(String[] args) {

        // test config
        String poolCount = FoxCfg.get("poolCount");
        if (poolCount == null) {
            Server.logger.error("Can't read poolCount key in `fox.properties` file.");
            System.exit(0);
        }

        final Getopt getopt = new Getopt("Fox", args, "p:x");

        int arg, port = 8080;
        while ((arg = getopt.getopt()) != -1) {
            switch (arg) {
            case 'p':
                port = Integer.valueOf(getopt.getOptarg());
                break;
            default:

            }
        }

        if (FoxServerUtil.isPortAvailable(port))
            new Server(port).start();
        else
            Server.logger.error("Port " + port + " in use or wrong argument, try an other one!");
    }
}
