
-- Drop tables in correct order
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS Reviews CASCADE;
DROP TABLE IF EXISTS Report CASCADE;
DROP TABLE IF EXISTS Chat CASCADE;
DROP TABLE IF EXISTS Task CASCADE;
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
    suspended BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE
);

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
    FOREIGN KEY (addressID) REFERENCES Address(addressID) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ======================================================
-- CHAT
-- ======================================================
CREATE TABLE Chat (
    chatID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    taskerID INT NOT NULL,
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ======================================================
-- REPORT
-- ======================================================
CREATE TABLE Report (
    reportID INT AUTO_INCREMENT PRIMARY KEY,
    taskID INT NOT NULL,
    header VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    reporter BOOLEAN NOT NULL,
    adminStatus VARCHAR(20) DEFAULT 'pending' NOT NULL CHECK (adminStatus IN ('pending', 'done')),
    FOREIGN KEY (taskID) REFERENCES Task(taskID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ======================================================
-- REVIEWS
-- ======================================================
CREATE TABLE Reviews (
    reviewID INT AUTO_INCREMENT PRIMARY KEY,
    taskID INT NOT NULL,
    rate DECIMAL(2,1) NOT NULL CHECK (rate >= 1.0 AND rate <= 5.0),
    comment VARCHAR(500),
    FOREIGN KEY (taskID) REFERENCES Task(taskID) ON DELETE CASCADE ON UPDATE CASCADE
);
