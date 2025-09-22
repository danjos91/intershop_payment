package io.github.danjos.intershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange()
                .pathMatchers("/", "/items/**", "/images/**", "/css/**", "/js/**", "/login", "/register", "/debug/**", "/test/**", "/oauth2/**", "/api/users/**").permitAll()
                .pathMatchers("/cart/**", "/orders/**").authenticated()
                .anyExchange().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
            .and()
            .oauth2Login()
            .and()
            .logout()
                .logoutUrl("/logout")
            .and()
            .csrf().disable()
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
