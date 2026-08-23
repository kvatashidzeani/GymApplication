package com.gymcrm.dao;

import com.gymcrm.dao.impl.UserDaoImpl;
import com.gymcrm.model.User;
import com.gymcrm.storage.UserStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDaoImplTest {

    private UserStorage userStorage;
    private UserDaoImpl userDao;
    private Map<Long, User> storageMap;

    @BeforeEach
    void setUp() {
        userStorage = mock(UserStorage.class);
        storageMap = new HashMap<>();
        when(userStorage.getStorage()).thenReturn(storageMap);
        userDao = new UserDaoImpl();
        userDao.setUserStorage(userStorage);
    }

    @Test
    void saveUser() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        assertEquals(user, userDao.save(user));
        assertTrue(storageMap.containsKey(1L));
    }

    @Test
    void saveUserThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> userDao.save(null));
        assertThrows(IllegalArgumentException.class, () -> userDao.save(new User()));
    }

    @Test
    void updateUserSuccess() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        storageMap.put(1L, user);
        user.setFirstName("Anna");
        assertEquals(user, userDao.update(user));
        assertEquals("Anna", storageMap.get(1L).getFirstName());
    }

    @Test
    void updateUserNotFound() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        assertThrows(IllegalArgumentException.class, () -> userDao.update(user));
    }

    @Test
    void findByIdFound() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        storageMap.put(1L, user);
        Optional<User> result = userDao.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void findByUsernameFound() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        storageMap.put(1L, user);
        Optional<User> result = userDao.findByUsername("Ani.Smith");
        assertTrue(result.isPresent());
        assertEquals("Ani.Smith", result.get().getUsername());
    }

    @Test
    void findByUsernameMissing() {
        assertTrue(userDao.findByUsername("Missing.User").isEmpty());
    }

    @Test
    void findAllReturnsUsers() {
        storageMap.put(1L, new User("A", "B", "A.B", "p", true, 1L));
        storageMap.put(2L, new User("C", "D", "C.D", "p", true, 2L));
        List<User> all = userDao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void deleteRemovesUser() {
        storageMap.put(1L, new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L));
        userDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }
}
