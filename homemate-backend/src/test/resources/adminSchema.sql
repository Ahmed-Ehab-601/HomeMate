
-- ======================================================
-- DATABASE SCHEMA WITH CONSTRAINTS, CASCADE, AND INDEXES
-- Updated: Renamed "User" table to Users (unquoted)
-- H2-Compatible / MySQL-Compatible (avoid dialect-specific features)
-- ======================================================

-- Drop tables in correct order
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS Report CASCADE;
DROP TABLE IF EXISTS Task CASCADE;
DROP TABLE IF EXISTS Chat CASCADE;
DROP TABLE IF EXISTS Address CASCADE;
DROP TABLE IF EXISTS Tasker CASCADE;
DROP TABLE IF EXISTS Service CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

SET REFERENTIAL_INTEGRITY TRUE;

-- ======================================================
-- USERS
-- ======================================================
CREATE TABLE Users (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    birthDate TIMESTAMP NULL,
    gender VARCHAR(1) CHECK (gender IN ('M', 'F')),
    phone VARCHAR(50),
    admin BOOLEAN DEFAULT FALSE NOT NULL,
    suspended BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_users_username ON Users(username);
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_users_suspended ON Users(suspended);

-- ======================================================
-- ADDRESS
-- ======================================================
CREATE TABLE Address (
    addressID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    country VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    street VARCHAR(50) NOT NULL,
    apartment VARCHAR(50),
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_address_user ON Address(userID);
CREATE INDEX idx_address_city ON Address(city);

-- ======================================================
-- SERVICE
-- ======================================================
CREATE TABLE Service (
    serviceID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    imageData BLOB,
    imageName VARCHAR(200),
    imageType VARCHAR(200)
);

CREATE INDEX idx_service_name ON Service(name);

-- ======================================================
-- TASKER
-- ======================================================
CREATE TABLE Tasker (
    taskerID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    birthDate TIMESTAMP NULL,
    phone VARCHAR(50),
    gender VARCHAR(1) CHECK (gender IN ('M', 'F')),
    image BLOB,
    availability VARCHAR(20) DEFAULT 'available' NOT NULL CHECK (availability IN ('available','unavailable')),
    rating DECIMAL(5,2) DEFAULT 0.00,
    hourRate DECIMAL(10,2) NOT NULL,
    bio VARCHAR(500),
    serviceID INT NOT NULL,
    totalEarning DOUBLE DEFAULT 0.00 NOT NULL,
    WorkedHours DOUBLE DEFAULT 0.00 NOT NULL,
    addressCity VARCHAR(200),
    suspended BOOLEAN DEFAULT FALSE NOT NULL,  -- ADD THIS LINE
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX idx_tasker_username ON Tasker(username);
CREATE INDEX idx_tasker_email ON Tasker(email);
CREATE INDEX idx_tasker_service ON Tasker(serviceID);
CREATE INDEX idx_tasker_availability ON Tasker(availability);
CREATE INDEX idx_tasker_rating ON Tasker(rating);
CREATE INDEX idx_tasker_city ON Tasker(addressCity);

-- ======================================================
-- CHAT
-- ======================================================
CREATE TABLE Chat (
    chatID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    taskerID INT NOT NULL,
    userIsActive BOOLEAN DEFAULT TRUE NOT NULL,
    taskerIsActive BOOLEAN DEFAULT TRUE NOT NULL,
    UNIQUE (userID, taskerID),
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_chat_user ON Chat(userID);
CREATE INDEX idx_chat_tasker ON Chat(taskerID);

-- ======================================================
-- TASK
-- ======================================================
CREATE TABLE Task (
    taskID INT AUTO_INCREMENT PRIMARY KEY,
    startDate TIMESTAMP NOT NULL,
    workedHours FLOAT DEFAULT 0,
    userID INT NOT NULL,
    taskerID INT NOT NULL,
    serviceID INT NOT NULL,
    endDate TIMESTAMP NULL,
    chatID INT,
    bill FLOAT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'InReview' NOT NULL CHECK (status IN ('InReview','Accepted','InProgress','Suspended','Done','Rejected')),
    startInProgress TIMESTAMP NULL,
    addressID INT NOT NULL,
    description VARCHAR(500),
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (addressID) REFERENCES Address(addressID) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX idx_task_user ON Task(userID);
CREATE INDEX idx_task_tasker ON Task(taskerID);
CREATE INDEX idx_task_service ON Task(serviceID);
CREATE INDEX idx_task_status ON Task(status);
CREATE INDEX idx_task_start_date ON Task(startDate);
CREATE INDEX idx_task_finish_date ON Task(endDate);
CREATE INDEX idx_task_chat ON Task(chatID);

-- ======================================================
-- REPORT
-- ======================================================
CREATE TABLE Report (
    reportID INT AUTO_INCREMENT PRIMARY KEY,
    header VARCHAR(100) NOT NULL,
    body VARCHAR(500) NOT NULL,
    taskID INT NOT NULL,
    reporter BOOLEAN NOT NULL,
    adminStatus VARCHAR(20) DEFAULT 'pending' NOT NULL CHECK (adminStatus IN ('pending','done')),
    FOREIGN KEY (taskID) REFERENCES Task(taskID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_report_task ON Report(taskID);
CREATE INDEX idx_report_admin_status ON Report(adminStatus);

