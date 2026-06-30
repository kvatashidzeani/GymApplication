package com.gym.crm.model;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType{
    private Long id;
    private String name;
    public TrainingType(String name){
        this.name = name;
    }

}