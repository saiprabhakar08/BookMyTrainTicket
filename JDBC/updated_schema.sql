-- BookMyTicket Train Booking System - UPDATED Complete Database Setup
-- MySQL Database Schema with Intermediate Stations and Stops Support

-- Create the database
CREATE DATABASE IF NOT EXISTS train_booking;
USE train_booking;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS waitlist;
DROP TABLE IF EXISTS rac;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS compartments;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS trains;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role ENUM('Admin', 'Regular', 'Senior', 'DifferentlyAbled') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create trains table
CREATE TABLE trains (
    train_id INT AUTO_INCREMENT PRIMARY KEY,
    train_name VARCHAR(100) NOT NULL,
    train_number VARCHAR(20) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create routes table with intermediate stations support
CREATE TABLE routes (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    source_station VARCHAR(100) NOT NULL,
    destination_station VARCHAR(100) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    distance_km INT,
    stops INT DEFAULT 0 COMMENT 'Number of intermediate stops',
    intermediate_stations TEXT COMMENT 'Comma-separated list of intermediate stations',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE
);

-- Create classes table (AC 1st, AC 2nd, AC 3rd, Sleeper, etc.)
CREATE TABLE classes (
    class_id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    class_type VARCHAR(50) NOT NULL,
    base_price_multiplier DECIMAL(3,2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE
);

-- Create compartments table
CREATE TABLE compartments (
    compartment_id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    compartment_name VARCHAR(50) NOT NULL,
    total_seats INT DEFAULT 24,
    capacity INT DEFAULT 0 COMMENT 'Maximum capacity of compartment',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
);

-- Create seats table
CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    compartment_id INT NOT NULL,
    berth_type ENUM('Lower', 'Middle', 'Upper', 'Side Lower', 'Side Upper') NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (compartment_id) REFERENCES compartments(compartment_id) ON DELETE CASCADE,
    UNIQUE KEY unique_seat_per_compartment (compartment_id, seat_number)
);

-- Create bookings table
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    seat_id INT,
    train_id INT NOT NULL,
    route_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Confirmed', 'Cancelled', 'RAC', 'Waiting') DEFAULT 'Confirmed',
    pnr_number VARCHAR(20) UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE SET NULL,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
);

-- Create payments table
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('Success', 'Failed', 'Pending') DEFAULT 'Pending',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    payment_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Create waitlist table
CREATE TABLE waitlist (
    waitlist_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    train_id INT NOT NULL,
    route_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Waiting', 'Promoted') DEFAULT 'Waiting',
    position INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
);

-- Create RAC (Reservation Against Cancellation) table
CREATE TABLE rac (
    rac_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    train_id INT NOT NULL,
    route_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('RAC', 'Promoted') DEFAULT 'RAC',
    position INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
);

-- Create station_info table for comprehensive station management
CREATE TABLE station_info (
    station_id INT AUTO_INCREMENT PRIMARY KEY,
    station_name VARCHAR(100) NOT NULL UNIQUE,
    station_code VARCHAR(10) UNIQUE,
    city VARCHAR(100),
    state VARCHAR(50),
    zone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_train_route ON bookings(train_id, route_id);
CREATE INDEX idx_seats_compartment ON seats(compartment_id);
CREATE INDEX idx_seats_available ON seats(is_available);
CREATE INDEX idx_routes_train ON routes(train_id);
CREATE INDEX idx_routes_stations ON routes(source_station, destination_station);
CREATE INDEX idx_routes_stops ON routes(stops);
CREATE INDEX idx_waitlist_train_route ON waitlist(train_id, route_id, position);
CREATE INDEX idx_rac_train_route ON rac(train_id, route_id, position);

-- Insert sample users
INSERT INTO users (username, password, email, role) VALUES
('admin', 'admin123', 'admin@bookmyticket.com', 'Admin'),
('john_doe', 'password123', 'john.doe@email.com', 'Regular'),
('senior_citizen', 'senior123', 'senior@email.com', 'Senior'),
('special_user', 'special123', 'special@email.com', 'DifferentlyAbled'),
('test_user', 'test123', 'test@email.com', 'Regular');

-- Insert sample trains
INSERT INTO trains (train_name, train_number) VALUES
('Rajdhani Express', '12301'),
('Shatabdi Express', '12002'),
('Duronto Express', '12259'),
('Gatimaan Express', '12049'),
('Vande Bharat Express', '22439');

-- Insert sample routes WITH intermediate stations
INSERT INTO routes (train_id, source_station, destination_station, departure_time, arrival_time, price, distance_km, stops, intermediate_stations) VALUES
(1, 'New Delhi', 'Mumbai Central', '16:55:00', '08:35:00', 1500.00, 1384, 3, 'Gwalior,Bhopal,Vadodara'),
(1, 'New Delhi', 'Ahmedabad', '16:55:00', '05:15:00', 1200.00, 934, 2, 'Mathura,Bharatpur'),
(2, 'New Delhi', 'Chandigarh', '17:20:00', '21:00:00', 800.00, 245, 1, 'Kurukshetra'),
(2, 'New Delhi', 'Amritsar', '17:20:00', '23:30:00', 950.00, 449, 2, 'Kurukshetra,Ludhiana'),
(3, 'Mumbai Central', 'Pune', '06:00:00', '09:30:00', 600.00, 192, 1, 'Lonavala'),
(3, 'Mumbai Central', 'Nagpur', '06:00:00', '17:45:00', 900.00, 825, 4, 'Nashik,Aurangabad,Akola,Amravati'),
(4, 'New Delhi', 'Agra Cantonment', '08:10:00', '09:50:00', 750.00, 188, 1, 'Mathura'),
(5, 'New Delhi', 'Varanasi', '06:00:00', '14:00:00', 1100.00, 765, 3, 'Kanpur,Allahabad,Mirzapur');

-- Insert sample station information
INSERT INTO station_info (station_name, station_code, city, state, zone) VALUES
('New Delhi', 'NDLS', 'New Delhi', 'Delhi', 'Northern Railway'),
('Mumbai Central', 'BCT', 'Mumbai', 'Maharashtra', 'Western Railway'),
('Chandigarh', 'CDG', 'Chandigarh', 'Chandigarh', 'Northern Railway'),
('Amritsar', 'ASR', 'Amritsar', 'Punjab', 'Northern Railway'),
('Pune', 'PUNE', 'Pune', 'Maharashtra', 'Central Railway'),
('Nagpur', 'NGP', 'Nagpur', 'Maharashtra', 'Central Railway'),
('Agra Cantonment', 'AGC', 'Agra', 'Uttar Pradesh', 'North Central Railway'),
('Varanasi', 'BSB', 'Varanasi', 'Uttar Pradesh', 'North Eastern Railway'),
('Gwalior', 'GWL', 'Gwalior', 'Madhya Pradesh', 'North Central Railway'),
('Bhopal', 'BPL', 'Bhopal', 'Madhya Pradesh', 'West Central Railway'),
('Vadodara', 'BRC', 'Vadodara', 'Gujarat', 'Western Railway'),
('Mathura', 'MTJ', 'Mathura', 'Uttar Pradesh', 'North Central Railway'),
('Bharatpur', 'BTE', 'Bharatpur', 'Rajasthan', 'North Western Railway'),
('Kurukshetra', 'KKDE', 'Kurukshetra', 'Haryana', 'Northern Railway'),
('Ludhiana', 'LDH', 'Ludhiana', 'Punjab', 'Northern Railway'),
('Lonavala', 'LNL', 'Lonavala', 'Maharashtra', 'Central Railway'),
('Nashik', 'NK', 'Nashik', 'Maharashtra', 'Central Railway'),
('Aurangabad', 'AWB', 'Aurangabad', 'Maharashtra', 'South Central Railway'),
('Akola', 'AK', 'Akola', 'Maharashtra', 'South East Central Railway'),
('Amravati', 'AMI', 'Amravati', 'Maharashtra', 'South East Central Railway'),
('Kanpur', 'CNB', 'Kanpur', 'Uttar Pradesh', 'North Central Railway'),
('Allahabad', 'ALD', 'Allahabad', 'Uttar Pradesh', 'North Central Railway'),
('Mirzapur', 'MZP', 'Mirzapur', 'Uttar Pradesh', 'North Eastern Railway');

-- Insert sample classes
INSERT INTO classes (train_id, class_type, base_price_multiplier) VALUES
(1, 'AC 1st Class', 3.00),
(1, 'AC 2 Tier', 2.00),
(1, 'AC 3 Tier', 1.50),
(2, 'AC Chair Car', 1.20),
(2, 'Executive Chair Car', 1.80),
(3, 'AC 3 Tier', 1.50),
(3, 'Sleeper', 1.00),
(4, 'AC Chair Car', 1.20),
(4, 'Executive Chair Car', 1.80),
(5, 'AC Chair Car', 1.20),
(5, 'Executive Chair Car', 1.80);

-- Insert sample compartments with capacity
INSERT INTO compartments (class_id, compartment_name, total_seats, capacity) VALUES
-- Rajdhani Express compartments
(1, 'H1', 18, 18),  -- AC 1st Class
(1, 'H2', 18, 18),
(2, 'A1', 46, 46),  -- AC 2 Tier
(2, 'A2', 46, 46),
(2, 'A3', 46, 46),
(3, 'B1', 64, 64),  -- AC 3 Tier
(3, 'B2', 64, 64),
(3, 'B3', 64, 64),

-- Shatabdi Express compartments
(4, 'CC1', 78, 78), -- AC Chair Car
(4, 'CC2', 78, 78),
(5, 'EC1', 52, 52), -- Executive Chair Car

-- Duronto Express compartments
(6, 'B1', 64, 64),  -- AC 3 Tier
(6, 'B2', 64, 64),
(7, 'S1', 72, 72),  -- Sleeper
(7, 'S2', 72, 72),

-- Gatimaan Express compartments
(8, 'CC1', 78, 78),  -- AC Chair Car
(9, 'EC1', 52, 52),  -- Executive Chair Car

-- Vande Bharat Express compartments
(10, 'C1', 78, 78), -- AC Chair Car
(11, 'EC1', 52, 52); -- Executive Chair Car

-- Insert sample seats (first few compartments)
-- AC 1st Class compartments (18 seats each)
INSERT INTO seats (compartment_id, berth_type, seat_number, is_available) VALUES
-- H1 compartment
(1, 'Lower', 'H1-1', TRUE), (1, 'Upper', 'H1-2', TRUE),
(1, 'Lower', 'H1-3', TRUE), (1, 'Upper', 'H1-4', TRUE),
(1, 'Lower', 'H1-5', TRUE), (1, 'Upper', 'H1-6', TRUE),
(1, 'Lower', 'H1-7', TRUE), (1, 'Upper', 'H1-8', TRUE),
(1, 'Lower', 'H1-9', TRUE), (1, 'Upper', 'H1-10', TRUE),
(1, 'Lower', 'H1-11', TRUE), (1, 'Upper', 'H1-12', TRUE),
(1, 'Lower', 'H1-13', TRUE), (1, 'Upper', 'H1-14', TRUE),
(1, 'Lower', 'H1-15', TRUE), (1, 'Upper', 'H1-16', TRUE),
(1, 'Lower', 'H1-17', TRUE), (1, 'Upper', 'H1-18', TRUE);

-- Insert some sample bookings
INSERT INTO bookings (user_id, seat_id, train_id, route_id, passenger_name, passenger_age, status, pnr_number) VALUES
(2, 1, 1, 1, 'John Doe', 30, 'Confirmed', 'PNR001234567'),
(3, 5, 1, 1, 'Senior Citizen', 65, 'Confirmed', 'PNR001234568'),
(4, 9, 1, 1, 'Special Passenger', 40, 'Confirmed', 'PNR001234569');

-- Update seat availability for booked seats
UPDATE seats SET is_available = FALSE WHERE seat_id IN (1, 5, 9);

-- Insert sample payments
INSERT INTO payments (booking_id, amount, status, payment_method, transaction_id) VALUES
(1, 1500.00, 'Success', 'Credit Card', 'TXN001234567'),
(2, 1500.00, 'Success', 'Debit Card', 'TXN001234568'),
(3, 1500.00, 'Success', 'UPI', 'TXN001234569');

-- Create comprehensive views (use CREATE OR REPLACE to handle existing views)
CREATE OR REPLACE VIEW route_details AS
SELECT 
    r.route_id,
    t.train_name,
    t.train_number,
    r.source_station,
    r.destination_station,
    r.departure_time,
    r.arrival_time,
    r.price,
    r.distance_km,
    r.stops,
    r.intermediate_stations,
    r.created_at
FROM routes r
JOIN trains t ON r.train_id = t.train_id
ORDER BY t.train_name, r.source_station;

CREATE OR REPLACE VIEW booking_details AS
SELECT 
    b.booking_id,
    b.pnr_number,
    u.username,
    u.role as user_role,
    t.train_name,
    t.train_number,
    r.source_station,
    r.destination_station,
    r.departure_time,
    r.arrival_time,
    r.price,
    r.stops,
    r.intermediate_stations,
    b.passenger_name,
    b.passenger_age,
    s.seat_number,
    s.berth_type,
    comp.compartment_name,
    cl.class_type,
    b.booking_time,
    b.status as booking_status,
    p.amount as payment_amount,
    p.status as payment_status
FROM bookings b
JOIN users u ON b.user_id = u.user_id
JOIN trains t ON b.train_id = t.train_id
JOIN routes r ON b.route_id = r.route_id
LEFT JOIN seats s ON b.seat_id = s.seat_id
LEFT JOIN compartments comp ON s.compartment_id = comp.compartment_id
LEFT JOIN classes cl ON comp.class_id = cl.class_id
LEFT JOIN payments p ON b.booking_id = p.booking_id;

-- Show summary
SELECT 'Database Setup Complete!' as Status;
SELECT 'Routes with Intermediate Stations:' as Info;
SELECT 
    CONCAT(source_station, ' → ', destination_station) as Route,
    stops,
    intermediate_stations
FROM routes 
WHERE stops > 0
ORDER BY train_id, route_id;
