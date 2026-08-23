package com.gymcrm.security;

import com.gymcrm.dao.UserDao;
import com.gymcrm.dao.impl.TraineeDaoImpl;
import com.gymcrm.dao.impl.TrainerDaoImpl;
import com.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads Gym CRM users from in-memory storage for Spring Security authentication.
 */
@Service
public class GymUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(GymUserDetailsService.class);

    private final UserDao userDao;
    private final TraineeDaoImpl traineeDao;
    private final TrainerDaoImpl trainerDao;
    private final LoginAttemptService loginAttemptService;

    public GymUserDetailsService(UserDao userDao, TraineeDaoImpl traineeDao, TrainerDaoImpl trainerDao,
                                 LoginAttemptService loginAttemptService) {
        this.userDao = userDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(username)) {
            log.warn("Authentication blocked for username={} (brute-force lock)", username);
            throw new LockedException("User is temporarily blocked due to too many failed login attempts");
        }

        User user = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found username={}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        boolean isTrainee = traineeDao.findAll().stream()
                .anyMatch(t -> user.getUserId().equals(t.getUserId()));
        boolean isTrainer = trainerDao.findAll().stream()
                .anyMatch(t -> user.getUserId().equals(t.getUserId()));

        if (isTrainee) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TRAINEE"));
        }
        if (isTrainer) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TRAINER"));
        }

        log.debug("Loaded user details for username={}, active={}, roles={}",
                username, user.isActive(), authorities);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isActive())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
