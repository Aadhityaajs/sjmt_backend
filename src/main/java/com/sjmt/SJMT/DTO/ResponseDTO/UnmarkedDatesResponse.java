package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for unmarked dates report
 *
 * @author SJMT Team
 * @version 1.0
 */
public class UnmarkedDatesResponse {

    private LocalDate date;
    private Integer totalActiveUsers;
    private Integer markedCount;
    private Integer unmarkedCount;
    private List<UserBasicInfo> unmarkedUsersList;

    // Constructors
    public UnmarkedDatesResponse() {
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTotalActiveUsers() {
        return totalActiveUsers;
    }

    public void setTotalActiveUsers(Integer totalActiveUsers) {
        this.totalActiveUsers = totalActiveUsers;
    }

    public Integer getMarkedCount() {
        return markedCount;
    }

    public void setMarkedCount(Integer markedCount) {
        this.markedCount = markedCount;
    }

    public Integer getUnmarkedCount() {
        return unmarkedCount;
    }

    public void setUnmarkedCount(Integer unmarkedCount) {
        this.unmarkedCount = unmarkedCount;
    }

    public List<UserBasicInfo> getUnmarkedUsersList() {
        return unmarkedUsersList;
    }

    public void setUnmarkedUsersList(List<UserBasicInfo> unmarkedUsersList) {
        this.unmarkedUsersList = unmarkedUsersList;
    }

    // Inner class for user basic info
    public static class UserBasicInfo {
        private Integer userId;
        private String username;
        private String fullName;

        public UserBasicInfo() {
        }

        public UserBasicInfo(Integer userId, String username, String fullName) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}