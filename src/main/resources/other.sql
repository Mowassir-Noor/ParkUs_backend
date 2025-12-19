CREATE INDEX idx_booking_renter ON booking(renter_id);
CREATE INDEX idx_booking_owner ON booking(spot_id);
CREATE INDEX idx_log_time ON bookinglog(logged_at);


-- to make sure each use can rate spot only once

UNIQUE (spot_id, user_id)
