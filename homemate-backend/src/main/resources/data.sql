-- ======================================================
-- HOMEMATE DATABASE POPULATION SCRIPT
-- ======================================================
-- This script populates the HomeMate database with sample data
-- Run this after creating the database schema

USE HomeMate;

-- ======================================================
-- POPULATE USERS
-- ======================================================
INSERT INTO Users (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES
('John', 'Smith', 'jsmith', '$2a$10$abcdefghijklmnopqrstuvwxyz123456', 'john.smith@email.com', '1990-05-15', 'M', '+1-555-0101', FALSE, FALSE),
('Sarah', 'Johnson', 'sjohnson', '$2a$10$abcdefghijklmnopqrstuvwxyz123457', 'sarah.j@email.com', '1988-08-22', 'F', '+1-555-0102', FALSE, FALSE),
('Michael', 'Brown', 'mbrown', '$2a$10$abcdefghijklmnopqrstuvwxyz123458', 'michael.b@email.com', '1992-03-10', 'M', '+1-555-0103', FALSE, FALSE),
('Emily', 'Davis', 'edavis', '$2a$10$abcdefghijklmnopqrstuvwxyz123459', 'emily.davis@email.com', '1995-11-30', 'F', '+1-555-0104', FALSE, FALSE),
('David', 'Wilson', 'dwilson', '$2a$10$abcdefghijklmnopqrstuvwxyz123460', 'david.w@email.com', '1987-07-18', 'M', '+1-555-0105', FALSE, FALSE),
('Jessica', 'Martinez', 'jmartinez', '$2a$10$abcdefghijklmnopqrstuvwxyz123461', 'jessica.m@email.com', '1993-01-25', 'F', '+1-555-0106', FALSE, FALSE),
('Robert', 'Garcia', 'rgarcia', '$2a$10$abcdefghijklmnopqrstuvwxyz123462', 'robert.g@email.com', '1991-09-05', 'M', '+1-555-0107', FALSE, FALSE),
('Amanda', 'Rodriguez', 'arodriguez', '$2a$10$abcdefghijklmnopqrstuvwxyz123463', 'amanda.r@email.com', '1989-12-12', 'F', '+1-555-0108', FALSE, FALSE),
('Admin', 'User', 'admin',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@homemate.com', '1985-06-20', 'M', '+1-555-0001', TRUE, FALSE),
('Chris', 'Taylor', 'ctaylor', '$2a$10$abcdefghijklmnopqrstuvwxyz123465', 'chris.t@email.com', '1994-04-08', 'M', '+1-555-0109', FALSE, FALSE);

-- ======================================================
-- POPULATE ADDRESSES
-- ======================================================
INSERT INTO Address (userID, country, city, street, apartment) VALUES
(1, 'USA', 'New York', '123 Main Street', 'Apt 4B'),
(1, 'USA', 'New York', '456 Park Avenue', NULL),
(2, 'USA', 'Los Angeles', '789 Sunset Blvd', 'Suite 12'),
(3, 'USA', 'Chicago', '321 Lake Shore Dr', 'Unit 5A'),
(4, 'USA', 'Houston', '654 Texas Avenue', NULL),
(5, 'USA', 'Phoenix', '987 Desert Road', 'Apt 2C'),
(6, 'USA', 'Philadelphia', '147 Liberty Street', NULL),
(7, 'USA', 'San Antonio', '258 River Walk', 'Unit 8'),
(8, 'USA', 'San Diego', '369 Beach Boulevard', 'Apt 3D'),
(10, 'USA', 'Dallas', '741 Commerce Street', NULL);

-- ======================================================
-- POPULATE SERVICES
-- ======================================================
INSERT INTO Service (name, description, imageData, imageName, imageType) VALUES
('House Cleaning', 'Professional cleaning services for your home including dusting, vacuuming, and sanitizing.', NULL, 'cleaning.jpg', 'image/jpeg'),
('Plumbing', 'Expert plumbing services for repairs, installations, and maintenance.', NULL, 'plumbing.jpg', 'image/jpeg'),
('Electrical Work', 'Licensed electricians for all your electrical needs and repairs.', NULL, 'electrical.jpg', 'image/jpeg'),
('Gardening', 'Lawn care, landscaping, and garden maintenance services.', NULL, 'gardening.jpg', 'image/jpeg'),
('Painting', 'Interior and exterior painting services with quality finishes.', NULL, 'painting.jpg', 'image/jpeg'),
('Carpentry', 'Custom woodwork, furniture repair, and carpentry services.', NULL, 'carpentry.jpg', 'image/jpeg'),
('Moving & Delivery', 'Reliable moving and delivery services for your belongings.', NULL, 'moving.jpg', 'image/jpeg'),
('HVAC Services', 'Heating, ventilation, and air conditioning installation and repair.', NULL, 'hvac.jpg', 'image/jpeg'),
('Pet Care', 'Pet sitting, dog walking, and pet care services.', NULL, 'petcare.jpg', 'image/jpeg'),
('Tutoring', 'Educational tutoring services for various subjects and levels.', NULL, 'tutoring.jpg', 'image/jpeg'),
('Cooking Services', 'Home-cooked meals, meal prep, and private chef services.', NULL, 'cooking.jpg', 'image/jpeg'),
('IT Support', 'Troubleshooting computers, networks, and software.', NULL, 'it_support.jpg', 'image/jpeg'),
('Car Washing', 'Mobile car washing and detailing at your location.', NULL, 'carwash.jpg', 'image/jpeg'),
('Home Repair', 'Handyman services for home fixes and repairs.', NULL, 'homerepair.jpg', 'image/jpeg'),
('Fitness Training', 'Personal trainers for workouts, weight loss, and strength building.', NULL, 'fitness.jpg', 'image/jpeg'),
('Baby Sitting', 'Responsible childcare and babysitting services.', NULL, 'babysitting.jpg', 'image/jpeg'),
('Elder Care', 'Caregivers providing support for elderly people.', NULL, 'eldercare.jpg', 'image/jpeg'),
('Interior Design', 'Home interior decoration and design services.', NULL, 'interiordesign.jpg', 'image/jpeg'),
('Photography', 'Professional photography for events and personal shoots.', NULL, 'photography.jpg', 'image/jpeg'),
('Home Security Installation', 'Installation of cameras, alarms, and security systems.', NULL, 'security.jpg', 'image/jpeg');
-- ======================================================
-- POPULATE TASKERS
-- ======================================================
INSERT INTO Tasker (firstName, lastName, username, password, email, birthDate, phone, gender, image, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES
('Tom', 'Anderson', 'tanderson', '$2a$10$tasker123456789abcdefghij', 'tom.a@tasker.com', '1985-03-20', '+1-555-1001', 'M', NULL, 'available', 4.8, 35.00, 'Experienced cleaner with 10 years in the business.', 1, 15400.00, 440.00, 'New York'),
('Lisa', 'Thompson', 'lthompson', '$2a$10$tasker223456789abcdefghij', 'lisa.t@tasker.com', '1982-07-14', '+1-555-1002', 'F', NULL, 'available', 4.9, 55.00, 'Licensed plumber with expertise in residential repairs.', 1, 28600.00, 520.00, 'Los Angeles'),
('Mark', 'White', 'mwhite', '$2a$10$tasker323456789abcdefghij', 'mark.w@tasker.com', '1988-11-02', '+1-555-1003', 'M', NULL, 'available', 4.7, 60.00, 'Certified electrician, safety is my priority.', 1, 19800.00, 330.00, 'Chicago'),
('Karen', 'Harris', 'kharris', '$2a$10$tasker423456789abcdefghij', 'karen.h@tasker.com', '1990-05-28', '+1-555-1004', 'F', NULL, 'available', 4.6, 30.00, 'Passionate gardener with a green thumb.', 1, 9600.00, 320.00, 'Houston'),
('James', 'Clark', 'jclark', '$2a$10$tasker523456789abcdefghij', 'james.c@tasker.com', '1986-09-17', '+1-555-1005', 'M', NULL, 'available', 4.9, 45.00, 'Professional painter with attention to detail.', 1, 22500.00, 500.00, 'Phoenix'),
('Nancy', 'Lewis', 'nlewis', '$2a$10$tasker623456789abcdefghij', 'nancy.l@tasker.com', '1984-12-08', '+1-555-1006', 'F', NULL, 'unavailable', 4.5, 50.00, 'Skilled carpenter, custom work specialist.', 1, 17000.00, 340.00, 'Philadelphia'),
('Steve', 'Walker', 'swalker', '$2a$10$tasker723456789abcdefghij', 'steve.w@tasker.com', '1992-02-22', '+1-555-1007', 'M', NULL, 'available', 4.4, 40.00, 'Reliable moving services, careful with your items.', 1, 12800.00, 320.00, 'San Antonio'),
('Patricia', 'Hall', 'phall', '$2a$10$tasker823456789abcdefghij', 'patricia.h@tasker.com', '1987-06-30', '+1-555-1008', 'F', NULL, 'available', 4.8, 65.00, 'HVAC expert with 15 years experience.', 1, 26000.00, 400.00, 'San Diego'),
('Daniel', 'Allen', 'dallen', '$2a$10$tasker923456789abcdefghij', 'daniel.a@tasker.com', '1995-08-11', '+1-555-1009', 'M', NULL, 'available', 4.7, 25.00, 'Animal lover providing quality pet care.', 1, 7500.00, 300.00, 'Dallas'),
('Maria', 'Young', 'myoung', '$2a$10$tasker1023456789abcdefghi', 'maria.y@tasker.com', '1989-10-19', '+1-555-1010', 'F', NULL, 'available', 4.9, 40.00, 'Experienced tutor in math and science.', 1, 16000.00, 400.00, 'New York'),
('Oliver', 'King', 'oking', '$2a$10$tasker113456789abcdefghij', 'oliver.k@tasker.com', '1991-04-12', '+1-555-1011', 'M', NULL, 'available', 4.6, 45.00, 'Home cleaning expert.', 1, 14500.00, 380.00, 'New York'),
('Emily', 'Green', 'egreen', '$2a$10$tasker123456789abcdefghij', 'emily.g@tasker.com', '1993-06-22', '+1-555-1012', 'F', NULL, 'available', 4.8, 60.00, 'Licensed plumber with 6 years of experience.', 1, 18200.00, 410.00, 'Los Angeles'),
('Henry', 'Moore', 'hmoore', '$2a$10$tasker133456789abcdefghij', 'henry.m@tasker.com', '1987-02-10', '+1-555-1013', 'M', NULL, 'available', 4.5, 58.00, 'Electrical expert.', 1, 20000.00, 440.00, 'Chicago'),
('Sophia', 'Baker', 'sbaker', '$2a$10$tasker143456789abcdefghij', 'sophia.b@tasker.com', '1994-03-18', '+1-555-1014', 'F', NULL, 'unavailable', 4.7, 32.00, 'Gardening and lawn care specialist.', 1, 10800.00, 300.00, 'Houston'),
('Ethan', 'Reed', 'ereed', '$2a$10$tasker153456789abcdefghij', 'ethan.r@tasker.com', '1990-07-09', '+1-555-1015', 'M', NULL, 'available', 4.9, 48.00, 'Professional painter.', 1, 24000.00, 500.00, 'Phoenix'),
('Grace', 'Price', 'gprice', '$2a$10$tasker163456789abcdefghij', 'grace.p@tasker.com', '1985-01-27', '+1-555-1016', 'F', NULL, 'available', 4.6, 52.00, 'Experienced carpenter.', 1, 19400.00, 460.00, 'Philadelphia'),
('Logan', 'Wright', 'lwright', '$2a$10$tasker173456789abcdefghij', 'logan.w@tasker.com', '1991-10-13', '+1-555-1017', 'M', NULL, 'unavailable', 4.4, 42.00, 'Moving & delivery specialist.', 1, 11000.00, 270.00, 'San Antonio'),
('Victoria', 'Brooks', 'vbrooks', '$2a$10$tasker183456789abcdefghij', 'victoria.b@tasker.com', '1989-11-07', '+1-555-1018', 'F', NULL, 'available', 4.8, 70.00, 'HVAC engineer.', 8, 28000.00, 420.00, 'San Diego'),
('Matthew', 'Evans', 'mevans', '$2a$10$tasker193456789abcdefghij', 'matt.e@tasker.com', '1994-05-16', '+1-555-1019', 'M', NULL, 'available', 4.9, 30.00, 'Pet care expert.', 9, 13500.00, 380.00, 'Dallas'),
('Hannah', 'Rivera', 'hrivera', '$2a$10$tasker203456789abcdefghij', 'hannah.r@tasker.com', '1992-08-23', '+1-555-1020', 'F', NULL, 'available', 4.7, 45.00, 'Academic tutor.', 10, 17500.00, 390.00, 'New York'),

('Noah', 'Scott', 'nscott', '$2a$10$tasker213456789abcdefghij', 'noah.s@tasker.com', '1990-01-11', '+1-555-1021', 'M', NULL, 'available', 4.6, 35.00, 'Chef and meal prep expert.', 11, 12000.00, 330.00, 'Los Angeles'),
('Ava', 'Cooper', 'acooper', '$2a$10$tasker223456789abcdefghij', 'ava.c@tasker.com', '1995-09-30', '+1-555-1022', 'F', NULL, 'available', 4.8, 55.00, 'IT support specialist.', 12, 21000.00, 450.00, 'Chicago'),
('Liam', 'Ward', 'lward', '$2a$10$tasker233456789abcdefghij', 'liam.w@tasker.com', '1988-12-03', '+1-555-1023', 'M', NULL, 'available', 4.5, 25.00, 'Car washing and detailing.', 13, 8000.00, 260.00, 'Houston'),
('Chloe', 'Cruz', 'ccruz', '$2a$10$tasker243456789abcdefghij', 'chloe.c@tasker.com', '1991-06-08', '+1-555-1024', 'F', NULL, 'available', 4.9, 40.00, 'Home repair handyman.', 14, 16000.00, 340.00, 'Phoenix'),
('Daniel', 'Bell', 'dbell', '$2a$10$tasker253456789abcdefghij', 'daniel.b@tasker.com', '1986-02-19', '+1-555-1025', 'M', NULL, 'available', 4.7, 65.00, 'Fitness trainer.', 15, 30000.00, 470.00, 'Philadelphia'),
('Ella', 'Murphy', 'emurphy', '$2a$10$tasker263456789abcdefghij', 'ella.m@tasker.com', '1994-07-14', '+1-555-1026', 'F', NULL, 'available', 4.6, 30.00, 'Babysitter with 5 years experience.', 16, 9000.00, 250.00, 'San Antonio'),
('Mason', 'Cook', 'mcook', '$2a$10$tasker273456789abcdefghij', 'mason.c@tasker.com', '1993-09-25', '+1-555-1027', 'M', NULL, 'unavailable', 4.8, 55.00, 'Elder care specialist.', 17, 22000.00, 390.00, 'San Diego'),
('Zoe', 'Morgan', 'zmorgan', '$2a$10$tasker283456789abcdefghij', 'zoe.m@tasker.com', '1992-04-04', '+1-555-1028', 'F', NULL, 'available', 4.9, 75.00, 'Interior designer.', 18, 31000.00, 410.00, 'Dallas'),
('Jack', 'Lee', 'jlee', '$2a$10$tasker293456789abcdefghij', 'jack.l@tasker.com', '1991-03-12', '+1-555-1029', 'M', NULL, 'available', 4.6, 80.00, 'Event photographer.', 19, 26000.00, 390.00, 'New York'),
('Layla', 'Turner', 'lturner', '$2a$10$tasker303456789abcdefghij', 'layla.t@tasker.com', '1990-11-29', '+1-555-1030', 'F', NULL, 'available', 4.7, 55.00, 'Home security installer.', 20, 17800.00, 350.00, 'Los Angeles'),
('Aaron', 'Mitchell', 'amitchell', '$2a$10$tasker313456789abcdefghij', 'aaron.m@tasker.com', '1987-05-12', '+1-555-1031', 'M', NULL, 'available', 4.5, 33.00, 'Cleaner.', 1, 11200.00, 300.00, 'Houston'),
('Natalie', 'Ross', 'nross', '$2a$10$tasker323456789abcdefghij', 'natalie.r@tasker.com', '1993-10-01', '+1-555-1032', 'F', NULL, 'available', 4.9, 58.00, 'Plumber.', 2, 21000.00, 430.00, 'Chicago'),
('Wyatt', 'Perry', 'wperry', '$2a$10$tasker333456789abcdefghij', 'wyatt.p@tasker.com', '1990-06-22', '+1-555-1033', 'M', NULL, 'available', 4.7, 65.00, 'Electrician.', 3, 24000.00, 450.00, 'Los Angeles'),
('Bella', 'Foster', 'bfoster', '$2a$10$tasker343456789abcdefghij', 'bella.f@tasker.com', '1991-01-18', '+1-555-1034', 'F', NULL, 'available', 4.8, 35.00, 'Gardener.', 4, 13200.00, 280.00, 'Phoenix'),
('Owen', 'Howard', 'ohoward', '$2a$10$tasker353456789abcdefghij', 'owen.h@tasker.com', '1989-03-03', '+1-555-1035', 'M', NULL, 'available', 4.9, 48.00, 'Painter.', 5, 20000.00, 420.00, 'New York'),
('Aria', 'Ward', 'award', '$2a$10$tasker363456789abcdefghij', 'aria.w@tasker.com', '1992-02-11', '+1-555-1036', 'F', NULL, 'available', 4.8, 52.00, 'Carpenter.', 6, 18500.00, 380.00, 'Philadelphia'),
('Carter', 'Fleming', 'cfleming', '$2a$10$tasker373456789abcdefghij', 'carter.f@tasker.com', '1994-09-27', '+1-555-1037', 'M', NULL, 'available', 4.6, 44.00, 'Mover.', 7, 12500.00, 310.00, 'Houston'),
('Hailey', 'Summers', 'hsummers', '$2a$10$tasker383456789abcdefghij', 'hailey.s@tasker.com', '1993-07-05', '+1-555-1038', 'F', NULL, 'available', 4.9, 72.00, 'HVAC technician.', 8, 28000.00, 430.00, 'Chicago'),
('Gabriel', 'Bryant', 'gbryant', '$2a$10$tasker393456789abcdefghij', 'gabriel.b@tasker.com', '1991-12-14', '+1-555-1039', 'M', NULL, 'available', 4.7, 26.00, 'Pet care expert.', 9, 9800.00, 300.00, 'Dallas'),
('Stella', 'Cole', 'scole', '$2a$10$tasker403456789abcdefghij', 'stella.c@tasker.com', '1990-04-21', '+1-555-1040', 'F', NULL, 'available', 4.8, 48.00, 'Tutor.', 10, 15000.00, 350.00, 'New York');
-- ======================================================
-- POPULATE CHATS
-- ======================================================
INSERT INTO Chat (userID, taskerID, userIsActive, taskerIsActive) VALUES
(1, 1, TRUE, TRUE),
(2, 2, TRUE, TRUE),
(3, 3, TRUE, TRUE),
(4, 4, TRUE, TRUE),
(5, 5, TRUE, TRUE),
(1, 2, TRUE, TRUE),
(2, 3, TRUE, FALSE),
(6, 6, TRUE, TRUE),
(7, 7, TRUE, TRUE),
(8, 8, TRUE, TRUE);

-- ======================================================
-- POPULATE TASKS
-- ======================================================
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-01 09:00:00', 4, 1, 1, 1, '2024-11-01 13:00:00', 1, 140.00, 'Done', '2024-11-01 09:00:00', 1, 'Deep cleaning of entire apartment'),
('2024-11-03 10:00:00', 3, 2, 2, 2, '2024-11-03 13:00:00', 2, 165.00, 'Done', '2024-11-03 10:00:00', 3, 'Fix leaking kitchen faucet'),
('2024-11-05 14:00:00', 5, 3, 3, 3, '2024-11-05 19:00:00', 3, 300.00, 'Done', '2024-11-05 14:00:00', 4, 'Install new light fixtures in living room'),
('2024-11-07 08:00:00', 6, 4, 4, 4, '2024-11-07 14:00:00', 4, 180.00, 'Done', '2024-11-07 08:00:00', 5, 'Lawn mowing and hedge trimming'),
('2024-11-10 09:00:00', 8, 5, 5, 5, '2024-11-10 17:00:00', 5, 360.00, 'Done', '2024-11-10 09:00:00', 6, 'Paint bedroom walls'),
('2024-11-12 10:00:00', 0, 1, 2, 2, NULL, 6, 0, 'Accepted', NULL, 2, 'Bathroom sink repair'),
('2024-11-14 11:00:00', 2, 2, 3, 3, NULL, 7, 120.00, 'InProgress', '2024-11-14 11:00:00', 3, 'Replace electrical outlet'),
('2024-11-15 09:00:00', 0, 6, 6, 6, NULL, 8, 0, 'InReview', NULL, 7, 'Build custom bookshelf'),
('2024-11-16 13:00:00', 0, 7, 7, 7, NULL, 9, 0, 'InReview', NULL, 8, 'Move furniture to new apartment'),
('2024-11-08 15:00:00', 4, 8, 8, 8, '2024-11-08 19:00:00', 10, 260.00, 'Done', '2024-11-08 15:00:00', 9, 'AC maintenance and filter replacement'),
('2024-11-18 10:00:00', 0, 1, 1, 3, NULL, 1, 0, 'InReview', NULL, 1, 'Install ceiling fan'),
('2024-11-19 14:00:00', 0, 1, 1, 4, NULL, 1, 0, 'Accepted', NULL, 1, 'Garden maintenance'),
('2024-11-20 09:00:00', 3, 1, 1, 5, '2024-11-20 12:00:00', 1, 180.00, 'Done', '2024-11-20 09:00:00', 1, 'Interior painting touch-up'),
('2024-11-21 11:00:00', 0, 1, 2, 1, NULL, 1, 0, 'InReview', NULL, 2, 'Window cleaning'),
('2024-11-22 08:00:00', 0, 1, 1, 2, NULL, 1, 0, 'Accepted', NULL, 2, 'Pipe leak repair'),
('2024-11-23 15:00:00', 2, 1, 1, 3, NULL, 1, 100.00, 'InProgress', '2024-11-23 15:00:00', 1, 'Light switch installation'),
('2024-11-17 13:00:00', 5, 1, 1, 2, '2024-11-17 18:00:00', 1, 250.00, 'Done', '2024-11-17 13:00:00', 1, 'Kitchen faucet replacement');

-- ======================================================
-- POPULATE REVIEWS
-- ======================================================
INSERT INTO Reviews (text, rate, time, taskID) VALUES
('Excellent service, very thorough!', 5.0, '2024-11-01 14:00:00', 1),
('Quick and professional work', 4.5, '2024-11-03 14:00:00', 2),
('Great job, very knowledgeable', 4.8, '2024-11-05 20:00:00', 3),
('Good work, on time', 4.3, '2024-11-07 15:00:00', 4),
('Amazing painting skills!', 5.0, '2024-11-10 18:00:00', 5),
('Very professional and efficient', 4.9, '2024-11-08 20:00:00', 10);

-- ======================================================
-- POPULATE REVIEW IMAGES
-- ======================================================
INSERT INTO ReviewImage (format, ImageFile, ImageName, reviewID) VALUES
('jpg', 0x89504E470D0A1A0A, 'before_after_cleaning.jpg', 1),
('jpg', 0x89504E470D0A1A0A, 'fixed_faucet.jpg', 2),
('jpg', 0x89504E470D0A1A0A, 'new_lights.jpg', 3),
('jpg', 0x89504E470D0A1A0A, 'lawn_result.jpg', 4);

-- ======================================================
-- POPULATE MESSAGES
-- ======================================================
INSERT INTO Message (chatID, content, timestamp, senderID, receiverID, isUserSender, status) VALUES
(1, 'Hi, I need my apartment cleaned this week', '2024-10-31 08:00:00', 1, 1, TRUE, 'seen'),
(1, 'Hello! I can help with that. When works for you?', '2024-10-31 08:15:00', 1, 1, FALSE, 'seen'),
(1, 'How about Friday at 9 AM?', '2024-10-31 08:20:00', 1, 1, TRUE, 'seen'),
(1, 'Perfect, I will be there!', '2024-10-31 08:25:00', 1, 1, FALSE, 'seen'),
(2, 'I have a leaking faucet in my kitchen', '2024-11-02 09:00:00', 2, 2, TRUE, 'seen'),
(2, 'I can fix that. Is Saturday morning good?', '2024-11-02 09:30:00', 2, 2, FALSE, 'seen'),
(2, 'Yes, that works great!', '2024-11-02 10:00:00', 2, 2, TRUE, 'seen'),
(3, 'Need help with electrical work', '2024-11-04 10:00:00', 3, 3, TRUE, 'seen'),
(3, 'What kind of work do you need done?', '2024-11-04 10:15:00', 3, 3, FALSE, 'seen'),
(3, 'Installing new light fixtures', '2024-11-04 10:20:00', 3, 3, TRUE, 'seen'),
(6, 'Can you help with my bathroom sink?', '2024-11-11 14:00:00', 1, 2, TRUE, 'received'),
(6, 'Yes, I can come tomorrow', '2024-11-11 14:30:00', 1, 2, FALSE, 'seen');

-- ======================================================
-- POPULATE MESSAGE IMAGES
-- ======================================================
INSERT INTO MessageImage (messageID, format, ImageFile, ImageName) VALUES
(1, 'jpg', 0x89504E470D0A1A0A, 'apartment_photo.jpg'),
(5, 'jpg', 0x89504E470D0A1A0A, 'leaking_faucet.jpg'),
(8, 'jpg', 0x89504E470D0A1A0A, 'electrical_outlet.jpg');

-- ======================================================
-- POPULATE REPORTS
-- ======================================================
INSERT INTO Report (header, body, taskID, reporter, adminStatus) VALUES
('Late arrival', 'Tasker arrived 30 minutes late without prior notice', 4, TRUE, 'pending'),
('Payment issue', 'User has not paid the full amount agreed upon', 7, FALSE, 'pending'),
('Incomplete work', 'The cleaning was not thorough, several areas were missed', 1, TRUE, 'done');

-- ======================================================
-- SUMMARY
-- ======================================================
-- Data inserted:
-- - 10 Users (including 1 admin)
-- - 10 Addresses
-- - 10 Services
-- - 10 Taskers
-- - 10 Chats
-- - 10 Tasks (6 completed, 2 in progress/accepted, 2 in review)
-- - 6 Reviews (for completed tasks)
-- - 4 Review Images
-- - 12 Messages
-- - 3 Message Images
-- - 3 Reports (2 pending, 1 resolved)
-- ======================================================