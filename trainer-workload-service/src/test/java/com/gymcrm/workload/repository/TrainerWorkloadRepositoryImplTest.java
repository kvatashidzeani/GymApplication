package com.gymcrm.workload.repository;

import com.gymcrm.workload.model.MonthWorkload;
import com.gymcrm.workload.model.TrainerWorkload;
import com.gymcrm.workload.model.YearWorkload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private TrainerWorkloadRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TrainerWorkloadRepositoryImpl(mongoTemplate);
    }

    @Test
    void updateByTrainerUsername_findAndReplaceOnId() {
        TrainerWorkload workload = sampleWorkload();
        when(mongoTemplate.findAndReplace(any(Query.class), eq(workload), any(FindAndReplaceOptions.class)))
                .thenReturn(workload);

        TrainerWorkload updated = repository.updateByTrainerUsername(workload);

        assertEquals("Mike.Brown", updated.getTrainerUsername());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).findAndReplace(
                queryCaptor.capture(),
                eq(workload),
                any(FindAndReplaceOptions.class));
        assertEquals("{ \"_id\" : \"Mike.Brown\"}", queryCaptor.getValue().getQueryObject().toJson());
    }

    @Test
    void updateByTrainerUsername_missingUsername_throws() {
        TrainerWorkload workload = new TrainerWorkload();
        assertThrows(IllegalArgumentException.class, () -> repository.updateByTrainerUsername(workload));
    }

    private static TrainerWorkload sampleWorkload() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Brown", "Mike", "Brown", true);
        YearWorkload year = new YearWorkload(2026);
        year.getMonths().add(new MonthWorkload(8, 60));
        workload.getYears().add(year);
        return workload;
    }
}
