package com.bookingcare.payment.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor  
public class ApiErrorResponse {
    public int StatusCode;
    public String Message;
    public Object Details;
}
