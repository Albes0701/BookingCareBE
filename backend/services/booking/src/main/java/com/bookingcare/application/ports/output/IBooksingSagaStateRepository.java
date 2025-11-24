package com.bookingcare.application.ports.output;

import com.bookingcare.domain.entity.BookingSagaState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for BookingSagaState persistence
 */
public interface IBooksingSagaStateRepository extends JpaRepository<BookingSagaState, String> {
    
    /**
     * Find saga state by booking ID
     */
    Optional<BookingSagaState> findByBookingId(String bookingId);
    
    /**
     * Find saga state by correlation ID (for distributed tracing)
     */
    Optional<BookingSagaState> findByCorrelationId(String correlationId);
}
