package com.library.library_management_system.security;

import com.library.library_management_system.entity.User;
import com.library.library_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("Loading user by identifier: {}", identifier);

        User user = userRepository.findByEmailOrUsername(identifier)
                .orElseThrow(() -> {
                    log.error("User not found with identifier: {}", identifier);
                    return new UsernameNotFoundException("User not found with identifier: " + identifier);
                });

        if (!user.getIsActive()) {
            log.warn("Inactive user attempted to login: {}", identifier);
            throw new UsernameNotFoundException("User account is deactivated: " + identifier);
        }

        log.debug("User loaded successfully: {} (Role: {})", user.getUsername(), user.getRole());
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with ID: " + id);
                });

        if (!user.getIsActive()) {
            log.warn("Inactive user accessed by ID: {}", id);
            throw new UsernameNotFoundException("User account is deactivated: " + id);
        }

        log.debug("User loaded by ID successfully: {} (Role: {})", user.getUsername(), user.getRole());
        return UserPrincipal.create(user);
    }
}