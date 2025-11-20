package com.bookingcare.domain.entity;

import com.bookingcare.domain.valueobject.DayId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private String id;
    private String startTime;
    private String endTime;
    private DayId day;
}
