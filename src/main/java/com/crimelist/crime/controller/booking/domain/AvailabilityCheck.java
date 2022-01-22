package com.crimelist.crime.controller.booking.domain;

import java.time.LocalDateTime;

public class AvailabilityCheck {

    LocalDateTime start;
    Long length;
    String groupId;
    Boolean wereClashing;

    public AvailabilityCheck() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Boolean getWereClashing() {
        return wereClashing;
    }

    public void setWereClashing(Boolean wereClashing) {
        this.wereClashing = wereClashing;
    }

    @Override
    public String toString() {
        return "AvailabilityCheck{" +
                "start=" + start +
                ", length=" + length +
                ", groupId='" + groupId + '\'' +
                ", wereClashing='" + wereClashing + '\'' +
                '}';
    }
}
