-- ======================================================
-- PAYMENT TEST DATA
-- Minimal test data for payment integration tests
-- ======================================================

-- ======================================================
-- POPULATE USERS
-- ======================================================
INSERT INTO Users (userID, firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES
                                                                                                                           (1, 'John', 'Smith', 'jsmith', '$2a$10$abcdefghijklmnopqrstuvwxyz123456', 'john.smith@email.com', '1990-05-15', 'M', '+1-555-0101', FALSE, FALSE),
                                                                                                                           (2, 'Sarah', 'Johnson', 'sjohnson', '$2a$10$abcdefghijklmnopqrstuvwxyz123457', 'sarah.j@email.com', '1988-08-22', 'F', '+1-555-0102', FALSE, FALSE);

-- ======================================================
-- POPULATE SERVICES
-- ======================================================
INSERT INTO Service (serviceID, name, description) VALUES
                                                       (1, 'House Cleaning', 'Professional cleaning services for your home'),
                                                       (2, 'Plumbing', 'Expert plumbing services for repairs and installations');

-- ======================================================
-- POPULATE TASKERS
-- ======================================================
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity, stripe_account_id) VALUES
                                                                                                                                                                                                                       (1, 'Tom', 'Anderson', 'tanderson', '$2a$10$tasker123456789abcdefghij', 'tom.a@tasker.com', '1985-03-20', '+1-555-1001', 'M', 'available', 4.8, 35.00, 'Experienced cleaner with 10 years in the business.', 1, 15400.00, 440.00, 'New York', 'acct_test123'),
                                                                                                                                                                                                                       (2, 'Lisa', 'Thompson', 'lthompson', '$2a$10$tasker223456789abcdefghij', 'lisa.t@tasker.com', '1982-07-14', '+1-555-1002', 'F', 'available', 4.9, 55.00, 'Licensed plumber with expertise in residential repairs.', 2, 28600.00, 520.00, 'Los Angeles', 'acct_test456');

-- ======================================================
-- POPULATE ADDRESSES
-- ======================================================
INSERT INTO Address (addressID, userID, country, city, street, apartment) VALUES
                                                                              (1, 1, 'USA', 'New York', '123 Main Street', 'Apt 4B'),
                                                                              (2, 2, 'USA', 'Los Angeles', '789 Sunset Blvd', 'Suite 12');

-- ======================================================
-- POPULATE CHATS
-- ======================================================
INSERT INTO Chat (chatID, userID, taskerID, userIsActive, taskerIsActive) VALUES
                                                                              (1, 1, 1, TRUE, TRUE),
                                                                              (2, 2, 2, TRUE, TRUE);

-- ======================================================
-- POPULATE TASKS
-- ======================================================
INSERT INTO Task (taskID, startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, startInProgress, addressID, description, paid) VALUES
                                                                                                                                                                 (1, '2024-11-01 09:00:00', 4, 1, 1, 1, '2024-11-01 13:00:00', 1, 140.00, 'Done', '2024-11-01 09:00:00', 1, 'Deep cleaning of entire apartment', FALSE),
                                                                                                                                                                 (2, '2024-11-03 10:00:00', 3, 2, 2, 2, '2024-11-03 13:00:00', 2, 165.00, 'Done', '2024-11-03 10:00:00', 2, 'Fix leaking kitchen faucet', FALSE),
                                                                                                                                                                 (3, '2024-11-05 14:00:00', 0, 1, 1, 1, NULL, 1, 0, 'Accepted', NULL, 1, 'Weekly house cleaning', FALSE),
                                                                                                                                                                 (4, '2024-11-07 08:00:00', 0, 2, 2, 2, NULL, 2, 0, 'InProgress', '2024-11-07 08:00:00', 2, 'Emergency plumbing repair', FALSE);

-- ======================================================
-- POPULATE PAYMENTS (Sample test data)
-- ======================================================
INSERT INTO payments (paymentID, taskId, userId, taskerId, totalAmount, platformFee, taskerAmount, stripePaymentIntentId, status, created_at, paid_at) VALUES
                                                                                                                                                           (1, 1, 1, 1, 140.00, 14.00, 126.00, 'pi_test_123456', 'PAID', '2024-11-01 09:00:00', '2024-11-01 13:30:00'),
                                                                                                                                                           (2, 2, 2, 2, 165.00, 16.50, 148.50, 'pi_test_789012', 'PAID', '2024-11-03 10:00:00', '2024-11-03 13:45:00'),
                                                                                                                                                           (3, 3, 1, 1, 100.00, 10.00, 90.00, NULL, 'CREATED', '2024-11-05 14:00:00', NULL);

-- ======================================================
-- SUMMARY
-- ======================================================
-- Data inserted for payment tests:
-- - 2 Users
-- - 2 Services
-- - 2 Taskers
-- - 2 Addresses
-- - 2 Chats
-- - 4 Tasks (2 completed, 1 accepted, 1 in progress)
-- - 3 Payments (2 paid, 1 created)
-- ======================================================