package com.gymcrm.exceptions;

public class TraineeNotFoundException extends RuntimeException{
    public TraineeNotFoundException() {
        super("Trainee not found");
    }

    public TraineeNotFoundException(String message) {
        super(message);
    }

    public TraineeNotFoundException(Long id) {
        super("Trainee not found with id: " + id);
    }
}