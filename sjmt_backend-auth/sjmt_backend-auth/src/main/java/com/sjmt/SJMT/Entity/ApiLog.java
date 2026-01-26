package com.sjmt.SJMT.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * API Log Entity
 * Stores API access logs
 * @author SJMT Team
 * @version 1.0
 */
@Entity
@Table(name = "api_logs")
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "api_endpoint", nullable = false, length = 500)
    private String apiEndpoint;

    @Column(name = "response_code", nullable = false)
    private Integer responseCode;

    // Constructors
    public ApiLog() {
    }

    public ApiLog(LocalDateTime timestamp, String username, String apiEndpoint, Integer responseCode) {
        this.timestamp = timestamp;
        this.username = username;
        this.apiEndpoint = apiEndpoint;
        this.responseCode = responseCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "ApiLog{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                ", apiEndpoint='" + apiEndpoint + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}