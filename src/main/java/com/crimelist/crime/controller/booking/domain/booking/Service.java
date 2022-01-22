package com.crimelist.crime.controller.booking.domain.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Service {
    private String id;
    private Boolean active;
    private String address;
    private String email;
    private List<String> functionalities;
    private String name;
    private String owner;
    private String phone;
    private String web;
    private String furtherContact;
    private List<Functionality> priceList;
}
