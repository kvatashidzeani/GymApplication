package com.gymcrm.workload.storage;

import com.gymcrm.workload.model.MonthWorkload;
import com.gymcrm.workload.model.TrainerWorkload;
import com.gymcrm.workload.model.YearWorkload;
import com.gymcrm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MongoWorkloadStorageTest {

    private TrainerWorkloadRepository repository;
    private MongoWorkloadStorage storage;

    @BeforeEach
    void setUp() {
        repository = mock(TrainerWorkloadRepository.class);
        storage = new MongoWorkloadStorage(repository);
    }

    @Test
    void findByUsername_delegatesToRepositorySearch() {
        TrainerWorkload workload = sampleWorkload();
        when(repository.findByTrainerUsername("Mike.Brown")).thenReturn(Optional.of(workload));

        Optional<TrainerWorkload> found = storage.findByUsername("Mike.Brown");

        assertTrue(found.isPresent());
        assertEquals("Mike.Brown", found.get().getTrainerUsername());
        verify(repository).findByTrainerUsername("Mike.Brown");
    }

    @Test
    void save_newTrainer_insertsDocument() {
        TrainerWorkload workload = sampleWorkload();
        when(repository.existsByTrainerUsername("Mike.Brown")).thenReturn(false);
        when(repository.save(workload)).thenReturn(workload);

        TrainerWorkload saved = storage.save(workload);

        assertEquals("Mike.Brown", saved.getTrainerUsername());
        verify(repository).save(workload);
        verify(repository, never()).updateByTrainerUsername(any());
    }

    @Test
    void save_existingTrainer_updatesByUsername() {
        TrainerWorkload workload = sampleWorkload();
        when(repository.existsByTrainerUsername("Mike.Brown")).thenReturn(true);
        when(repository.updateByTrainerUsername(workload)).thenReturn(workload);

        TrainerWorkload saved = storage.save(workload);

        assertEquals("Mike.Brown", saved.getTrainerUsername());
        verify(repository).updateByTrainerUsername(workload);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_delegatesToRepositoryDeleteByUsername() {
        storage.delete("Mike.Brown");

        verify(repository).deleteByTrainerUsername("Mike.Brown");
        verify(repository, never()).save(any());
    }

    private static TrainerWorkload sampleWorkload() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Brown", "Mike", "Brown", true);
        YearWorkload year = new YearWorkload(2026);
        year.getMonths().add(new MonthWorkload(8, 60));
        workload.getYears().add(year);
        return workload;
    }
}
