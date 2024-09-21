package com.runningduk.unirun.exceptions;

public class NotParticipatingException extends Exception {
    public NotParticipatingException(String runningScheduleId) {
        super("You cannot cancel participation because you are not currently participating in the schedule with ID: " + runningScheduleId);
    }
}