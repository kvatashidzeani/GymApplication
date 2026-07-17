package com.gymcrm.dao;

import com.gymcrm.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user);
    User update(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void delete(Long id);
}
