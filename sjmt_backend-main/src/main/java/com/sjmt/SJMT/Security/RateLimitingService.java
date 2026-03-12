package com.sjmt.SJMT.Security;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

/**
 * Simple in-memory rate limiting service for Auth endpoints
 */
@Service
public class RateLimitingService {
    
    private final ConcurrentHashMap<String, RequestData> requestCounts = new ConcurrentHashMap<>();
    
    public RateLimitingService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
            requestCounts.entrySet().removeIf(entry -> entry.getValue().lastRequestTime.isBefore(threshold));
        }, 15, 15, TimeUnit.MINUTES);
    }
    
    public boolean isAllowed(String key) {
        RequestData data = requestCounts.compute(key, (k, val) -> {
            if (val == null || val.lastRequestTime.isBefore(LocalDateTime.now().minusMinutes(15))) {
                return new RequestData(1, LocalDateTime.now());
            }
            val.count++;
            val.lastRequestTime = LocalDateTime.now();
            return val;
        });
        
        // Allow max 10 requests per 15 minutes per key/IP
        return data.count <= 10;
    }
    
    private static class RequestData {
        int count;
        LocalDateTime lastRequestTime;
        RequestData(int count, LocalDateTime lastRequestTime) {
            this.count = count;
            this.lastRequestTime = lastRequestTime;
        }
    }
}
