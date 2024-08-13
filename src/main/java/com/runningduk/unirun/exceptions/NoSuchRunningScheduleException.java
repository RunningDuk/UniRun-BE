package com.runningduk.unirun.exceptions;

public class NoSuchRunningScheduleException extends Exception {
    public NoSuchRunningScheduleException(int runningScheduleId) {
        super("Running schedule with ID " + runningScheduleId + " not found.");
    }
}
