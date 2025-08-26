package org.sportstracker.exception;

public class OtpSendException extends RuntimeException {
    public OtpSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
