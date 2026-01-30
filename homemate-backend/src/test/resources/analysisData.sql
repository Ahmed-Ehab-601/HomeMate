INSERT INTO Users (userID, firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended, createdTime)
VALUES 
    (1, 'John', 'Doe', 'johndoe', 'password123', 'john@test.com', '1990-01-01 00:00:00', 'M', '1234567890', false, false, '2023-01-01 10:00:00'),
    (2, 'Jane', 'Smith', 'janesmith', 'password456', 'jane@test.com', '1992-05-15 00:00:00', 'F', '0987654321', false, false, '2023-02-15 12:30:00'),
    (3, 'Bob', 'Wilson', 'bobwilson', 'password789', 'bob@test.com', '1985-08-20 00:00:00', 'M', '5555555555', false, true, '2023-03-20 14:45:00'),
    (4, 'Alice', 'Johnson', 'alicej', 'password101', 'alice@test.com', '1995-03-10 00:00:00', 'F', '4444444444', false, false, '2023-04-10 09:15:00'),
    (5, 'Charlie', 'Brown', 'charlieb', 'password202', 'charlie@test.com', '1988-12-25 00:00:00', 'M', '3333333333', false, false, '2023-05-25 16:20:00'),
    (6, 'Diana', 'Miller', 'dianam', 'password303', 'diana@test.com', '1990-01-01 00:00:00', 'F', '2222222222', false, true, '2023-06-01 11:30:00');

INSERT INTO Address (addressID, userID, country, city, street, apartment)
VALUES 
    (1, 1, 'Egypt', 'Cairo', 'Main St', '101'),
    (2, 2, 'Egypt', 'Alexandria', 'Sea St', '202'),
    (3, 3, 'Egypt', 'Giza', 'Pyramid St', '303'),
    (4, 4, 'Egypt', 'Cairo', 'Nile St', '404'),
    (5, 5, 'Egypt', 'Cairo', 'Garden St', '505'),
    (6, 6, 'Egypt', 'Alexandria', 'Beach St', '606');

INSERT INTO Service (serviceID, name, description)
VALUES 
    (1, 'Plumbing', 'Professional plumbing services'),
    (2, 'Electrical', 'Electrical repair and installation'),
    (3, 'Cleaning', 'House cleaning services'),
    (4, 'Painting', 'Interior and exterior painting');

INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity, suspended, createdTime)
VALUES 
    (1, 'Mike', 'Johnson', 'mikej', 'pass123', 'mike@test.com', '1985-03-10 00:00:00', '1111111111', 'M', 'available', 4.5, 25.50, 'Experienced plumber', 1, 500.00, 20.00, 'Cairo', false, '2023-01-15 08:00:00'),
    (2, 'Sarah', 'Williams', 'sarahw', 'pass456', 'sarah@test.com', '1990-07-22 00:00:00', '2222222222', 'F', 'available', 4.8, 30.00, 'Professional electrician', 2, 800.00, 30.00, 'Alexandria', false, '2023-02-20 10:30:00'),
    (3, 'Tom', 'Davis', 'tomd', 'pass789', 'tom@test.com', '1988-11-05 00:00:00', '3333333333', 'M', 'unavailable', 3.5, 20.00, 'Expert cleaner', 3, 300.00, 15.00, 'Giza', true, '2023-03-10 13:45:00'),
    (4, 'Emma', 'Taylor', 'emmat', 'pass101', 'emma@test.com', '1992-06-18 00:00:00', '4444444444', 'F', 'available', 4.2, 28.00, 'Skilled painter', 4, 600.00, 25.00, 'Cairo', false, '2023-04-05 15:20:00'),
    (5, 'James', 'Anderson', 'jamesa', 'pass202', 'james@test.com', '1987-09-30 00:00:00', '5555555555', 'M', 'available', 4.0, 22.00, 'Plumbing specialist', 1, 400.00, 18.00, 'Cairo', false, '2023-05-12 09:00:00');

INSERT INTO Task (taskID, startDate, workedHours, userID, taskerID, serviceID, endDate, bill, status, addressID, description, estimation)
VALUES 
    (1, '2024-01-01 09:00:00', 2.5, 1, 1, 1, '2024-01-01 11:30:00', 63.75, 'Done', 1, 'Fix kitchen sink', 3),
    (2, '2024-01-02 10:00:00', 3.0, 2, 2, 2, '2024-01-02 13:00:00', 90.00, 'Done', 2, 'Install light fixtures', 3),
    (3, '2024-01-03 14:00:00', 0, 1, 2, 2, NULL, 0, 'InProgress', 1, 'Electrical panel repair', 4),
    (4, '2024-01-04 08:00:00', 0, 2, 1, 1, NULL, 0, 'Accepted', 2, 'Bathroom plumbing', 2),
    (5, '2024-01-05 15:00:00', 0, 3, 3, 3, NULL, 0, 'InReview', 3, 'House cleaning', 5),
    (6, '2024-01-06 11:00:00', 4.0, 4, 4, 4, '2024-01-06 15:00:00', 112.00, 'Done', 4, 'Paint living room', 4),
    (7, '2024-01-07 09:00:00', 0, 5, 5, 1, NULL, 0, 'Rejected', 5, 'Pipe replacement', 2),
    (8, '2024-01-08 13:00:00', 2.0, 1, 1, 1, '2024-01-08 15:00:00', 51.00, 'Done', 1, 'Fix bathroom faucet', 2),
    (9, '2024-01-09 10:00:00', 0, 2, 2, 2, NULL, 0, 'Suspended', 2, 'Rewire bedroom', 6),
    (10, '2024-01-10 08:00:00', 1.5, 4, 1, 1, '2024-01-10 09:30:00', 38.25, 'Done', 4, 'Unclog drain', 2);

INSERT INTO Chat (chatID, userID, taskerID)
VALUES 
    (1, 1, 1),
    (2, 2, 2),
    (3, 1, 2),
    (4, 3, 3),
    (5, 4, 4);

INSERT INTO Report (reportID, taskID, header, body, reporter, adminStatus)
VALUES 
    (1, 1, 'Tasker did not complete the work', 'The tasker left before finishing the job.', true, 'pending'),
    (2, 2, 'User did not pay', 'User refused to pay after work was completed.', false, 'pending'),
    (3, 5, 'Poor quality work', 'The work quality was below expectations.', true, 'done');

INSERT INTO Reviews (reviewID, taskID, rate, text)
VALUES 
    (1, 1, 4.5, 'Good work overall'),
    (2, 2, 5.0, 'Excellent service!'),
    (3, 6, 4.0, 'Nice paint job'),
    (4, 8, 4.5, 'Fixed the faucet quickly'),
    (5, 10, 3.5, 'Took a bit long but got the job done');