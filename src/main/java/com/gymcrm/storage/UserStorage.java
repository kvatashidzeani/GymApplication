package com.gymcrm.storage;

import com.gymcrm.model.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserStorage {

    private final Map<Long, User> userMap = new HashMap<>();

    public Map<Long, User> getStorage() {
        return userMap;
    }
}
