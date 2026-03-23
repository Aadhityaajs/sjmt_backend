package com.sjmt.SJMT.Entity;

/**
 * Attendance Status Enum
 * Defines all possible attendance statuses
 *
 * @author SJMT Team
 * @version 1.0
 */
public enum AttendanceStatusEnum {
    PRESENT("Present"),
    ABSENT("Absent"),
    HALF_DAY("Half Day"),
    COMPANY_HOLIDAY("Company Holiday"),
    GENERAL_HOLIDAY("General Holiday");

    private final String displayName;

    AttendanceStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}