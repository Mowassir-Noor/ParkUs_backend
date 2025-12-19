CREATE OR REPLACE FUNCTION log_booking()
RETURNS TRIGGER AS $$
BEGIN
    -- Only log when status becomes confirmed
    IF NEW.status = 'confirmed' AND OLD.status IS DISTINCT FROM 'confirmed' THEN
        INSERT INTO BookingLog (
            booking_id,
            spot_id,
            owner_id,
            renter_id,
            start_time,
            end_time,
            duration_hours,
            total_amount,
            booking_status
        )
SELECT
    NEW.booking_id,
    NEW.spot_id,
    ps.owner_id,
    NEW.renter_id,
    NEW.start_time,
    NEW.end_time,
    EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time)) / 3600,
    NEW.total_amount,
    NEW.status
FROM ParkingSpot ps
WHERE ps.spot_id = NEW.spot_id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- trigger for log booking
CREATE TRIGGER trg_log_booking
    AFTER UPDATE ON Booking
    FOR EACH ROW
    EXECUTE FUNCTION log_booking();
