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
    
    public Mono<User> createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        return userRepository.save(user);
    }
    
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Mono<Long> getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(User::getId);
    }
    
    public Mono<Long> getOrCreateUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(User::getId)
            .switchIfEmpty(Mono.defer(() -> {
                // Create a default user if not found
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(username + "@example.com");
                newUser.setPassword(passwordEncoder.encode("defaultPassword"));
                newUser.setBalance(java.math.BigDecimal.valueOf(1000.0));
                return userRepository.save(newUser)
                    .map(User::getId);
            }));
    }
    
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
    
    public Mono<User> updateUser(User user) {
        return userRepository.save(user);
    }
}
