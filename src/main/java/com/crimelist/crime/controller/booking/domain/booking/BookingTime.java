package com.crimelist.crime.controller.booking.domain.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingTime {

    private long nanoseconds;
    private long seconds;

}
