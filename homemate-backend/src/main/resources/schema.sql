-- ======================================================
-- DATABASE SCHEMA
-- ======================================================

-- create database HomeMate;

-- Drop tables in correct order (optional)
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS message_img;
DROP TABLE IF EXISTS Message;
DROP TABLE IF EXISTS Chat;
DROP TABLE IF EXISTS Review_Image;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS Report;
DROP TABLE IF EXISTS Task;
DROP TABLE IF EXISTS Address;
DROP TABLE IF EXISTS Tasker;
DROP TABLE IF EXISTS Service;
DROP TABLE IF EXISTS User;

SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================
-- USER
-- ======================================================
CREATE TABLE User (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50),
    lastName VARCHAR(50),
    username VARCHAR(50) UNIQUE,
    password VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    birthDate TIMESTAMP,
    gender CHAR(1),
    phone VARCHAR(50),
    admin BOOLEAN DEFAULT FALSE,
    suspended BOOLEAN DEFAULT FALSE
);

-- ======================================================
-- ADDRESS
-- ======================================================
CREATE TABLE Address (
    addressID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT,
    country VARCHAR(50),
    city VARCHAR(50),
    street VARCHAR(50),
    apartment VARCHAR(50),
    FOREIGN KEY (userID) REFERENCES User(userID)
);

-- ======================================================
-- SERVICE
-- ======================================================
CREATE TABLE Service (
    serviceID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(100),
    imagedata LONGBLOB,
    imageName VARCHAR(200),
    imageType VARCHAR(200)
);

-- ======================================================
-- TASKER
-- ======================================================
CREATE TABLE Tasker (
    taskerID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50),
    lastName VARCHAR(50),
    username VARCHAR(50),
    password VARCHAR(50),
    email VARCHAR(50),
    birthDate TIMESTAMP,
    phone VARCHAR(50),
    gender CHAR(1),
    image LONGBLOB,
    availability ENUM('available','unavailable'),
    rating DECIMAL(5,2),
    hourrate DECIMAL(10,2),
    bio VARCHAR(500),
    serviceID INT,
    totalEarning DOUBLE,
    WorkedHours DOUBLE,
    addressCity VARCHAR(200),
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID)
);

-- ======================================================
-- CHAT
-- ======================================================
CREATE TABLE Chat (
    chat_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    tasker_id INT,
    useris_active BOOLEAN DEFAULT TRUE,
    taskeris_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES User(userID),
    FOREIGN KEY (tasker_id) REFERENCES Tasker(taskerID)
);

-- ======================================================
-- TASK
-- ======================================================
CREATE TABLE Task (
    taskID INT AUTO_INCREMENT PRIMARY KEY,
    startDate TIMESTAMP,
    workedHours FLOAT,
    userID INT,
    taskerID INT,
    serviceID INT,
    finishDate TIMESTAMP,
    chatID INT,
    bill FLOAT,
    status ENUM('inReview','Accepted','Inprogress','suspended','done'),
    startInprogress TIMESTAMP,
    addressID INT,
    descriptionNotes VARCHAR(250),

    FOREIGN KEY (userID) REFERENCES User(userID),
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID),
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID),
    FOREIGN KEY (chatID) REFERENCES Chat(chat_id),
    FOREIGN KEY (addressID) REFERENCES Address(addressID)
);

-- ======================================================
-- REPORT
-- ======================================================
CREATE TABLE Report (
    reportID INT AUTO_INCREMENT PRIMARY KEY,
    header VARCHAR(50),
    body VARCHAR(200),
    taskID INT,
    reporter BOOLEAN, -- true = user, false = tasker
    adminStatus ENUM('pending','done'),
    FOREIGN KEY (taskID) REFERENCES Task(taskID)
);

-- ======================================================
-- REVIEWS
-- ======================================================
CREATE TABLE Reviews (
    reviewID INT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(50),
    rate DECIMAL(3,1),
    time TIMESTAMP,
    taskID INT,
    FOREIGN KEY (taskID) REFERENCES Task(taskID)
);

-- ======================================================
-- REVIEW IMAGE
-- ======================================================
CREATE TABLE Review_Image (
    imgId INT AUTO_INCREMENT PRIMARY KEY,
    format VARCHAR(50),
    ImgFile LONGBLOB,
    ImgName VARCHAR(100),
    review_id INT,
    FOREIGN KEY (review_id) REFERENCES Reviews(reviewID)
);

-- ======================================================
-- MESSAGE
-- ======================================================
CREATE TABLE Message (
    messageId INT AUTO_INCREMENT PRIMARY KEY,
    chat_id INT,
    content VARCHAR(200),
    timestamp TIMESTAMP,
    senderid INT,
    receiverid INT,
    isusersender BOOLEAN, -- true=user, false=tasker
    status ENUM('sent','received','seen'),
    FOREIGN KEY (chat_id) REFERENCES Chat(chat_id)
);

-- ======================================================
-- MESSAGE IMAGE
-- ======================================================
CREATE TABLE message_img (
    imgId INT AUTO_INCREMENT PRIMARY KEY,
    messageID INT,
    format VARCHAR(50),
    ImgFile LONGBLOB,
    ImgName VARCHAR(100),
    FOREIGN KEY (messageID) REFERENCES Message(messageId)
);
