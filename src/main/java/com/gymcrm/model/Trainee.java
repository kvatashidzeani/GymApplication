package com.gym.crm.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Trainee extends User {
    private String address;
    private LocalDate dateOfBirth;


    public Trainee(Long id, String firstName, String lastName, String username, String password, boolean isActive,String address,LocalDate dateOfBirth) {
        super(id, firstName, lastName, username, password, isActive);
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    public Trainee(){
        super();
    }


}