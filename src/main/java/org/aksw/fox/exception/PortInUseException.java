package org.aksw.fox.exception;

public class PortInUseException extends Exception {
    private static final long serialVersionUID = 8440331545375107244L;

    public PortInUseException(int port) {
        super("Port " + port + " in use or wrong argument, try another one!");
    }
}
