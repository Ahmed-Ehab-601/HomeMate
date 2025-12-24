-- ======================================================
-- PAYMENT TEST DATABASE SCHEMA
-- Simplified schema for payment integration tests
-- ======================================================

-- Drop tables in correct order
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS Task CASCADE;
DROP TABLE IF EXISTS Address CASCADE;
DROP TABLE IF EXISTS Chat CASCADE;
DROP TABLE IF EXISTS Tasker CASCADE;
DROP TABLE IF EXISTS Service CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

SET REFERENTIAL_INTEGRITY TRUE;

-- ======================================================
-- USERS (Minimal for payment tests)
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
                        availability VARCHAR(20) DEFAULT 'available' NOT NULL,
                        rating DECIMAL(5,2) DEFAULT 0.00,
                        hourRate DECIMAL(10,2) NOT NULL,
                        bio VARCHAR(500),
                        serviceID INT NOT NULL,
                        totalEarning DOUBLE DEFAULT 0.00 NOT NULL,
                        WorkedHours DOUBLE DEFAULT 0.00 NOT NULL,
                        addressCity VARCHAR(200),
                        stripe_account_id VARCHAR(255),
                        FOREIGN KEY (serviceID) REFERENCES Service(serviceID)
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
                         FOREIGN KEY (userID) REFERENCES Users(userID)
);

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
                      FOREIGN KEY (userID) REFERENCES Users(userID),
                      FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID)
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
                      status VARCHAR(20) DEFAULT 'InReview' NOT NULL,
                      estimation INT DEFAULT 0,
                      startInProgress TIMESTAMP NULL,
                      addressID INT NOT NULL,
                      description VARCHAR(500),
                      paid BOOLEAN DEFAULT FALSE,
                      FOREIGN KEY (userID) REFERENCES Users(userID),
                      FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID),
                      FOREIGN KEY (serviceID) REFERENCES Service(serviceID),
                      FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE SET NULL,
                      FOREIGN KEY (addressID) REFERENCES Address(addressID)
);

CREATE INDEX idx_task_status ON Task(status);

-- ======================================================
-- PAYMENTS
-- ======================================================
CREATE TABLE payments (
                          paymentID INT AUTO_INCREMENT PRIMARY KEY,
                          taskId INT NOT NULL,
                          userId INT NOT NULL,
                          taskerId INT NOT NULL,
                          totalAmount FLOAT DEFAULT 0,
                          platformFee FLOAT DEFAULT 0,
                          taskerAmount FLOAT DEFAULT 0,
                          stripePaymentIntentId VARCHAR(255),
                          status VARCHAR(30),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          paid_at TIMESTAMP,
                          FOREIGN KEY (taskId) REFERENCES Task(taskID)
);

CREATE INDEX idx_payment_task ON payments(taskId);
CREATE INDEX idx_payment_status ON payments(status);