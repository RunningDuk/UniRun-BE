package com.runningduk.unirun.exceptions;

public class CreatorCannotCancelException extends Exception {
    public CreatorCannotCancelException() {
        super("The creator of the schedule cannot cancel their own participation.");
    }
}
