package com.gymcrm.security;

import com.gymcrm.dao.UserDao;
import com.gymcrm.dao.impl.TraineeDaoImpl;
import com.gymcrm.dao.impl.TrainerDaoImpl;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymUserDetailsServiceTest {

    private UserDao userDao;
    private TraineeDaoImpl traineeDao;
    private TrainerDaoImpl trainerDao;
    private LoginAttemptService loginAttemptService;
    private GymUserDetailsService service;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        traineeDao = mock(TraineeDaoImpl.class);
        trainerDao = mock(TrainerDaoImpl.class);
        loginAttemptService = mock(LoginAttemptService.class);
        service = new GymUserDetailsService(userDao, traineeDao, trainerDao, loginAttemptService);
    }

    @Test
    void loadUserByUsername_trainee_hasTraineeRole() {
        User user = new User("Ani", "Smith", "Ani.Smith", "secret", true, 1L);
        Trainee trainee = new Trainee(10L, null, null, 1L);
        when(loginAttemptService.isBlocked("Ani.Smith")).thenReturn(false);
        when(userDao.findByUsername("Ani.Smith")).thenReturn(Optional.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(trainerDao.findAll()).thenReturn(List.of());

        UserDetails details = service.loadUserByUsername("Ani.Smith");

        assertEquals("Ani.Smith", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINEE")));
    }

    @Test
    void loadUserByUsername_inactive_disabled() {
        User user = new User("Ani", "Smith", "Ani.Smith", "secret", false, 1L);
        when(loginAttemptService.isBlocked("Ani.Smith")).thenReturn(false);
        when(userDao.findByUsername("Ani.Smith")).thenReturn(Optional.of(user));
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());

        UserDetails details = service.loadUserByUsername("Ani.Smith");

        assertFalse(details.isEnabled());
    }

    @Test
    void loadUserByUsername_missing_throws() {
        when(loginAttemptService.isBlocked("Missing.User")).thenReturn(false);
        when(userDao.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("Missing.User"));
    }

    @Test
    void loadUserByUsername_trainer_hasTrainerRole() {
        User user = new User("Mike", "Brown", "Mike.Brown", "pass", true, 2L);
        Trainer trainer = new Trainer(20L, null, 2L);
        when(loginAttemptService.isBlocked("Mike.Brown")).thenReturn(false);
        when(userDao.findByUsername("Mike.Brown")).thenReturn(Optional.of(user));
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        UserDetails details = service.loadUserByUsername("Mike.Brown");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER")));
    }

    @Test
    void loadUserByUsername_whenBlocked_throwsLockedException() {
        when(loginAttemptService.isBlocked("Ani.Smith")).thenReturn(true);

        assertThrows(LockedException.class, () -> service.loadUserByUsername("Ani.Smith"));
        verify(userDao, never()).findByUsername(any());
    }
}
