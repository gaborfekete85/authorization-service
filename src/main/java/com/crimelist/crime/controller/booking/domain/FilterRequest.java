package com.crimelist.crime.controller.booking.domain;

public class FilterRequest {
    String id;

    public FilterRequest() {
    }

    public FilterRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FilterRequest{" +
                "id='" + id + '\'' +
                '}';
    }
}
