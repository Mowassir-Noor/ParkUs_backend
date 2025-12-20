package com.gazi.ParkUs.services;

import com.gazi.ParkUs.dto.ParkingSpotRequestDto;
import com.gazi.ParkUs.dto.ParkingSpotResponseDto;
import com.gazi.ParkUs.entities.ParkingSpot;
import com.gazi.ParkUs.entities.RegularUser;
import com.gazi.ParkUs.entities.UserEntity;
import com.gazi.ParkUs.repositories.ParkingSpotRepository;
import com.gazi.ParkUs.repositories.UserRepository;
import com.gazi.ParkUs.security.SecurityUtils;
import com.gazi.ParkUs.services.ParkingSpotService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@Transactional
public class ParkingSpotServiceImpl implements ParkingSpotService {

    private final ParkingSpotRepository spotRepo;
    private final UserRepository userRepo;

    public ParkingSpotServiceImpl(ParkingSpotRepository spotRepo, UserRepository userRepo) {
        this.spotRepo = spotRepo;
        this.userRepo = userRepo;
    }

    // CREATE (no owner check)
    @Override
    public ParkingSpotResponseDto createSpot(ParkingSpotRequestDto dto) {
        UserEntity currentUser=userRepo.findByEmail(SecurityUtils.currentUserEmail()).orElseThrow(()->new RuntimeException("user not found"));



        ParkingSpot spot = new ParkingSpot();
        applyDto(spot, dto);
        spot.setOwner((RegularUser) currentUser);

        return toResponse(spotRepo.save(spot));
    }

    // READ
    @Override
    public ParkingSpotResponseDto getSpotById(Long id) {
        return toResponse(findSpot(id));
    }
    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public List<ParkingSpotResponseDto> getAllSpots() {
        return spotRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ParkingSpotResponseDto> getSpotsByOwner(Long ownerId) {
        return spotRepo.findByOwner_UserId(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // UPDATE (owner only)
    @Override
    public ParkingSpotResponseDto updateSpot(Long spotId, ParkingSpotRequestDto dto)
            throws AccessDeniedException {

        ParkingSpot spot = findSpot(spotId);
       UserEntity currentUser=userRepo.findByEmail(SecurityUtils.currentUserEmail()).orElseThrow(()->new RuntimeException("user not found"));


        assertOwner(spot, currentUser);

        applyDto(spot, dto);
        return toResponse(spotRepo.save(spot));
    }

    // DELETE (owner only)
    @Override
    public void deleteSpot(Long spotId) throws AccessDeniedException {

        ParkingSpot spot = findSpot(spotId);
        UserEntity currentUser=userRepo.findByEmail(SecurityUtils.currentUserEmail()).orElseThrow(()->new RuntimeException("user not found"));


        assertOwner(spot, currentUser);

        spotRepo.delete(spot);
    }

    // ---------- helpers ----------

    private ParkingSpot findSpot(Long id) {
        return spotRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Parking spot not found"));
    }

    private void assertOwner(ParkingSpot spot, UserEntity user)
            throws AccessDeniedException {

        if (!spot.getOwner().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("You do not own this parking spot");
        }
    }

    private void applyDto(ParkingSpot spot, ParkingSpotRequestDto dto) {
        spot.setTitle(dto.getTitle());
        spot.setDescription(dto.getDescription());
        spot.setLocation(dto.getLocation());
        spot.setLatitude(dto.getLatitude());
        spot.setLongitude(dto.getLongitude());
        spot.setSlotType(dto.getSlotType());
        spot.setPricePerHour(dto.getPricePerHour());
    }

    private ParkingSpotResponseDto toResponse(ParkingSpot spot) {
        ParkingSpotResponseDto dto = new ParkingSpotResponseDto();
        dto.setSpotId(spot.getSpotId());
        dto.setTitle(spot.getTitle());
        dto.setDescription(spot.getDescription());
        dto.setLocation(spot.getLocation());
        dto.setLatitude(spot.getLatitude());
        dto.setLongitude(spot.getLongitude());
        dto.setSlotType(spot.getSlotType());
        dto.setPricePerHour(spot.getPricePerHour());
        dto.setOwnerId(spot.getOwner().getUserId());
        return dto;
    }
}
