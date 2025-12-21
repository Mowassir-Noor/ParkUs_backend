package com.gazi.ParkUs.repositories;

import com.gazi.ParkUs.entities.SpotAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpotAvailabilityRepository extends JpaRepository<SpotAvailability, Long> {

    List<SpotAvailability> findBySpot_SpotId(Long spotId);
    
    List<SpotAvailability> findBySpot_SpotIdAndIsBookedFalse(Long spotId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM SpotAvailability a
        WHERE a.availabilityId = :id
    """)
    Optional<SpotAvailability> lockById(@Param("id") Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
        SELECT a FROM SpotAvailability a
        WHERE a.spot.spotId = :spotId
          AND a.startTime < :endTime
          AND a.endTime > :startTime
        """)
        List<SpotAvailability> findOverlapping(
            @Param("spotId") Long spotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
        );
}
