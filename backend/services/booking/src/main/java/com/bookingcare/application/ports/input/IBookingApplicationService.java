package com.bookingcare.application.ports.input;

import java.util.List;
import java.util.Map;

import com.bookingcare.application.dto.CreateBookingCommand;
import com.bookingcare.application.dto.HealthCheckBookHistoryResponse;
import com.bookingcare.application.dto.QueryBookingOrderDetailInfoResponse;
import com.bookingcare.application.dto.QueryBookingPackageDetailInfo;
import com.bookingcare.application.dto.QueryOrdersResponse;

public interface IBookingApplicationService {
    QueryBookingOrderDetailInfoResponse getBookingsOrderDetailInfo(String id);
    String createBooking(CreateBookingCommand command);
    List<QueryOrdersResponse> getBookingByPatientId(String id);
    List<QueryOrdersResponse> getBookingByClinicId(String id);
    List<QueryOrdersResponse> getAllBookingOrders();
    boolean updateBookingStatus(String id, String statusUpdate);
    Map<String, Object> getPaymentUrl(String bookingId);
    List<QueryBookingPackageDetailInfo> getAllBookingPackageDetails();
    List<HealthCheckBookHistoryResponse> getBookingHistoryByPatientId(String id);
}
