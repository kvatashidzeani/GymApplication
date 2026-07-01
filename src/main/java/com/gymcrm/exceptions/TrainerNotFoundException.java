package com.gymcrm.exceptions;

public class TrainerNotFoundException extends RuntimeException {
    private static final String message = "Trainer requested does not exist.";

    public TrainerNotFoundException(){
        super(message);
    }
    public TrainerNotFoundException(String info){
        super(message + " " + info);
    }
}