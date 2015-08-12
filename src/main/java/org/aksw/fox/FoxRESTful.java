package org.aksw.fox;

import java.lang.reflect.InvocationTargetException;

import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.aksw.fox.data.exception.PortInUseException;
import org.aksw.fox.data.exception.UnsupportedLangException;
import org.aksw.fox.utils.FoxCfg;
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
public class FoxRESTful {

    static {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    }

    public static Logger LOG = LogManager.getLogger(FoxRESTful.class);

    /**
     * Starts FOX web service.
     * 
     * @param args
     *            <p>
     *            -p port, default is 8080
     *            </p>
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     * @throws PortInUseException
     * @throws UnsupportedLangException
     * @throws LoadingNotPossibleException
     */
    public static void main(String[] args)
            throws NumberFormatException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException,
            PortInUseException, LoadingNotPossibleException, UnsupportedLangException {

        /*
        final Getopt getopt = new Getopt("Fox", args, "p:x");

        int arg, port = 8080;
        while ((arg = getopt.getopt()) != -1) {
            switch (arg) {
            case 'p':
                port = Integer.valueOf(getopt.getOptarg());
            }
        }
        */
        if (FoxCfg.loadFile(FoxCfg.CFG_FILE))
            new Server().start();
    }
}
