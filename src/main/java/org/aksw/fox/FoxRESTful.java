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
     */
    public static void main(String[] args) {

        if (FoxCfg.loadFile(FoxCfg.CFG_FILE))
            try {
                new Server().start();
            } catch (
                    IllegalArgumentException | InvocationTargetException |
                    NoSuchMethodException | SecurityException |
                    LoadingNotPossibleException | UnsupportedLangException |
                    PortInUseException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
    }
}
