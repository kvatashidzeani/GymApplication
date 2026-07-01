package com.gymcrm.Loader;

import com.gymcrm.storage.StorageInitializer;
import org.springframework.stereotype.Component;

@Component
public class SeedDataContext {

    private StorageInitializer.SeedData seedData;

    public StorageInitializer.SeedData getSeedData() {
        return seedData;
    }

    public void setSeedData(StorageInitializer.SeedData seedData) {
        this.seedData = seedData;
    }
}