package com.runningduk.unirun.exceptions;

public class NoSuchRunningDataException extends Exception {
    public NoSuchRunningDataException(int runningDataId) {
        super("Running data with ID " + runningDataId + " not found.");
    }
}
