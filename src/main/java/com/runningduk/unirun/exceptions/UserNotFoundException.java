package com.runningduk.unirun.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String userId) {
        super(userId);
    }
}
