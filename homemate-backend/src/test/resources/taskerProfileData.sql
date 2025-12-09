
-- Insert Services
INSERT INTO Service (serviceID, name, description) VALUES 
(1, 'Plumbing', 'Professional plumbing services'),
(2, 'Electrical', 'Professional electrical services');

-- Insert Taskers
-- 1. Standard Tasker
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(1, 'John', 'Doe', 'johndoe', 'password123', 'john.doe@email.com', '1990-05-15 00:00:00', '+1-555-0101', 'M', 'available', 4.5, 50.0, 'Experienced tasker', 1, 1000.0, 20.0, 'New York');

-- 2. Standard Tasker (Female, Unavailable)
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(2, 'Jane', 'Smith', 'janesmith', 'password456', 'jane.smith@email.com', '1988-08-22 00:00:00', '+1-555-0102', 'F', 'unavailable', 4.8, 60.0, 'Professional service', 1, 2000.0, 35.0, 'Los Angeles');

-- 3. Max length username
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(3, 'Max', 'User', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'password789', 'max.user@email.com', '1992-03-10 00:00:00', '+1-555-0103', 'M', 'available', 4.0, 45.0, 'Test bio', 1, 500.0, 10.0, 'Chicago');

-- 4. Max length email
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(4, 'Email', 'Test', 'emailtest', 'password123', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@email.com', '1995-11-30 00:00:00', '+1-555-0104', 'F', 'available', 4.2, 55.0, 'Email test', 2, 750.0, 15.0, 'Houston');

-- 5. Max length phone
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(5, 'Phone', 'Test', 'phonetest', 'password999', 'phone.test@email.com', '1987-07-18 00:00:00', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'M', 'available', 4.7, 65.0, 'Phone test', 1, 1500.0, 25.0, 'Phoenix');

-- 6. Max length bio (500 chars)
-- Note: 'a' * 500 is very long. I'll construct it.
-- Using H2 string repeat function or manual. REPEAT('a', 500) works in H2.
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(6, 'Bio', 'Test', 'biotest', 'password111', 'bio.test@email.com', '1993-01-25 00:00:00', '+1-555-0105', 'F', 'unavailable', 4.9, 70.0, REPEAT('a', 500), 2, 3000.0, 50.0, 'Philadelphia');

-- 7. Null bio
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(7, 'Null', 'Bio', 'nullbio', 'password222', 'null.bio@email.com', '1991-09-05 00:00:00', '+1-555-0106', 'M', 'available', 3.5, 40.0, NULL, 1, 200.0, 5.0, 'San Antonio');

-- 8. Null phone
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(8, 'Null', 'Phone', 'nullphone', 'password333', 'null.phone@email.com', '1989-12-12 00:00:00', NULL, 'F', 'available', 4.3, 55.0, 'No phone', 2, 800.0, 18.0, 'San Diego');

-- 9. Null gender
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(9, 'Null', 'Gender', 'nullgender', 'password444', 'null.gender@email.com', '1994-04-08 00:00:00', '+1-555-0107', NULL, 'available', 4.6, 58.0, 'No gender', 1, 1200.0, 22.0, 'Dallas');

-- 10. Minimum values
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(10, 'A', 'B', 'ab', 'p', 'a@b.c', '2000-01-01 00:00:00', '1', 'M', 'available', 0.0, 0.0, 'Min', 1, 0.0, 0.0, 'A');

-- 11. High rating
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(11, 'High', 'Rating', 'highrating', 'password555', 'high.rating@email.com', '1985-06-20 00:00:00', '+1-555-0108', 'M', 'available', 5.0, 100.0, 'Perfect rating', 2, 10000.0, 100.0, 'San Jose');

-- 12. No service (uses Service 1 in test setup but desc says 'No service')
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES 
(12, 'No', 'Service', 'noservice', 'password666', 'no.service@email.com', '1996-02-14 00:00:00', '+1-555-0109', 'F', 'unavailable', 3.0, 30.0, 'No service', 1, 100.0, 3.0, 'Austin');
