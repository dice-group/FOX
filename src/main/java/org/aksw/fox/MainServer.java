package org.aksw.fox;

import gnu.getopt.Getopt;

import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxServerUtil;
import org.aksw.fox.web.Server;
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

    /**
     * 
     * @param args
     *            <p>
     *            -p port
     *            </p>
     */
    public static void main(String[] args) {

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

        // test port
        if (FoxServerUtil.isPortAvailable(port)) {

            // test config
            String poolCount = FoxCfg.get(Server.CFG_KEY_POOL_SIZE);
            if (poolCount == null) {
                Server.LOG.error(
                        "Can't read " + Server.CFG_KEY_POOL_SIZE + " key in `" + FoxCfg.CFG_FILE + "` file."
                        );
                System.exit(0);
            }

            // start server
            new Server(port).start();

        }
        else
            Server.LOG.error("Port " + port + " in use or wrong argument, try an other one!");
    }
}
