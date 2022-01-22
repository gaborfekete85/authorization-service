package com.crimelist.crime.controller.booking.domain.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Functionality {

    private String id;
    private int length;
    private String name;
    private BigDecimal price;
    private String serviceId;
    private String symbol;

}
