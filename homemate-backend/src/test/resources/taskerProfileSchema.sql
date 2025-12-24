
-- Drop tables in correct order
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS MessageImage CASCADE;
DROP TABLE IF EXISTS Message CASCADE;
DROP TABLE IF EXISTS Chat CASCADE;
DROP TABLE IF EXISTS ReviewImage CASCADE;
DROP TABLE IF EXISTS Reviews CASCADE;
DROP TABLE IF EXISTS Report CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
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
    suspended BOOLEAN DEFAULT FALSE NOT NULL,
    stripe_customer_id VARCHAR(255) NULL
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
    stripe_account_id VARCHAR(255),
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE
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
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE CASCADE ON UPDATE CASCADE
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
    paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (addressID) REFERENCES Address(addressID) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ======================================================
-- REVIEWS
-- ======================================================
CREATE TABLE Reviews (
    reviewID INT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(50),
    rate DECIMAL(3,1) NOT NULL CHECK (rate >= 0 AND rate <= 5),
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    taskID INT NOT NULL UNIQUE,
    FOREIGN KEY (taskID) REFERENCES Task(taskID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ======================================================
-- REVIEW IMAGE
-- ======================================================
CREATE TABLE ReviewImage (
    imageID INT AUTO_INCREMENT PRIMARY KEY,
    format VARCHAR(50) NOT NULL,
    imageFile BLOB NOT NULL,
    imageName VARCHAR(100) NOT NULL,
    reviewID INT NOT NULL,
    FOREIGN KEY (reviewID) REFERENCES Reviews(reviewID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE payments (
                          paymentID INT AUTO_INCREMENT PRIMARY KEY ,
                          taskId INT NOT NULL,
                          userId INT NOT NULL,
                          taskerId INT NOT NULL,
                          totalAmount  FLOAT DEFAULT 0,
                          platformFee FLOAT DEFAULT 0,
                          taskerAmount FLOAT DEFAULT 0,
                          stripePaymentIntentId VARCHAR(255),
                          status VARCHAR(30), -- CREATED, REQUIRES_PAYMENT, PAID, FAILED
                          created_at TIMESTAMP,
                          paid_at TIMESTAMP,
                          FOREIGN KEY (taskId) REFERENCES Task(taskID)
);


