package com.codebits.d4m;

public class D4MException extends RuntimeException {

    public D4MException() {
    }

    public D4MException(String message) {
        super(message);
    }

    public D4MException(String message, Throwable cause) {
        super(message, cause);
    }

    public D4MException(Throwable cause) {
        super(cause);
    }

}
