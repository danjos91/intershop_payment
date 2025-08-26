package io.github.danjos.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import io.github.danjos.intershop.repository.UserRepository;
import io.github.danjos.intershop.model.User;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> getCurrentUser() {
        return userRepository.findByUsername("currentUser")
                .switchIfEmpty(Mono.error(new RuntimeException("Current user not found")));
    }

    public User getCurrentUserBlocking() {
        return getCurrentUser().block();
    }
    
    public Mono<User> createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
