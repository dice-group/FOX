package org.aksw.fox;

import gnu.getopt.Getopt;

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
    /**
     * The main method.
     * 
     * @param args
     */
    public static void main(String[] args) {

        PropertyConfigurator.configure("log4j.properties");

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
