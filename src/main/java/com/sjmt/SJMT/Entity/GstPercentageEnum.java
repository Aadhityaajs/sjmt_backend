package com.sjmt.SJMT.Entity;

/**
 * GST Percentage Enum for Indian Tax Rates
 */
public enum GstPercentageEnum {
    GST_0(0),
    GST_5(5),
    GST_12(12),
    GST_18(18),
    GST_28(28);
    
    private final int value;
    
    GstPercentageEnum(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static GstPercentageEnum fromValue(int value) {
        for (GstPercentageEnum gst : GstPercentageEnum.values()) {
            if (gst.getValue() == value) {
                return gst;
            }
        }
        throw new IllegalArgumentException("Invalid GST percentage: " + value + ". Valid values are: 0, 5, 12, 18, 28");
    }
}