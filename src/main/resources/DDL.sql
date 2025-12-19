
--  used this query to change the ids from int to bigint in every table
-- ALTER TABLE booking
-- ALTER COLUMN availability_id TYPE BIGINT;




-- Users (both spot owners and renters)
CREATE TABLE Users (
                       user_id SERIAL PRIMARY KEY,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       is_owner BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Parking spots listed by owners
-- Parking spots with exact coordinates
CREATE TABLE ParkingSpot (
                             spot_id SERIAL PRIMARY KEY,
                             owner_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
                             title VARCHAR(100) NOT NULL,
                             description TEXT,
                             location VARCHAR(255),  -- optional textual address
                             latitude DECIMAL(9,6) NOT NULL,   -- e.g., 41.008238
                             longitude DECIMAL(9,6) NOT NULL,  -- e.g., 28.978359
                             slot_type VARCHAR(20) DEFAULT 'regular', -- regular, EV, handicapped, etc.
                             price_per_hour NUMERIC(10,2) NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Availability windows for each parking spot
CREATE TABLE SpotAvailability (
                                  availability_id SERIAL PRIMARY KEY,
                                  spot_id INT NOT NULL REFERENCES ParkingSpot(spot_id) ON DELETE CASCADE,
                                  start_time TIMESTAMP NOT NULL,
                                  end_time TIMESTAMP NOT NULL,
                                  is_booked BOOLEAN DEFAULT FALSE
);

-- Bookings made by users
CREATE TABLE Booking (
                         booking_id SERIAL PRIMARY KEY,
                         spot_id INT NOT NULL REFERENCES ParkingSpot(spot_id),
                         renter_id INT NOT NULL REFERENCES Users(user_id),
                         availability_id INT NOT NULL REFERENCES SpotAvailability(availability_id),
                         booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         status VARCHAR(20) DEFAULT 'pending', -- pending, confirmed, cancelled, completed
                         total_amount NUMERIC(10,2) NOT NULL
);

-- Optional: ratings for spots
CREATE TABLE SpotRating (
                            rating_id SERIAL PRIMARY KEY,
                            spot_id INT NOT NULL REFERENCES ParkingSpot(spot_id),
                            user_id INT NOT NULL REFERENCES Users(user_id),
                            rating INT CHECK (rating BETWEEN 1 AND 5),
                            comment TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);






CREATE TABLE BookingLog (
                            log_id SERIAL PRIMARY KEY,

                            booking_id INT NOT NULL,
                            spot_id INT NOT NULL,
                            owner_id INT NOT NULL,
                            renter_id INT NOT NULL,

                            start_time TIMESTAMP NOT NULL,
                            end_time TIMESTAMP NOT NULL,

                            duration_hours NUMERIC(5,2) NOT NULL,
                            total_amount NUMERIC(10,2) NOT NULL,

                            booking_status VARCHAR(20),

                            logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);






