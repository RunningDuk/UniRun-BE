package com.runningduk.unirun.exceptions;

public class DuplicateAttendingException extends Exception {
    public DuplicateAttendingException(String runningScheduleId) {
        super("Attendee has already registered for the running schedule with ID: " + runningScheduleId);
    }
}
