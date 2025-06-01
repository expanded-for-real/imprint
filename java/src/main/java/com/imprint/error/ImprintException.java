package com.imprint.error;

/**
 * Exception thrown by Imprint operations.
 */
public class ImprintException extends Exception {
    private final ErrorType errorType;
    
    public ImprintException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public ImprintException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public ErrorType getErrorType() { 
        return errorType; 
    }
    
    @Override
    public String toString() {
        return String.format("ImprintException{type=%s, message='%s'}", errorType, getMessage());
    }
}
