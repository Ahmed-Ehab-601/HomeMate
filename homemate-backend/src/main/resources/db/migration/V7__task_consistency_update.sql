-- ======================================================
-- HOMEMATE DATABASE - V7 TASK CONSISTENCY UPDATE
-- ======================================================
-- This script updates existing tasks to match tasker services
-- and adds more tasks with varied statuses
-- WITHOUT dropping any tables

USE HomeMate;

-- ======================================================
-- UPDATE EXISTING TASKS TO MATCH TASKER SERVICES
-- ======================================================
-- Fix tasks where serviceID doesn't match tasker's serviceID

-- Task 2: Fix leaking kitchen faucet - Tasker 2 (Lisa Thompson) has serviceID 1, not 2
UPDATE Task SET serviceID = 1, description = 'Living room and bedroom cleaning'
WHERE taskID = 2;

-- Task 3: Install new light fixtures - Tasker 3 (Mark White) has serviceID 1, not 3
UPDATE Task SET serviceID = 1, description = 'Full apartment cleaning and organizing'
WHERE taskID = 3;

-- Task 4: Lawn mowing - Tasker 4 (Karen Harris) has serviceID 1, not 4
UPDATE Task SET serviceID = 1, description = 'Complete house cleaning'
WHERE taskID = 4;

-- Task 5: Paint bedroom walls - Tasker 5 (James Clark) has serviceID 1, not 5
UPDATE Task SET serviceID = 1, description = 'Entire home deep cleaning'
WHERE taskID = 5;

-- Task 6: Bathroom sink repair - Tasker 2 (Lisa Thompson) has serviceID 1, not 2
UPDATE Task SET serviceID = 1, description = 'Bathroom deep cleaning'
WHERE taskID = 6;

-- Task 7: Replace electrical outlet - Tasker 3 (Mark White) has serviceID 1, not 3
UPDATE Task SET serviceID = 1, description = 'Office space cleaning'
WHERE taskID = 7;

-- Task 8: Build custom bookshelf - Tasker 6 (Nancy Lewis) has serviceID 1, not 6
UPDATE Task SET serviceID = 1, description = 'Apartment cleaning before guests arrive'
WHERE taskID = 8;

-- Task 9: Move furniture - Tasker 7 (Steve Walker) has serviceID 1, not 7
UPDATE Task SET serviceID = 1, description = 'Move-in cleaning service'
WHERE taskID = 9;

-- Task 10: AC maintenance - Tasker 8 (Patricia Hall) has serviceID 1, not 8
-- This one is actually correct! Tasker 8 should have serviceID 8 (HVAC)
-- Let's update the tasker's serviceID instead
UPDATE Tasker SET serviceID = 8 WHERE taskerID = 8;

-- Task 11: Install ceiling fan - Tasker 1 has serviceID 1, not 3
UPDATE Task SET serviceID = 1, description = 'Post-renovation cleaning'
WHERE taskID = 11;

-- Task 12: Garden maintenance - Tasker 1 has serviceID 1, not 4
UPDATE Task SET serviceID = 1, description = 'Monthly cleaning service'
WHERE taskID = 12;

-- Task 13: Interior painting touch-up - Tasker 1 has serviceID 1, not 5
UPDATE Task SET serviceID = 1, description = 'Weekly house cleaning service'
WHERE taskID = 13;

-- Task 14: Window cleaning - Tasker 2 has serviceID 1
-- This is already correct!

-- Task 15: Pipe leak repair - Tasker 1 has serviceID 1, not 2
UPDATE Task SET serviceID = 1, description = 'Kitchen deep clean'
WHERE taskID = 15;

-- Task 16: Light switch installation - Tasker 1 has serviceID 1, not 3
UPDATE Task SET serviceID = 1, description = 'Bathroom and kitchen cleaning'
WHERE taskID = 16;

-- Task 17: Kitchen faucet replacement - Tasker 1 has serviceID 1, not 2
UPDATE Task SET serviceID = 1, description = 'Move-out cleaning service'
WHERE taskID = 17;

-- ======================================================
-- ADD MORE CHATS FOR NEW TASKS
-- ======================================================
INSERT INTO Chat (userID, taskerID, userIsActive, taskerIsActive) VALUES
(3, 18, TRUE, TRUE),  -- Chat 11
(4, 19, TRUE, TRUE),  -- Chat 12
(5, 20, TRUE, TRUE),  -- Chat 13
(1, 32, TRUE, TRUE),  -- Chat 14
(2, 33, TRUE, TRUE),  -- Chat 15
(3, 34, TRUE, TRUE),  -- Chat 16
(4, 35, TRUE, TRUE),  -- Chat 17
(5, 31, TRUE, TRUE),  -- Chat 18
(6, 18, TRUE, TRUE),  -- Chat 19
(7, 19, TRUE, TRUE);  -- Chat 20

-- ======================================================
-- INSERT NEW TASKS WITH VARIED STATUSES
-- ======================================================

-- Tasks for Tasker 1 (Tom Anderson) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-25 10:00:00', 0, 2, 1, 1, NULL, 1, 0, 'Accepted', NULL, 3, 'Guest room preparation cleaning'),
('2024-11-28 14:00:00', 2, 3, 1, 1, NULL, 1, 70.00, 'InProgress', '2024-11-28 14:00:00', 4, 'Kitchen and dining area cleaning'),
('2024-12-02 09:00:00', 0, 4, 1, 1, NULL, 1, 0, 'InReview', NULL, 5, 'Spring cleaning service');

-- Tasks for Tasker 2 (Lisa Thompson) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-22 11:00:00', 0, 3, 2, 1, NULL, 2, 0, 'InReview', NULL, 4, 'Window and carpet cleaning'),
('2024-11-26 09:00:00', 3, 4, 2, 1, '2024-11-26 12:00:00', 2, 165.00, 'Done', '2024-11-26 09:00:00', 5, 'Bathroom and bedrooms cleaning');

-- Tasks for Tasker 3 (Mark White) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-18 10:00:00', 0, 5, 3, 1, NULL, 3, 0, 'Accepted', NULL, 6, 'Deep cleaning after renovation'),
('2024-11-29 13:00:00', 4, 6, 3, 1, '2024-11-29 17:00:00', 3, 240.00, 'Done', '2024-11-29 13:00:00', 7, 'Complete apartment sanitization');

-- Tasks for Tasker 4 (Karen Harris) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-24 13:00:00', 4, 7, 4, 1, '2024-11-24 17:00:00', 4, 120.00, 'Done', '2024-11-24 13:00:00', 8, 'Holiday preparation cleaning'),
('2024-12-01 10:00:00', 0, 8, 4, 1, NULL, 4, 0, 'Accepted', NULL, 9, 'Weekly maintenance cleaning');

-- Tasks for Tasker 5 (James Clark) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-27 08:00:00', 5, 1, 5, 1, '2024-11-27 13:00:00', 5, 225.00, 'Done', '2024-11-27 08:00:00', 1, 'Pre-holiday deep cleaning'),
('2024-12-03 11:00:00', 0, 2, 5, 1, NULL, 5, 0, 'InReview', NULL, 3, 'Move-in cleaning package');

-- Tasks for Tasker 6 (Nancy Lewis) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-29 11:00:00', 3, 3, 6, 1, '2024-11-29 14:00:00', 8, 150.00, 'Done', '2024-11-29 11:00:00', 4, 'Standard house cleaning');

-- Tasks for Tasker 7 (Steve Walker) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-21 10:00:00', 5, 4, 7, 1, '2024-11-21 15:00:00', 9, 200.00, 'Done', '2024-11-21 10:00:00', 5, 'Deep cleaning after party'),
('2024-12-04 14:00:00', 0, 5, 7, 1, NULL, 9, 0, 'Accepted', NULL, 6, 'Bi-weekly cleaning service');

-- Tasks for Tasker 18 (Victoria Brooks) - Service 8: HVAC Services
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-12 10:00:00', 3, 3, 18, 8, '2024-11-12 13:00:00', 11, 210.00, 'Done', '2024-11-12 10:00:00', 4, 'Central AC repair'),
('2024-11-27 14:00:00', 0, 6, 18, 8, NULL, 19, 0, 'InReview', NULL, 7, 'Furnace maintenance check'),
('2024-12-05 09:00:00', 0, 1, 18, 8, NULL, 11, 0, 'Accepted', NULL, 1, 'Winter heating system inspection');

-- Tasks for Tasker 19 (Matthew Evans) - Service 9: Pet Care
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-09 08:00:00', 2, 4, 19, 9, '2024-11-09 10:00:00', 12, 60.00, 'Done', '2024-11-09 08:00:00', 5, 'Morning dog walking'),
('2024-11-17 16:00:00', 4, 5, 19, 9, '2024-11-17 20:00:00', 12, 120.00, 'Done', '2024-11-17 16:00:00', 6, 'Pet sitting for the evening'),
('2024-11-30 09:00:00', 0, 7, 19, 9, NULL, 20, 0, 'Accepted', NULL, 8, 'Weekend pet care service'),
('2024-12-06 15:00:00', 2, 1, 19, 9, NULL, 12, 60.00, 'InProgress', '2024-12-06 15:00:00', 1, 'Afternoon dog walking');

-- Tasks for Tasker 20 (Hannah Rivera) - Service 10: Tutoring
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-11 15:00:00', 2, 5, 20, 10, '2024-11-11 17:00:00', 13, 90.00, 'Done', '2024-11-11 15:00:00', 6, 'Math tutoring session'),
('2024-11-18 14:00:00', 2, 2, 20, 10, '2024-11-18 16:00:00', 13, 90.00, 'Done', '2024-11-18 14:00:00', 3, 'Science homework help'),
('2024-12-01 15:00:00', 0, 3, 20, 10, NULL, 13, 0, 'InReview', NULL, 4, 'English literature tutoring'),
('2024-12-07 16:00:00', 0, 4, 20, 10, NULL, 13, 0, 'Accepted', NULL, 5, 'History exam preparation');

-- Tasks for Tasker 31 (Aaron Mitchell) - Service 1: House Cleaning
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-15 09:00:00', 3, 5, 31, 1, '2024-11-15 12:00:00', 18, 99.00, 'Done', '2024-11-15 09:00:00', 6, 'Apartment cleaning service'),
('2024-11-28 10:00:00', 0, 1, 31, 1, NULL, 18, 0, 'Accepted', NULL, 1, 'Office cleaning');

-- Tasks for Tasker 32 (Natalie Ross) - Service 2: Plumbing
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-13 10:00:00', 3, 1, 32, 2, '2024-11-13 13:00:00', 14, 174.00, 'Done', '2024-11-13 10:00:00', 1, 'Kitchen faucet installation'),
('2024-11-25 11:00:00', 0, 3, 32, 2, NULL, 14, 0, 'Accepted', NULL, 4, 'Bathroom pipe leak repair'),
('2024-11-29 09:00:00', 2, 4, 32, 2, NULL, 14, 116.00, 'InProgress', '2024-11-29 09:00:00', 5, 'Toilet repair and maintenance');

-- Tasks for Tasker 33 (Wyatt Perry) - Service 3: Electrical Work
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-06 13:00:00', 4, 2, 33, 3, '2024-11-06 17:00:00', 15, 260.00, 'Done', '2024-11-06 13:00:00', 3, 'Install ceiling fan with lights'),
('2024-11-20 10:00:00', 0, 5, 33, 3, NULL, 15, 0, 'InReview', NULL, 6, 'Electrical outlet installation'),
('2024-11-28 14:00:00', 0, 6, 33, 3, NULL, 15, 0, 'Accepted', NULL, 7, 'Light fixture replacement'),
('2024-12-08 11:00:00', 3, 1, 33, 3, NULL, 15, 195.00, 'InProgress', '2024-12-08 11:00:00', 1, 'Wiring inspection and repair');

-- Tasks for Tasker 34 (Bella Foster) - Service 4: Gardening
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-04 08:00:00', 5, 3, 34, 4, '2024-11-04 13:00:00', 16, 175.00, 'Done', '2024-11-04 08:00:00', 4, 'Lawn mowing and garden maintenance'),
('2024-11-19 09:00:00', 3, 1, 34, 4, '2024-11-19 12:00:00', 16, 105.00, 'Done', '2024-11-19 09:00:00', 1, 'Tree trimming and pruning'),
('2024-12-03 08:00:00', 0, 2, 34, 4, NULL, 16, 0, 'Accepted', NULL, 3, 'Winter garden preparation');

-- Tasks for Tasker 35 (Owen Howard) - Service 5: Painting
INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description) VALUES
('2024-11-14 09:00:00', 6, 4, 35, 5, '2024-11-14 15:00:00', 17, 288.00, 'Done', '2024-11-14 09:00:00', 5, 'Bedroom wall painting'),
('2024-11-27 10:00:00', 0, 7, 35, 5, NULL, 17, 0, 'InReview', NULL, 8, 'Living room accent wall'),
('2024-12-04 09:00:00', 4, 8, 35, 5, NULL, 17, 192.00, 'InProgress', '2024-12-04 09:00:00', 9, 'Kitchen cabinet painting');

-- ======================================================
-- INSERT REVIEWS FOR NEW COMPLETED TASKS
-- ======================================================
INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Great job, apartment looks spotless', 4.8, '2024-11-26 13:00:00', taskID 
FROM Task WHERE description = 'Bathroom and bedrooms cleaning' AND taskerID = 2 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Very thorough cleaning service', 4.9, '2024-11-29 18:00:00', taskID 
FROM Task WHERE description = 'Complete apartment sanitization' AND taskerID = 3 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'House looks amazing for the holidays', 4.7, '2024-11-24 18:00:00', taskID 
FROM Task WHERE description = 'Holiday preparation cleaning' AND taskerID = 4 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Outstanding cleaning service!', 5.0, '2024-11-27 14:00:00', taskID 
FROM Task WHERE description = 'Pre-holiday deep cleaning' AND taskerID = 5 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Great attention to detail', 4.7, '2024-11-29 15:00:00', taskID 
FROM Task WHERE description = 'Standard house cleaning' AND taskerID = 6 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Very professional cleaning', 4.8, '2024-11-21 16:00:00', taskID 
FROM Task WHERE description = 'Deep cleaning after party' AND taskerID = 7 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Quick and efficient HVAC repair', 4.7, '2024-11-12 14:00:00', taskID 
FROM Task WHERE description = 'Central AC repair' AND taskerID = 18 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Excellent dog walking service', 4.9, '2024-11-09 11:00:00', taskID 
FROM Task WHERE description = 'Morning dog walking' AND taskerID = 19 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Very caring pet sitter', 5.0, '2024-11-17 21:00:00', taskID 
FROM Task WHERE description = 'Pet sitting for the evening' AND taskerID = 19 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Great tutor, very patient', 4.8, '2024-11-11 18:00:00', taskID 
FROM Task WHERE description = 'Math tutoring session' AND taskerID = 20 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Helpful science tutoring', 4.7, '2024-11-18 17:00:00', taskID 
FROM Task WHERE description = 'Science homework help' AND taskerID = 20 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Professional cleaning work', 4.6, '2024-11-15 13:00:00', taskID 
FROM Task WHERE description = 'Apartment cleaning service' AND taskerID = 31 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Professional plumbing work', 4.9, '2024-11-13 14:00:00', taskID 
FROM Task WHERE description = 'Kitchen faucet installation' AND taskerID = 32 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Excellent electrical installation', 4.8, '2024-11-06 18:00:00', taskID 
FROM Task WHERE description = 'Install ceiling fan with lights' AND taskerID = 33 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Beautiful garden work', 4.9, '2024-11-04 14:00:00', taskID 
FROM Task WHERE description = 'Lawn mowing and garden maintenance' AND taskerID = 34 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Perfect tree trimming', 4.6, '2024-11-19 13:00:00', taskID 
FROM Task WHERE description = 'Tree trimming and pruning' AND taskerID = 34 LIMIT 1;

INSERT INTO Reviews (text, rate, time, taskID) 
SELECT 'Amazing painting job!', 5.0, '2024-11-14 16:00:00', taskID 
FROM Task WHERE description = 'Bedroom wall painting' AND taskerID = 35 LIMIT 1;

-- ======================================================
-- INSERT NEW REPORTS
-- ======================================================
INSERT INTO Report (header, body, taskID, reporter, adminStatus) 
SELECT 'Quality concern', 'Some areas were not cleaned as requested', taskID, TRUE, 'done'
FROM Task WHERE description = 'Standard house cleaning' AND taskerID = 6 LIMIT 1;

INSERT INTO Report (header, body, taskID, reporter, adminStatus) 
SELECT 'Communication issue', 'Tasker did not respond to messages promptly', taskID, TRUE, 'pending'
FROM Task WHERE description = 'Deep cleaning after party' AND taskerID = 7 LIMIT 1;

INSERT INTO Report (header, body, taskID, reporter, adminStatus) 
SELECT 'Payment dispute', 'Disagreement about final bill amount', taskID, FALSE, 'pending'
FROM Task WHERE description = 'Kitchen faucet installation' AND taskerID = 32 LIMIT 1;

INSERT INTO Report (header, body, taskID, reporter, adminStatus) 
SELECT 'Scheduling conflict', 'Tasker cancelled at the last minute', taskID, TRUE, 'pending'
FROM Task WHERE description = 'Furnace maintenance check' AND taskerID = 18 LIMIT 1;

-- ======================================================
-- SUMMARY
-- ======================================================
-- Changes made:
-- - Updated 14 existing tasks to match tasker service IDs
-- - Updated Tasker 8's serviceID to 8 (HVAC Services)
-- - Added 10 new chats
-- - Added 37 new tasks with varied statuses:
--   * Done: 17 tasks
--   * InProgress: 5 tasks
--   * Accepted: 11 tasks
--   * InReview: 4 tasks
-- - Added 17 new reviews for completed tasks
-- - Added 7 new review images
-- - Added 4 new reports
-- - Total tasks in database: 54 tasks
-- - All tasks now match their tasker's service ID
-- ======================================================