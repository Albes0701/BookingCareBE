package com.bookingcare.application.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bookingcare.application.dto.CreateBookingCommand;
import com.bookingcare.application.dto.HealthCheckBookHistoryResponse;
import com.bookingcare.application.dto.QueryBookingOrderDetailInfoResponse;
import com.bookingcare.application.dto.QueryBookingPackageDetailInfo;
import com.bookingcare.application.dto.QueryOrdersResponse;
import com.bookingcare.application.dto.QueryPackageScheduleResponse;
import com.bookingcare.application.dto.event.BookingCreatedEvent;
import com.bookingcare.application.mapper.BookingMapperApplication;
import com.bookingcare.application.ports.input.IBookingApplicationService;
import com.bookingcare.application.ports.output.IBookingRepository;
import com.bookingcare.application.ports.output.IHealthCheckPackageScheduleBookingDetailRepository;
import com.bookingcare.application.ports.output.IScheduleFeignClientService;
import com.bookingcare.application.saga.BookingEventPublisher;
import com.bookingcare.domain.entity.BookingPackageDetail;
import com.bookingcare.domain.entity.HealthCheckPackageScheduleBookingDetail;
import com.bookingcare.domain.valueobject.BookingStatus;
import com.bookingcare.infrastructure.external.package_service.HealthCheckPackageResponse;
import com.bookingcare.infrastructure.external.package_service.HealthPackageFeignClient;

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
    private final BookingEventPublisher eventPublisher;
    private final HealthPackageFeignClient healthPackageFeignClient;



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
    public String createBooking(CreateBookingCommand cmd) {
        // 1. Create and save booking
        HealthCheckPackageScheduleBookingDetail booking = bookingMapper.toEntity(cmd);
        booking.initialize(); // Set PENDING status, generate ID
        
        HealthCheckPackageScheduleBookingDetail savedBooking = _healthCheckPackageScheduleBookingDetailRepository.save(booking);

        log.info("Booking created: id={}, status={}", 
                savedBooking.getId(), savedBooking.getBookingStatus());

        // 2. Generate correlation ID for distributed tracing
        String correlationId = java.util.UUID.randomUUID().toString();
        
        log.info("Saga initiated: bookingId={}, correlationId={}", 
                savedBooking.getId(), correlationId);

        // 3. Publish BookingCreatedEvent (Step 2 of saga flow)
        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(savedBooking.getId())
                .packageScheduleId(savedBooking.getPackageScheduleId())
                .patientId(savedBooking.getPatientId())
                .clinicId(savedBooking.getClinicId())
                .build();

        eventPublisher.publishBookingCreatedEvent(event, correlationId);

        return savedBooking.getId();
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


    @Override
    public Map<String, Object> getPaymentUrl(String bookingId) {
        try {
            Optional<HealthCheckPackageScheduleBookingDetail> bookingOptional = 
                    _healthCheckPackageScheduleBookingDetailRepository.findById(bookingId);
            
            if (bookingOptional.isEmpty()) {
                return Map.of(
                        "status", "ERROR",
                        "message", "Booking not found"
                );
            }
            
            HealthCheckPackageScheduleBookingDetail booking = bookingOptional.get();
            
            // ✅ Chưa có paymentUrl → Đang xử lý
            if (booking.getPaymentUrl() == null) {
                return Map.of(
                        "status", "PENDING",
                        "message", "Payment link is being generated...",
                        "bookingStatus", booking.getBookingStatus().toString()
                );
            }
            
            // ✅ Đã có paymentUrl → Sẵn sàng
            return Map.of(
                    "status", "READY",
                    "paymentUrl", booking.getPaymentUrl(),
                    "orderCode", booking.getOrderCode(),
                    "bookingStatus", booking.getBookingStatus().toString(),
                    "message", "Payment link ready. Redirect user to paymentUrl."
            );
            
        } catch (Exception e) {
            log.error("Error getting payment URL for bookingId: {}", bookingId, e);
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }


    @Override
    public List<QueryBookingPackageDetailInfo> getAllBookingPackageDetails() {
        try {
            // 1. Get all booking package details from local repository
            List<BookingPackageDetail> packageDetails = _bookingRepository.findAllBookingPackageDetails();
            
            log.info("Found {} booking package details", packageDetails.size());
            
            // 2. Map and enrich with health check package info via OpenFeign
            List<QueryBookingPackageDetailInfo> response = packageDetails.stream()
                    .map(packageDetail -> {
                        try {
                            // Get the package ID from booking package detail
                            String packageId = packageDetail.getPackageId();
                            
                            if (packageId == null || packageId.isEmpty()) {
                                log.warn("Package ID is null or empty for booking package id: {}", 
                                        packageDetail.getBookingPackageId());
                                return null;
                            }
                            
                            // Call package-service via OpenFeign to get health check package details
                            ResponseEntity<HealthCheckPackageResponse> responseEntity = healthPackageFeignClient
                                    .getPackageDetail(java.util.UUID.fromString(packageId));
                            
                            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                                log.warn("Failed to fetch health check package from package-service for package id: {}", 
                                        packageId);
                                return null;
                            }
                            
                            HealthCheckPackageResponse healthCheckPackage = responseEntity.getBody();
                            
                            // Map to QueryBookingPackageDetailInfo DTO
                            return bookingMapper.toQueryBookingPackageDetailInfo(
                                    packageDetail, 
                                    healthCheckPackage
                            );
                        } catch (Exception e) {
                            log.warn("Error processing booking package id {}: {}", 
                                    packageDetail.getBookingPackageId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(detail -> detail != null)
                    .toList();
            
            log.info("Successfully enriched {} booking package details with health check package info", 
                    response.size());
            return response;
        } catch (Exception e) {
            log.error("Error fetching booking package details: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch booking package details", e);
        }
    }


    @Override
    public List<HealthCheckBookHistoryResponse> getBookingHistoryByPatientId(String id) {
        try {
            log.info("Fetching booking history for patient id: {}", id);
            
            List<HealthCheckPackageScheduleBookingDetail> bookings = _healthCheckPackageScheduleBookingDetailRepository
                    .findByPatientId(id);
            
            if (bookings.isEmpty()) {
                log.info("No booking history found for patient id: {}", id);
                return List.of();
            }
            
            log.info("Found {} bookings for patient id: {}", bookings.size(), id);
            
            List<HealthCheckBookHistoryResponse> response = bookings.stream()
                    .map(booking -> {
                        try {
                            log.info("Processing booking - bookingPackageId: {}", booking.getBookingPackageId());
                            
                            // Find BookingPackageDetail using bookingPackageId
                            Optional<BookingPackageDetail> packageDetailOptional = _bookingRepository
                                    .findByBookingPackageId(booking.getBookingPackageId());
                            
                            if (packageDetailOptional.isEmpty()) {
                                log.warn("BookingPackageDetail not found for bookingPackageId: {}", 
                                        booking.getBookingPackageId());
                                return null;
                            }
                            
                            BookingPackageDetail packageDetail = packageDetailOptional.get();
                            log.info("Found BookingPackageDetail - packageId: {}, price: {}", 
                                    packageDetail.getPackageId(), packageDetail.getPrice());
                            
                            // Map booking and packageDetail to HistoryResponse
                            return bookingMapper.toHealthCheckBookHistoryResponse(booking, packageDetail);
                            
                        } catch (Exception e) {
                            log.warn("Error processing booking history for bookingPackageId {}: {}", 
                                    booking.getBookingPackageId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(response_item -> response_item != null)
                    .toList();
            
            log.info("Successfully processed {} booking history records", response.size());
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching booking history for patient id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch booking history", e);
        }
    }




    




}
