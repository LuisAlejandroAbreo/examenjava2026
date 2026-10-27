package com.littlehorizons.erp.util;

/** Application-level checked exception used for business and data access errors. */
public class ErpException extends Exception {

    private static final long serialVersionUID = 1L;

    public ErpException(String message) {
        super(message);
    }

    public ErpException(String message, Throwable cause) {
        super(message, cause);
    }
}
