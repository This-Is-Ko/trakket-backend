package org.trakket.exception;

public class OtpSendException extends RuntimeException {
    public OtpSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
