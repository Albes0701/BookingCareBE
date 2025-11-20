package com.bookingcare.application.handler;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bookingcare.application.dto.ApiResponse;
import com.bookingcare.application.dto.CreateBookingCommand;
import com.bookingcare.application.dto.QueryBookingOrderDetailInfoResponse;
import com.bookingcare.application.dto.QueryOrdersResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.mapper.BookingMapperApplication;
import com.bookingcare.application.ports.input.IBookingApplicationService;
import com.bookingcare.application.ports.output.IBookingRepository;
import com.bookingcare.application.ports.output.IHealthCheckPackageScheduleBookingDetailRepository;
import com.bookingcare.application.ports.output.IScheduleFeignClientService;
import com.bookingcare.domain.entity.BookingPackageDetail;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;
import com.bookingcare.domain.exception.BookingDomainException;
import com.bookingcare.domain.valueobject.BookingStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingApplicationService implements IBookingApplicationService {
    private final BookingMapperApplication bookingMapper;
    private final IBookingRepository _bookingRepository;
    private final IHealthCheckPackageScheduleBookingDetailRepository _healthCheckPackageScheduleBookingDetailRepository;
    private final IScheduleFeignClientService _scheduleFeignClientService;

    @Override
    public QueryBookingOrderDetailInfoResponse getBookingsOrderDetailInfo(String id) {
        try {
            Optional<HealthCheckPackageScheduleBookingDetail> orderDetailOptional = _healthCheckPackageScheduleBookingDetailRepository
                    .findById(id);

            if (orderDetailOptional.isEmpty()) {
                log.warn("Booking order detail with id {} not found", id);
                return null;
            }

            HealthCheckPackageScheduleBookingDetail orderDetail = orderDetailOptional.get();

            Optional<QueryPackageScheduleResponse> packageScheduleOptional = _scheduleFeignClientService
                    .getPackageScheduleById(orderDetail.getPackageScheduleId()).getData();

            if (packageScheduleOptional.isEmpty()) {
                log.warn("Package schedule with id {} not found", orderDetail.getPackageScheduleId());
                return null;
            }

            QueryPackageScheduleResponse packageSchedule = packageScheduleOptional.get();

            Optional<BookingPackageDetail> packageDetailOptional = _bookingRepository
                    .findById(orderDetail.getBookingPackageId(),
                            packageSchedule.packageId());

            if (packageDetailOptional.isEmpty()) {
                log.warn("Booking package detail not found for package: {} and schedule: {}",
                        orderDetail.getBookingPackageId(), packageSchedule.packageId());
            }

            BookingPackageDetail packageDetail = packageDetailOptional.get();

            return bookingMapper.toQueryBookingOrderDetailInfoResponse(orderDetail, packageSchedule, packageDetail);
        } catch (Exception e) {
            log.error("Error fetching bookings for patient id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public String createBooking(CreateBookingCommand command) {
        try {
            HealthCheckPackageScheduleBookingDetail orderDetail = bookingMapper.toEntity(command);

            validateOrderReferences(orderDetail);

            orderDetail.initialize();

            log.info("Order after initialization and validation: {}", orderDetail);

            HealthCheckPackageScheduleBookingDetail savedOrder = _healthCheckPackageScheduleBookingDetailRepository
                    .save(orderDetail);
            return savedOrder.getId();
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage());
            throw e;
        }
    }

    private void validateOrderReferences(HealthCheckPackageScheduleBookingDetail order) {
        String packageScheduleId = order.getPackageScheduleId();
        try {
            ApiResponse<Optional<QueryPackageScheduleResponse>> response = _scheduleFeignClientService
                    .getPackageScheduleById(packageScheduleId);

            if (response.getStatus() == 404 || response.getData().isEmpty()) {
                log.error("Package schedule not found: {}", packageScheduleId);
                throw new BookingDomainException("Package schedule not found: " + packageScheduleId);
            }

            Optional<BookingPackageDetail> packageDetail = _bookingRepository
                    .findById(order.getBookingPackageId(),
                            response.getData().get().packageId());

            if (packageDetail.isEmpty()) {
                log.error("Booking package detail not found for package: {} and schedule: {}",
                        order.getBookingPackageId(), response.getData().get().packageId());
                throw new BookingDomainException("Booking package detail not found");
            }

        } catch (Exception e) {
            log.error("Failed to validate package schedule: {}", packageScheduleId, e);
            throw new BookingDomainException("Unable to validate package schedule: " + e.getMessage());
        }
    }

    @Override
    public List<QueryOrdersResponse> getBookingByPatientId(String id) {
        try {
            List<HealthCheckPackageScheduleBookingDetail> bookings = _healthCheckPackageScheduleBookingDetailRepository
                    .findByPatientId(id);
            List<QueryOrdersResponse> response = bookings.stream()
                    .map(bookingMapper::toQueryOrdersResponse)
                    .toList();
            return response;
        } catch (Exception e) {
            log.error("Error fetching bookings for patient id {}: {}", id, e.getMessage());
            throw e;
        } catch (StackOverflowError se) {
            log.error("Stack overflow error while fetching bookings for patient id {}: {}", id, se.getMessage());
            throw se;
        }
    }

    @Override
    public List<QueryOrdersResponse> getBookingByClinicId(String id) {
        try {
            List<HealthCheckPackageScheduleBookingDetail> bookings = _healthCheckPackageScheduleBookingDetailRepository
                    .findByClinicId(id);
            List<QueryOrdersResponse> response = bookings.stream()
                    .map(bookingMapper::toQueryOrdersResponse)
                    .toList();
            return response;
        } catch (Exception e) {
            log.error("Error fetching bookings for patient id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<QueryOrdersResponse> getAllBookingOrders() {
        try {
            List<HealthCheckPackageScheduleBookingDetail> bookings = _healthCheckPackageScheduleBookingDetailRepository
                    .findAll();
            List<QueryOrdersResponse> response = bookings.stream()
                    .map(bookingMapper::toQueryOrdersResponse)
                    .toList();
            return response;
        } catch (Exception e) {
            log.error("Error fetching bookings: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean updateBookingStatus(String id, String statusUpdate) {
        try {
            Optional<HealthCheckPackageScheduleBookingDetail> bookingOptional = _healthCheckPackageScheduleBookingDetailRepository
                    .findById(id);
            if (bookingOptional.isEmpty()) {
                log.warn("Booking with id {} not found", id);
                return false;
            }

            HealthCheckPackageScheduleBookingDetail booking = bookingOptional.get();
            booking.setBookingStatus(BookingStatus.fromString(statusUpdate));
            _healthCheckPackageScheduleBookingDetailRepository.save(booking);

            return true;
        } catch (Exception e) {
            log.error("Error updating booking status for id {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
