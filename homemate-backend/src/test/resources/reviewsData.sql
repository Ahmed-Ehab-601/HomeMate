
-- Users
INSERT INTO Users (userID, firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES 
(1, 'John', 'Doe', 'johndoe', 'pass123', 'john@test.com', '1990-01-01 00:00:00', 'M', '1234567890', false, false),
(2, 'Jane', 'Smith', 'janesmith', 'pass456', 'jane@test.com', '1995-05-05 00:00:00', 'F', '0987654321', false, false);

-- Address
INSERT INTO Address (addressID, userID, country, city, street, apartment) VALUES 
(1, 1, 'USA', 'New York', 'Fifth Ave', '101'),
(2, 2, 'USA', 'Los Angeles', 'Sunset Blvd', '202');

-- Service
INSERT INTO Service (serviceID, name, description) VALUES 
(1, 'Plumbing', 'Fix leaks and pipes'),
(2, 'Cleaning', 'Home cleaning services');

-- Tasker
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity, suspended) VALUES 
(1, 'Mike', 'Plumber', 'mike_plumber', 'pass1', 'mike@test.com', '1980-08-08 00:00:00', '111111', 'M', 'available', 4.5, 50.00, 'Expert plumber', 1, 1000.0, 20.0, 'New York', false),
(2, 'Alice', 'Cleaner', 'alice_shine', 'pass2', 'alice@test.com', '1985-02-02 00:00:00', '222222', 'F', 'available', 4.8, 30.00, 'Top cleaner', 2, 500.0, 15.0, 'Los Angeles', false);

-- Task
-- Task1: Done, User1, Tasker1
INSERT INTO Task (taskID, startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, addressID, description) VALUES 
(1, '2023-01-01 10:00:00', 2, 1, 1, 1, '2023-01-01 12:00:00', NULL, 100.00, 'Done', 1, 'Fix sink');

-- Task2: Done, User2, Tasker2
INSERT INTO Task (taskID, startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, addressID, description) VALUES 
(2, '2023-02-01 10:00:00', 3, 2, 2, 2, '2023-02-01 13:00:00', NULL, 90.00, 'Done', 2, 'Clean items');

-- Task3: InProgress, User1, Tasker2 (No Review possible technically if logic enforced, but for DB test it's fine)
INSERT INTO Task (taskID, startDate, workedHours, userID, taskerID, serviceID, endDate, chatID, bill, status, addressID, description) VALUES 
(3, '2023-03-01 10:00:00', 0, 1, 2, 2, NULL, NULL, 0.00, 'InProgress', 1, 'Ongoing cleaning');

-- Reviews
-- Review for Task1
INSERT INTO Reviews (reviewID, text, rate, time, taskID) VALUES 
(1, 'Great job!', 5.0, '2023-01-02 10:00:00', 1);

-- ReviewImage for Review1
-- H2 BLOBs can be hex strings. 'CAFEBABE' is just dummy data.
INSERT INTO ReviewImage (imageID, format, imageFile, imageName, reviewID) VALUES 
(1, 'png', '89504E47', 'photo1.png', 1);
