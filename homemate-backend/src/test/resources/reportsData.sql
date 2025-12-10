
-- Insert test users
INSERT INTO Users (userID, firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended)
VALUES 
    (1, 'John', 'Doe', 'johndoe', 'password123', 'john@test.com', '1990-01-01 00:00:00', 'M', '1234567890', false, false),
    (2, 'Jane', 'Smith', 'janesmith', 'password456', 'jane@test.com', '1992-05-15 00:00:00', 'F', '0987654321', false, false),
    (3, 'Suspended', 'User', 'suspendeduser', 'password789', 'suspended@test.com', '1988-12-20 00:00:00', 'M', '5555555555', false, true);

-- Insert test addresses
INSERT INTO Address (addressID, userID, country, city, street, apartment)
VALUES 
    (1, 1, 'Egypt', 'Cairo', 'Main St', '101'),
    (2, 2, 'Egypt', 'Alexandria', 'Sea St', '202'),
    (3, 3, 'Egypt', 'Giza', 'Pyramid St', '303');

-- Insert test services
INSERT INTO Service (serviceID, name, description)
VALUES 
    (1, 'Plumbing', 'Professional plumbing services'),
    (2, 'Electrical', 'Electrical repair and installation'),
    (3, 'Cleaning', 'House cleaning services');

-- Insert test taskers
INSERT INTO Tasker (taskerID, firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity, suspended)
VALUES 
    (1, 'Mike', 'Johnson', 'mikej', 'pass123', 'mike@test.com', '1985-03-10 00:00:00', '1111111111', 'M', 'available', 4.5, 25.50, 'Experienced plumber', 1, 0.00, 0.00, 'Cairo', false),
    (2, 'Sarah', 'Williams', 'sarahw', 'pass456', 'sarah@test.com', '1990-07-22 00:00:00', '2222222222', 'F', 'available', 4.8, 30.00, 'Professional electrician', 2, 0.00, 0.00, 'Alexandria', false),
    (3, 'Bob', 'Suspended', 'suspendedtasker', 'pass789', 'suspendedt@test.com', '1988-11-05 00:00:00', '3333333333', 'M', 'unavailable', 3.5, 20.00, 'Suspended tasker', 3, 0.00, 0.00, 'Giza', true);

-- Insert test tasks
INSERT INTO Task (taskID, startDate, workedHours, userID, taskerID, serviceID, endDate, bill, status, addressID, description)
VALUES 
    (1, '2024-01-01 09:00:00', 2.5, 1, 1, 1, '2024-01-01 11:30:00', 63.75, 'Done', 1, 'Fix kitchen sink'),
    (2, '2024-01-02 10:00:00', 3.0, 2, 2, 2, '2024-01-02 13:00:00', 90.00, 'Done', 2, 'Install light fixtures'),
    (3, '2024-01-03 14:00:00', 0, 1, 2, 2, NULL, 0, 'InProgress', 1, 'Electrical panel repair'),
    (4, '2024-01-04 08:00:00', 0, 2, 1, 1, NULL, 0, 'Accepted', 2, 'Bathroom plumbing'),
    (5, '2024-01-05 15:00:00', 0, 3, 3, 3, NULL, 0, 'InReview', 3, 'House cleaning');

-- Insert test reports
INSERT INTO Report (reportID, taskID, header, body, reporter, adminStatus)
VALUES 
    (1, 1, 'Tasker did not complete the work', 'The tasker left before finishing the job and the sink is still leaking.', true, 'pending'),
    (2, 2, 'User did not pay', 'User refused to pay after work was completed successfully.', false, 'pending'),
    (3, 1, 'Poor quality work', 'The work quality was below expectations and needs to be redone.', true, 'done'),
    (4, 3, 'Communication issues', 'Tasker not responding to messages and missed scheduled appointments.', true, 'pending'),
    (5, 5, 'Rude behavior', 'User was very rude and disrespectful during the service.', false, 'done');
