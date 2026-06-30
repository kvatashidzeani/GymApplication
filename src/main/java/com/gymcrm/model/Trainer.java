package com.gym.crm.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Trainer extends User {
    private TrainingType specialization;

    public Trainer(Long id, String firstName, String lastName, String username, String password, boolean isActive,TrainingType specialization) {
        super(id, firstName, lastName, username, password, isActive);
        this.specialization = specialization;
    }
    public Trainer(){
        super();
    }

}