package com.gymcrm.Util;

import com.gymcrm.dao.UserDao;
import com.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);
    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String generateUsername(String firstname, String lastname) {
        String base = firstname.trim() + "." + lastname.trim();
        String username = base;
        int counter = 1;
        Set<String> existingUsernames = getAllUsernames();
        while (existingUsernames.contains(username)) {
            username = base + counter;
            counter++;
            log.debug("Username {} already exists, trying {}", base, username);
        }
        existingUsernames.add(username);
        log.debug("Generated username {}", username);
        return username;
    }

    public Set<String> getAllUsernames() {
        return userDao.findAll().stream()
                .map(User::getUsername)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
