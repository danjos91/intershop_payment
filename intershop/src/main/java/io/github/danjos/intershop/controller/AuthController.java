package io.github.danjos.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.validation.annotation.Validated;
import io.github.danjos.intershop.service.UserService;
import io.github.danjos.intershop.dto.UserRegistrationDto;
import reactor.core.publisher.Mono;

@Controller
public class AuthController {
    
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @PostMapping("/register")
    public Mono<Rendering> registerUser(@Validated UserRegistrationDto userDto) {
        return userService.createUser(userDto.getUsername(), userDto.getPassword(), userDto.getEmail())
            .map(user -> Rendering.redirectTo("/login?registered=true").build())
            .onErrorReturn(Rendering.redirectTo("/register?error=true").build());
    }
    
    // Remove custom logout - let Spring Security handle it
    // @GetMapping("/logout")
    // public String logout() {
    //     return "redirect:/";
    // }

    @GetMapping("/debug/users")
    @ResponseBody
    public String debugUsers() {
        try {
            // Use a different approach to avoid blocking
            var userOpt = userService.findByUsername("currentUser").blockOptional();
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                boolean passwordMatch = userService.matchesPassword("testpass", user.getPassword());
                return String.format("User found: %s, Password matches 'testpass': %s, Stored hash: %s", 
                    user.getUsername(), passwordMatch, user.getPassword());
            } else {
                return "User 'currentUser' not found";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/debug/generate-hash")
    @ResponseBody
    public String generateHash() {
        try {
            // Generate a new hash for 'testpass'
            String newHash = userService.getPasswordEncoder().encode("testpass");
            boolean matches = userService.matchesPassword("testpass", newHash);
            return String.format("New hash for 'testpass': %s, Matches: %s", newHash, matches);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/debug/password")
    @ResponseBody
    public String debugPassword() {
        try {
            // Test the known BCrypt hash
            String knownHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
            boolean matches = userService.matchesPassword("testpass", knownHash);
            return String.format("Known hash matches 'testpass': %s", matches);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
