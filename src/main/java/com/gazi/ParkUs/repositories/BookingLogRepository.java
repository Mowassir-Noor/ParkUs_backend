package com.gazi.ParkUs.repositories;

import com.gazi.ParkUs.entities.BookingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingLogRepository extends JpaRepository<BookingLog, Long> {

    List<BookingLog> findByBookingId(Long bookingId);

    List<BookingLog> findByRenterId(Long renterId);

    List<BookingLog> findByOwnerId(Long ownerId);

    @Query("""
        SELECT l FROM BookingLog l
        WHERE l.loggedAt BETWEEN :from AND :to
    """)
    List<BookingLog> findLogsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
