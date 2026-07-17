package com.gymcrm.service;

import com.gymcrm.exceptions.TraineeNotFoundException;
import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.UserDao;
import com.gymcrm.dao.impl.TraineeDaoImpl;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.User;
import com.gymcrm.validators.TraineeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
    private TraineeDaoImpl traineeDao;
    private UserDao userDao;
    private IdGenerator idGenerator;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeValidator traineeValidator;

    @BeforeEach
    void setUp() {
        traineeDao = mock(TraineeDaoImpl.class);
        userDao = mock(UserDao.class);
        idGenerator = mock(IdGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        traineeValidator = mock(TraineeValidator.class);
        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setUserDao(userDao);
        traineeService.setIdGenerator(idGenerator);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setTraineeValidator(traineeValidator);
    }

    @Test
    void testCreateTrainee_success() {
        when(idGenerator.generateNextId()).thenReturn(10L, 11L);
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("jdoe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.createTrainee(
                "John", "Doe", LocalDate.of(2000, 1, 1), "Tbilisi"
        );

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals(10L, result.getUserId());
        assertEquals("jdoe", result.getUser().getUsername());
        assertEquals("pass123", result.getUser().getPassword());
        verify(userDao, times(1)).save(any(User.class));
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testSelectTrainee_existing() {
        Trainee trainee = new Trainee();
        trainee.setId(5L);
        trainee.setUserId(50L);
        when(traineeDao.findById(5L)).thenReturn(Optional.of(trainee));
        when(userDao.findById(50L)).thenReturn(Optional.of(new User("A", "B", "a.b", "p", true, 50L)));

        Trainee result = traineeService.select(5L);
        assertEquals(5L, result.getId());
        assertNotNull(result.getUser());
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
        t.setUserId(10L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(t));

        traineeService.deleteTrainee(1L);
        verify(traineeDao, times(1)).delete(1L);
        verify(userDao, times(1)).delete(10L);
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
        t.setUserId(10L);
        User user = new User("Old", "Name", "old.name", "p", true, 10L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(t));
        when(userDao.findById(10L)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee updated = traineeService
                .updateTrainee(1L, "Jane", "Doe", LocalDate.of(2001, 1, 1), "Batumi", true);

        assertEquals("Jane", updated.getUser().getFirstName());
        assertTrue(updated.getUser().isActive());
        assertEquals("Batumi", updated.getAddress());
        verify(userDao, times(1)).update(any(User.class));
        verify(traineeDao, times(1)).update(any(Trainee.class));
    }

    @Test
    void testCreateTrainee_invalidInput() {
        doThrow(new IllegalArgumentException("Invalid trainee"))
                .when(traineeValidator)
                .validateTrainee(any(), any(), any(), any());

        assertThrows(IllegalArgumentException.class, () ->
                traineeService.createTrainee(null, "Doe", LocalDate.of(2000, 1, 1), "Tbilisi"));
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
                traineeService.updateTrainee(null, "John", "Doe", LocalDate.of(2000, 1, 1), "Tbilisi", true));
    }

    @Test
    void testMatchTraineeCredentials_success() {
        User user = new User("John", "Doe", "jdoe", "pass123", true, 10L);
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUserId(10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertTrue(traineeService.matchTraineeCredentials("jdoe", "pass123"));
    }

    @Test
    void testMatchTraineeCredentials_wrongPassword() {
        User user = new User("John", "Doe", "jdoe", "pass123", true, 10L);
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUserId(10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertFalse(traineeService.matchTraineeCredentials("jdoe", "wrong"));
    }
}
