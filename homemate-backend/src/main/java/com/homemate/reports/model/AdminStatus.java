package com.homemate.reports.model;

public enum AdminStatus {
    PENDING("pending"),
    DONE("done");

    private final String value;

    AdminStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AdminStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AdminStatus status : AdminStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown admin status: " + value);
    }
}
