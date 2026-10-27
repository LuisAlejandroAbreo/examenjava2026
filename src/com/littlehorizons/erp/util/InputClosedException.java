package com.littlehorizons.erp.util;

/** Thrown when standard input is closed (end of stream), so the application can exit cleanly. */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException(Throwable cause) {
        super("Entrada cerrada", cause);
    }
}
