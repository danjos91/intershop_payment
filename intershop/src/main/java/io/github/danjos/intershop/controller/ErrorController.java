package io.github.danjos.intershop.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
public class ErrorController {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
                
                Throwable error = getError(request);
                
                if (error instanceof ResponseStatusException) {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    errorAttributes.put("status", ex.getStatus().value());
                    errorAttributes.put("error", ex.getStatus().getReasonPhrase());
                    errorAttributes.put("message", ex.getReason());
                }
                
                // Add custom error messages
                if (error != null) {
                    String message = error.getMessage();
                    if (message == null || message.isEmpty()) {
                        message = "An unexpected error occurred";
                    }
                    errorAttributes.put("message", message);
                }
                
                return errorAttributes;
            }
        };
    }

    public static Mono<ServerResponse> handleError(ServerRequest request) {
        Map<String, Object> errorAttributes = request.attributes()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith("org.springframework.boot.web.reactive.error"))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(value -> (Map<String, Object>) value)
                .orElse(Map.of());

        Integer status = (Integer) errorAttributes.getOrDefault("status", 500);
        String message = (String) errorAttributes.getOrDefault("message", "Internal Server Error");
        
        return ServerResponse.status(HttpStatus.valueOf(status))
                .render("error", Map.of(
                    "status", status,
                    "message", message,
                    "timestamp", System.currentTimeMillis()
                ));
    }
}
