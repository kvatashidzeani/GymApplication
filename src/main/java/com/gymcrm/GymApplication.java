package com.gym.crm;


import com.gym.crm.config.AppConfig;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.storage.TraineeStorage;
import com.gym.crm.storage.TrainerStorage;
import com.gym.crm.storage.TrainingStorage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymApplication {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);
        facade.createTrainee("Davit","Jincharadze", LocalDate.of(2005,7,26),"Javakhishvili");
        TrainingType kungfu = new TrainingType(1L,"Kung-fu");
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
                System.out.println(id + " -> " + t.getTrainingName() + " (" + t.getTrainingType().getName() + ")")
        );

        context.close();
    }
}