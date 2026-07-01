package com.gymcrm;


import com.gymcrm.config.AppConfig;

import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymApplication {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);
        facade.createTrainee("Ani","Kvatashidze", LocalDate.of(2005,6,9),"Gora");
        TrainingType kungfu = new TrainingType("Kung-fu",1L);
        facade.createTrainer("Giorgi","Janelidze",kungfu);
        System.out.println(facade.selectAllTrainees());
        System.out.println(facade.selectAllTrainers());

        System.out.println("\n");
        System.out.println("**************** Now to test the loaders ******************************");
        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);
        TrainerStorage trainerStorage = context.getBean(TrainerStorage.class);
        TrainingStorage trainingStorage = context.getBean(TrainingStorage.class);

        System.out.println("====== Trainees ======");
        traineeStorage.getStorage().forEach((id, t) -> System.out.println(id + " -> " + t.getUsername()));

        System.out.println("\n====== Trainers ======");
        trainerStorage.getStorage().forEach((id, t) -> System.out.println(id + " -> " + t.getUsername()));

        System.out.println("\n====== Trainings ======");
        trainingStorage.getStorage().forEach((id, t) ->
                System.out.println(id + " -> " + t.getTrainingName() + " (" + t.getTrainingType().getTrainingTypeName() + ")")
        );

        context.close();
    }
}