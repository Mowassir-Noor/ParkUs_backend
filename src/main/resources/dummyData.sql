INSERT INTO Users (first_name, last_name, email, password_hash, is_owner, role)
VALUES
    ('Ali', 'Kaya', 'ali.kaya@example.com', 'hashedpass1', TRUE, 'ADMIN'),
    ('Zeynep', 'Yilmaz', 'zeynep.yilmaz@example.com', 'hashedpass2', TRUE, 'USER'),
    ('Ahmet', 'Demir', 'ahmet.demir@example.com', 'hashedpass3', FALSE, 'USER'),
    ('Ece', 'Sahin', 'ece.sahin@example.com', 'hashedpass4', FALSE, 'USER'),
    ('Mehmet', 'Ozturk', 'mehmet.ozturk@example.com', 'hashedpass5', TRUE, 'USER'),
    ('Ayse', 'Kilic', 'ayse.kilic@example.com', 'hashedpass6', TRUE, 'USER'),
    ('Can', 'Yilmaz', 'can.yilmaz@example.com', 'hashedpass7', FALSE, 'USER'),
    ('Deniz', 'Aksoy', 'deniz.aksoy@example.com', 'hashedpass8', FALSE, 'USER'),
    ('Selin', 'Celik', 'selin.celik@example.com', 'hashedpass9', TRUE, 'USER'),
    ('Murat', 'Kara', 'murat.kara@example.com', 'hashedpass10', FALSE, 'USER');

INSERT INTO ParkingSpot (owner_id, title, description, location, latitude, longitude, slot_type, price_per_hour)
VALUES
    (1, 'Taksim Central', 'Covered spot near Taksim square', 'Istanbul, Taksim', 41.0369, 28.9850, 'regular', 12.00),
    (2, 'Besiktas EV Charger', 'EV charging available', 'Istanbul, Besiktas', 41.0430, 29.0010, 'EV', 15.00),
    (2, 'Kadikoy Garage', 'Safe indoor garage', 'Istanbul, Kadikoy', 40.9928, 29.0275, 'regular', 10.00),
    (5, 'Uskudar Rooftop', 'Rooftop spot with view', 'Istanbul, Uskudar', 41.0211, 29.0170, 'regular', 9.50),
    (6, 'Levent Underground', 'Covered underground spot', 'Istanbul, Levent', 41.0633, 29.0076, 'regular', 11.00),
    (9, 'Sisli Street', 'Open-air parking near main street', 'Istanbul, Sisli', 41.0613, 28.9876, 'regular', 8.50),
    (9, 'Bakirkoy Mall', 'Inside mall parking', 'Istanbul, Bakirkoy', 40.9905, 28.8725, 'EV', 14.00),
    (5, 'Atasehir Lot', 'Night parking available', 'Istanbul, Atasehir', 40.9922, 29.1270, 'regular', 7.50),
    (2, 'Kadikoy Marina', 'Near the marina, easy access', 'Istanbul, Kadikoy', 40.9900, 29.0320, 'regular', 10.50),
    (6, 'Beyoglu Open Spot', 'Open-air spot near cafes', 'Istanbul, Beyoglu', 41.0330, 28.9855, 'regular', 9.00);


INSERT INTO SpotAvailability (spot_id, start_time, end_time)
VALUES
    (1, '2025-12-19 08:00', '2025-12-19 20:00'),
    (2, '2025-12-18 09:00', '2025-12-18 18:00'),
    (3, '2025-12-18 00:00', '2025-12-18 23:59'),
    (4, '2025-12-19 06:00', '2025-12-19 22:00'),
    (5, '2025-12-18 07:00', '2025-12-18 21:00'),
    (6, '2025-12-18 08:00', '2025-12-18 20:00'),
    (7, '2025-12-19 09:00', '2025-12-19 18:00'),
    (8, '2025-12-18 00:00', '2025-12-18 23:59'),
    (9, '2025-12-18 08:00', '2025-12-18 20:00'),
    (10, '2025-12-18 07:00', '2025-12-18 21:00');

INSERT INTO Booking (spot_id, renter_id, availability_id, status, total_amount)
VALUES
    (1, 3, 1, 'confirmed', 120.00),
    (2, 4, 2, 'pending', 90.00),
    (3, 7, 3, 'confirmed', 100.00),
    (4, 8, 4, 'pending', 120.00),
    (5, 10, 5, 'confirmed', 110.00),
    (6, 3, 6, 'pending', 85.00),
    (7, 4, 7, 'confirmed', 140.00),
    (8, 8, 8, 'pending', 75.00),
    (9, 10, 9, 'confirmed', 105.00),
    (10, 7, 10, 'pending', 90.00);


INSERT INTO SpotRating (spot_id, user_id, rating, comment)
VALUES
    (1, 3, 5, 'Very convenient spot, easy to park!'),
    (2, 4, 4, 'Good spot but a bit tight for big cars.'),
    (3, 7, 5, 'Safe and covered. Loved it!'),
    (5, 10, 4, 'Good location, but a bit pricey.'),
    (7, 4, 5, 'Perfect for mall parking.');
