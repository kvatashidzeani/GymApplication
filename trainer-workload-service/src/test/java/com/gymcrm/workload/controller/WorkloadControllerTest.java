package com.gymcrm.workload.controller;

import com.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.gymcrm.workload.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkloadController(workloadService)).build();
    }

    @Test
    void workloadUsage_returnsActiveMqHint() throws Exception {
        mockMvc.perform(get("/workload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("ActiveMQ")));
    }

    @Test
    void getWorkload_returnsTrainerSummary() throws Exception {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setTrainerUsername("Mike.Brown");
        when(workloadService.getWorkload(eq("Mike.Brown"), isNull(), isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/workload/Mike.Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Mike.Brown"));
    }
}
