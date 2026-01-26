package com.sjmt.SJMT.Interceptor;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sjmt.SJMT.Service.LoggerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API Logging Interceptor
 * Intercepts all API requests and logs them
 * @author SJMT Team
 * @version 1.0
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingInterceptor.class);

    @Autowired
    private LoggerService loggerService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("=== INTERCEPTOR PREHANDE CALLED === URI: {}", request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        try {
            logger.info("=== INTERCEPTOR AFTER COMPLETION CALLED === URI: {}", request.getRequestURI());

            // Get timestamp
            LocalDateTime timestamp = LocalDateTime.now();

            // Get username from security context
            String username = extractUsername();
            logger.info("Extracted username: {}", username);

            // Get API endpoint
            String apiEndpoint = request.getRequestURI();
            logger.info("API endpoint: {}", apiEndpoint);

            // Get response status code
            Integer responseCode = response.getStatus();
            logger.info("Response code: {}", responseCode);

            // Log the API call
            loggerService.logApiCall(timestamp, username, apiEndpoint, responseCode);

            logger.info("Successfully logged API call: {} | {} | {}", username, apiEndpoint, responseCode);

        } catch (Exception e) {
            logger.error("Error in logging interceptor: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract username from Security Context
     */
    private String extractUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            logger.info("Authentication object: {}", authentication);

            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                String username = authentication.getName();
                logger.info("Found authenticated user: {}", username);
                return username;
            }
        } catch (Exception e) {
            logger.error("Error extracting username: {}", e.getMessage(), e);
        }

        logger.info("No authenticated user found, returning ANONYMOUS");
        return "ANONYMOUS";
    }
}