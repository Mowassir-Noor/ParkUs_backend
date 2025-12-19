package com.gazi.ParkUs.repositories;

import com.gazi.ParkUs.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {



    List<Booking> findByRenter_UserId(Long renterId);

    List<Booking> findBySpot_Owner_UserId(Long ownerId);

    List<Booking> findByStatus(String status);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.spot.spotId = :spotId
          AND b.status IN ('confirmed', 'pending')
    """)
    List<Booking> findActiveBookingsForSpot(@Param("spotId") Long spotId);
}
