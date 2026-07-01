package com.gymcrm.exceptions;

public class TrainingNotFoundException extends RuntimeException{
    public TrainingNotFoundException() {
        super("Training not found");
    }

    public TrainingNotFoundException(String message) {
        super(message);
    }

    public TrainingNotFoundException(Long id) {
        super("Training not found with id: " + id);
    }
}