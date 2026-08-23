package com.gymcrm.dao.impl;

import com.gymcrm.dao.UserDao;
import com.gymcrm.model.User;
import com.gymcrm.storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);
    private UserStorage userStorage;

    @Autowired
    public void setUserStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User save(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User or user ID cannot be null");
        }
        userStorage.getStorage().put(user.getUserId(), user);
        log.debug("User saved with id {}. Total users: {}", user.getUserId(), userStorage.getStorage().size());
        return user;
    }

    @Override
    public User update(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User or user ID cannot be null");
        }
        Long id = user.getUserId();
        if (!userStorage.getStorage().containsKey(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userStorage.getStorage().put(id, user);
        log.info("Updated user with id {}", id);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userStorage.getStorage().get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userStorage.getStorage().values().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userStorage.getStorage().values());
    }

    @Override
    public void delete(Long id) {
        userStorage.getStorage().remove(id);
        log.info("Deleted user with id {}", id);
    }
}
