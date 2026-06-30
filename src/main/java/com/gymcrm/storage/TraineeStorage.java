    package com.gym.crm.storage;

    import com.gym.crm.model.Trainee;
    import org.springframework.stereotype.Component;

    import java.util.HashMap;
    import java.util.Map;

    @Component
    public class TraineeStorage{
        private Map<Long, Trainee> traineeMap = new HashMap<>();


        public Map<Long,Trainee> getStorage(){
            return traineeMap;
        }

    }