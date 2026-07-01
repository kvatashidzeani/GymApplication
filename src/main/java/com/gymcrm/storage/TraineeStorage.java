    package com.gymcrm.storage;

    import com.gymcrm.model.Trainee;
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