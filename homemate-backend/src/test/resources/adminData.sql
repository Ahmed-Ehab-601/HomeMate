
-- Insert test users (with all required NOT NULL fields)
INSERT INTO Users (userID, firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended)
VALUES 
    (1, 'Admin', 'User', 'admin_user', 'password123', 'admin@test.com', '1990-01-01 00:00:00', 'M', '1234567890', true, false),
    (2, 'Regular', 'User', 'regular_user', 'password456', 'user@test.com', '1992-05-15 00:00:00', 'F', '0987654321', false, false),
    (3, 'Suspended', 'User', 'suspended_user', 'password789', 'suspended@test.com', '1988-12-20 00:00:00', 'M', '5555555555', false, true);

-- Insert test services (required for Taskers foreign key)
INSERT INTO Service (serviceID, name, description)
VALUES 
    (1, 'Plumbing', 'Professional plumbing services'),
    (2, 'Electrical', 'Electrical repair and installation'),
    (3, 'Cleaning', 'House cleaning services');

-- Insert test taskers (with all required NOT NULL fields)
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity, suspended)
VALUES 
    (1, 'Tasker', 'One', 'samaa', 'pass123', 'tasker1@test.com', '1985-03-10 00:00:00', '1111111111', 'M', 'available', 4.5, 25.50, 'Experienced worker', 1, 0.00, 0.00, 'New York', false),
    (2, 'Tasker', 'Two', 'tasker2', 'pass456', 'tasker2@test.com', '1990-07-22 00:00:00', '2222222222', 'F', 'available', 4.8, 30.00, 'Professional', 2, 0.00, 0.00, 'Los Angeles', false),
    (3, 'Suspended', 'Tasker', 'suspended_tasker', 'pass789', 'suspended_tasker@test.com', '1988-11-05 00:00:00', '3333333333', 'M', 'unavailable', 3.5, 20.00, 'Suspended account', 3, 0.00, 0.00, 'Chicago', true);
