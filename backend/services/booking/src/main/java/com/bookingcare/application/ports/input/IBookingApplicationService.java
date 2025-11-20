package com.bookingcare.application.ports.input;

import java.util.List;

import com.bookingcare.application.dto.CreateBookingCommand;
import com.bookingcare.application.dto.QueryBookingOrderDetailInfoResponse;
import com.bookingcare.application.dto.QueryOrdersResponse;

public interface IBookingApplicationService {
    QueryBookingOrderDetailInfoResponse getBookingsOrderDetailInfo(String id);
    String createBooking(CreateBookingCommand command);
    List<QueryOrdersResponse> getBookingByPatientId(String id);
    List<QueryOrdersResponse> getBookingByClinicId(String id);
    List<QueryOrdersResponse> getAllBookingOrders();
    boolean updateBookingStatus(String id, String statusUpdate);
}
