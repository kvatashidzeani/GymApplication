package org.example.ServiceTests;


import com.gym.crm.exceptions.TraineeNotFoundException;
import com.gym.crm.Util.IdGenerator;
import com.gym.crm.Util.PasswordGenerator;
import com.gym.crm.Util.UsernameGenerator;
import com.gym.crm.dao.impl.TraineeDaoImpl;
import com.gym.crm.model.Trainee;
import com.gym.crm.service.TraineeService;
import com.gym.crm.validators.TraineeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
    private TraineeDaoImpl traineeDao;
    private IdGenerator idGenerator;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeValidator traineeValidator;

    @BeforeEach
    void setUp() {
        traineeDao = mock(TraineeDaoImpl.class);
        idGenerator = mock(IdGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        traineeValidator = mock(TraineeValidator.class);
        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setIdGenerator(idGenerator);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setTraineeValidator(traineeValidator);
    }

    @Test
    void testCreateTrainee_success() {
        when(idGenerator.generateNextId()).thenReturn(10L);
        when(usernameGenerator.generateUsername("Ani", "Smith")).thenReturn("asmth");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");

        Trainee traineeToSave = new Trainee();
        traineeToSave.setId(10L);
        traineeToSave.setFirstName("Ani");
        traineeToSave.setLastName("Smith");
        traineeToSave.setUsername("asmth");
        traineeToSave.setPassword("pass123");
        traineeToSave.setActive(true);
        traineeToSave.setDateOfBirth(LocalDate.of(2000,1,1));
        traineeToSave.setAddress("Tbilisi");
        when(traineeDao.save(any(Trainee.class))).thenReturn(traineeToSave);

        Trainee result = traineeService.createTrainee(
                "Ani", "Smith", LocalDate.of(2000,1,1), "Tbilisi"
        );

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("asmth", result.getUsername());
        assertEquals("pass123", result.getPassword());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testSelectTrainee_existing() {
        Trainee trainee = new Trainee();
        trainee.setId(5L);
        when(traineeDao.findById(5L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.select(5L);
        assertEquals(5L, result.getId());
    }

    @Test
    void testSelectTrainee_notFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TraineeNotFoundException.class, () -> traineeService.select(1L));
    }

    @Test
    void testSelectAllTrainees() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        Trainee t2 = new Trainee();
        t2.setId(2L);

        when(traineeDao.findAll()).thenReturn(Arrays.asList(t1, t2));

        var result = traineeService.selectAllTrainees();
        assertEquals(2, result.size());
    }

    @Test
    void testDeleteTrainee_success() {
        Trainee t = new Trainee();
        t.setId(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(t));

        traineeService.deleteTrainee(1L);
        verify(traineeDao, times(1)).delete(1L);
    }

    @Test
    void testDeleteTrainee_notFound() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TraineeNotFoundException.class, () -> traineeService.deleteTrainee(99L));
    }

    @Test
    void testUpdateTrainee_success() {
        Trainee t = new Trainee();
        t.setId(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(t));
        when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee updated = traineeService
                .updateTrainee(1L, "Ani", "Smith", LocalDate.of(2001,1,1), "Tbilisi", true);

        assertEquals("Ani", updated.getFirstName());
        assertTrue(updated.isActive());
        verify(traineeDao, times(1)).update(any(Trainee.class));
    }

    @Test
    void testCreateTrainee_invalidInput() {
        doThrow(new IllegalArgumentException("Invalid trainee"))
                .when(traineeValidator)
                .validateTrainee(any(),any(),any(),any());

        assertThrows(IllegalArgumentException.class, () ->
                traineeService.createTrainee(null, "Smith", LocalDate.of(2000,1,1), "Tbilisi"));
    }


    @Test
    void testSelect_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.select(null));
    }

    @Test
    void testDeleteTrainee_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.deleteTrainee(null));
    }

    @Test
    void testUpdateTrainee_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                traineeService.updateTrainee(null, "Ani", "Smith", LocalDate.of(2000,1,1), "Tbilisi", true));
    }
}