package com.gazi.ParkUs.repositories;

import com.gazi.ParkUs.entities.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    List<ParkingSpot> findByOwner_UserId(Long ownerId);

    boolean existsByOwner_UserId(Long ownerId);



    @Query("""
        SELECT p FROM ParkingSpot p
        WHERE p.latitude BETWEEN :latMin AND :latMax
          AND p.longitude BETWEEN :lngMin AND :lngMax
    """)
    List<ParkingSpot> findNearby(
            @Param("latMin") BigDecimal latMin,
            @Param("latMax") BigDecimal latMax,
            @Param("lngMin") BigDecimal lngMin,
            @Param("lngMax") BigDecimal lngMax
    );
}
