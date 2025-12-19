package com.gazi.ParkUs.repositories;

import com.gazi.ParkUs.entities.SpotRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotRatingRepository extends JpaRepository<SpotRating, Long> {

    List<SpotRating> findBySpot_SpotId(Long spotId);

    boolean existsBySpot_SpotIdAndUser_UserId(Long spotId, Long userId);
}
