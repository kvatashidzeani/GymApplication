package com.gym.crm.Loader;

import com.gym.crm.storage.StorageInitializer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SeedDataContext {
    private StorageInitializer.SeedData seedData;

}