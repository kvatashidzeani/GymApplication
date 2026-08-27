package com.gymcrm.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkloadController.class)
class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkloadService workloadService;

    @Test
    void updateWorkload_validRequest_returns200() throws Exception {
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setTrainerUsername("Mike.Brown");
        request.setTrainerFirstName("Mike");
        request.setTrainerLastName("Brown");
        request.setIsActive(true);
        request.setTrainingDate(LocalDate.of(2026, 3, 15));
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);

        mockMvc.perform(post("/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(workloadService).applyTrainingEvent(any(WorkloadUpdateRequest.class));
    }

    @Test
    void updateWorkload_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
