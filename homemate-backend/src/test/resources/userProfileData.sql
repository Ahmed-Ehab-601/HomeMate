
-- Insert Users
INSERT INTO Users (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES 
('John', 'Smith', 'jsmith', '$2a$10$test123456789', 'john.smith@email.com', '1990-05-15 00:00:00', 'M', '+1-555-0101', false, false),
('Sarah', 'Johnson', 'sjohnson', '$2a$10$test123456790', 'sarah.j@email.com', '1988-08-22 00:00:00', 'F', '+1-555-0102', false, false),
('Michael', 'Brown', 'mbrown', '$2a$10$test123456791', 'michael.b@email.com', '1992-03-10 00:00:00', 'M', '+1-555-0103', false, false),
('Emily', 'Davis', 'edavis', '$2a$10$test123456792', 'emily.davis@email.com', '1995-11-30 00:00:00', 'F', '+1-555-0104', false, false),
('David', 'Wilson', 'dwilson', '$2a$10$test123456793', 'david.w@email.com', '1987-07-18 00:00:00', 'M', '+1-555-0105', false, false),
('Jessica', 'Martinez', 'jmartinez', '$2a$10$test123456794', 'jessica.m@email.com', '1993-01-25 00:00:00', 'F', '+1-555-0106', false, false),
('Robert', 'Garcia', 'rgarcia', '$2a$10$test123456795', 'robert.g@email.com', '1991-09-05 00:00:00', 'M', '+1-555-0107', false, false),
('Amanda', 'Rodriguez', 'arodriguez', '$2a$10$test123456796', 'amanda.r@email.com', '1989-12-12 00:00:00', 'F', '+1-555-0108', false, false),
('Admin', 'User', 'admin', '$2a$10$test123456797', 'admin@homemate.com', '1985-06-20 00:00:00', 'M', '+1-555-0001', true, false),
('Chris', 'Taylor', 'ctaylor', '$2a$10$test123456798', 'chris.t@email.com', '1994-04-08 00:00:00', 'M', '+1-555-0109', false, false),
('Alex', 'Moore', 'amoore', '$2a$10$test123456799', 'alex.moore@email.com', '1996-02-14 00:00:00', 'M', '+1-555-0110', false, false),
('Sophia', 'Lee', 'slee', '$2a$10$test123456800', 'sophia.lee@email.com', '1997-06-03 00:00:00', 'F', '+1-555-0111', false, false);

-- Insert Address
INSERT INTO Address (userID, country, city, street, apartment) VALUES 
(1, 'USA', 'New York', '123 Main Street', 'Apt 4B'),
(1, 'USA', 'New York', '456 Park Avenue', null),
(2, 'USA', 'Los Angeles', '789 Sunset Blvd', 'Suite 12'),
(3, 'USA', 'Chicago', '321 Lake Shore Dr', 'Unit 5A'),
(4, 'USA', 'Houston', '654 Texas Avenue', null),
(5, 'USA', 'Phoenix', '987 Desert Road', 'Apt 2C'),
(6, 'USA', 'Philadelphia', '147 Liberty Street', null),
(7, 'USA', 'San Antonio', '258 River Walk', 'Unit 8'),
(8, 'USA', 'San Diego', '369 Beach Boulevard', 'Apt 3D'),
(10, 'USA', 'Dallas', '741 Commerce Street', null),
(11, 'USA', 'Seattle', '852 Pine Street', 'Apt 6F'),
(12, 'USA', 'Boston', '963 Beacon Hill', 'Unit 9B');
