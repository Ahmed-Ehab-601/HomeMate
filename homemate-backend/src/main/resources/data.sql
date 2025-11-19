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
('Tutoring', 'Educational tutoring services for various subjects and levels.', NULL, 'tutoring.jpg', 'image/jpeg');

-- ======================================================
-- POPULATE TASKERS
-- ======================================================
INSERT INTO Taskers (firstName, lastName, username, password, email, birthDate, phone, gender, image, availability, rating, hourrate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES
('Tom', 'Anderson', 'tanderson', '$2a$10$tasker123456789abcdefghij', 'tom.a@tasker.com', '1985-03-20', '+1-555-1001', 'M', NULL, 'available', 4.8, 35.00, 'Experienced cleaner with 10 years in the business.', 1, 15400.00, 440.00, 'New York'),
('Lisa', 'Thompson', 'lthompson', '$2a$10$tasker223456789abcdefghij', 'lisa.t@tasker.com', '1982-07-14', '+1-555-1002', 'F', NULL, 'available', 4.9, 55.00, 'Licensed plumber with expertise in residential repairs.', 2, 28600.00, 520.00, 'Los Angeles'),
('Mark', 'White', 'mwhite', '$2a$10$tasker323456789abcdefghij', 'mark.w@tasker.com', '1988-11-02', '+1-555-1003', 'M', NULL, 'available', 4.7, 60.00, 'Certified electrician, safety is my priority.', 3, 19800.00, 330.00, 'Chicago'),
('Karen', 'Harris', 'kharris', '$2a$10$tasker423456789abcdefghij', 'karen.h@tasker.com', '1990-05-28', '+1-555-1004', 'F', NULL, 'available', 4.6, 30.00, 'Passionate gardener with a green thumb.', 4, 9600.00, 320.00, 'Houston'),
('James', 'Clark', 'jclark', '$2a$10$tasker523456789abcdefghij', 'james.c@tasker.com', '1986-09-17', '+1-555-1005', 'M', NULL, 'available', 4.9, 45.00, 'Professional painter with attention to detail.', 5, 22500.00, 500.00, 'Phoenix'),
('Nancy', 'Lewis', 'nlewis', '$2a$10$tasker623456789abcdefghij', 'nancy.l@tasker.com', '1984-12-08', '+1-555-1006', 'F', NULL, 'unavailable', 4.5, 50.00, 'Skilled carpenter, custom work specialist.', 6, 17000.00, 340.00, 'Philadelphia'),
('Steve', 'Walker', 'swalker', '$2a$10$tasker723456789abcdefghij', 'steve.w@tasker.com', '1992-02-22', '+1-555-1007', 'M', NULL, 'available', 4.4, 40.00, 'Reliable moving services, careful with your items.', 7, 12800.00, 320.00, 'San Antonio'),
('Patricia', 'Hall', 'phall', '$2a$10$tasker823456789abcdefghij', 'patricia.h@tasker.com', '1987-06-30', '+1-555-1008', 'F', NULL, 'available', 4.8, 65.00, 'HVAC expert with 15 years experience.', 8, 26000.00, 400.00, 'San Diego'),
('Daniel', 'Allen', 'dallen', '$2a$10$tasker923456789abcdefghij', 'daniel.a@tasker.com', '1995-08-11', '+1-555-1009', 'M', NULL, 'available', 4.7, 25.00, 'Animal lover providing quality pet care.', 9, 7500.00, 300.00, 'Dallas'),
('Maria', 'Young', 'myoung', '$2a$10$tasker1023456789abcdefghi', 'maria.y@tasker.com', '1989-10-19', '+1-555-1010', 'F', NULL, 'available', 4.9, 40.00, 'Experienced tutor in math and science.', 10, 16000.00, 400.00, 'New York');

-- ======================================================
-- POPULATE CHATS
-- ======================================================
INSERT INTO Chat (user_id, tasker_id, useris_active, taskeris_active) VALUES
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
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, finishDate, chatID, bill, status, startInprogress, addressID, descriptionNotes) VALUES
('2024-11-01 09:00:00', 4, 1, 1, 1, '2024-11-01 13:00:00', 1, 140.00, 'done', '2024-11-01 09:00:00', 1, 'Deep cleaning of entire apartment'),
('2024-11-03 10:00:00', 3, 2, 2, 2, '2024-11-03 13:00:00', 2, 165.00, 'done', '2024-11-03 10:00:00', 3, 'Fix leaking kitchen faucet'),
('2024-11-05 14:00:00', 5, 3, 3, 3, '2024-11-05 19:00:00', 3, 300.00, 'done', '2024-11-05 14:00:00', 4, 'Install new light fixtures in living room'),
('2024-11-07 08:00:00', 6, 4, 4, 4, '2024-11-07 14:00:00', 4, 180.00, 'done', '2024-11-07 08:00:00', 5, 'Lawn mowing and hedge trimming'),
('2024-11-10 09:00:00', 8, 5, 5, 5, '2024-11-10 17:00:00', 5, 360.00, 'done', '2024-11-10 09:00:00', 6, 'Paint bedroom walls'),
('2024-11-12 10:00:00', 0, 1, 2, 2, NULL, 6, 0, 'Accepted', NULL, 2, 'Bathroom sink repair'),
('2024-11-14 11:00:00', 2, 2, 3, 3, NULL, 7, 120.00, 'Inprogress', '2024-11-14 11:00:00', 3, 'Replace electrical outlet'),
('2024-11-15 09:00:00', 0, 6, 6, 6, NULL, 8, 0, 'inReview', NULL, 7, 'Build custom bookshelf'),
('2024-11-16 13:00:00', 0, 7, 7, 7, NULL, 9, 0, 'inReview', NULL, 8, 'Move furniture to new apartment'),
('2024-11-08 15:00:00', 4, 8, 8, 8, '2024-11-08 19:00:00', 10, 260.00, 'done', '2024-11-08 15:00:00', 9, 'AC maintenance and filter replacement');

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
INSERT INTO Review_Image (format, ImgFile, ImgName, review_id) VALUES
('jpg', 0x89504E470D0A1A0A, 'before_after_cleaning.jpg', 1),
('jpg', 0x89504E470D0A1A0A, 'fixed_faucet.jpg', 2),
('jpg', 0x89504E470D0A1A0A, 'new_lights.jpg', 3),
('jpg', 0x89504E470D0A1A0A, 'lawn_result.jpg', 4);

-- ======================================================
-- POPULATE MESSAGES
-- ======================================================
INSERT INTO Message (chat_id, content, timestamp, senderid, receiverid, isusersender, status) VALUES
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
INSERT INTO message_img (messageID, format, ImgFile, ImgName) VALUES
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