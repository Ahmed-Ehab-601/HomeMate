-- ======================================================
-- DATABASE SCHEMA WITH CONSTRAINTS, CASCADE, AND INDEXES
-- ======================================================

-- create database HomeMate;
-- use HomeMate;

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
DROP TABLE IF EXISTS Taskers;
DROP TABLE IF EXISTS Service;
DROP TABLE IF EXISTS Users;

SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================
-- USER
-- ======================================================
CREATE TABLE Users (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Increased for hashed passwords
    email VARCHAR(100) NOT NULL UNIQUE,
    birthDate TIMESTAMP NULL,
    gender ENUM('M', 'F'),
    phone VARCHAR(50),
    admin BOOLEAN DEFAULT FALSE NOT NULL,
    suspended BOOLEAN DEFAULT FALSE NOT NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_suspended (suspended)
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
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_user_address (userID),
    INDEX idx_city (city)
);

-- ======================================================
-- SERVICE
-- ======================================================
CREATE TABLE Service (
    serviceID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    imagedata LONGBLOB,
    imageName VARCHAR(200),
    imageType VARCHAR(200),
    INDEX idx_service_name (name)
);

-- ======================================================
-- TASKER
-- ======================================================
CREATE TABLE Taskers (
    taskerID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Increased for hashed passwords
    email VARCHAR(50) NOT NULL UNIQUE,
    birthDate TIMESTAMP NULL,
    phone VARCHAR(50),
    gender ENUM('M', 'F'),
    image LONGBLOB,
    availability ENUM('available','unavailable') DEFAULT 'available' NOT NULL,
    rating DECIMAL(5,2) DEFAULT 0.00,
    hourrate DECIMAL(10,2) NOT NULL,
    bio VARCHAR(500),
    serviceID INT NOT NULL,
    totalEarning DOUBLE DEFAULT 0.00 NOT NULL,
    WorkedHours DOUBLE DEFAULT 0.00 NOT NULL,
    addressCity VARCHAR(200),
    suspended BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_tasker_username (username),
    INDEX idx_tasker_email (email),
    INDEX idx_tasker_service (serviceID),
    INDEX idx_tasker_availability (availability),
    INDEX idx_tasker_rating (rating),
    INDEX idx_tasker_city (addressCity)
);

-- ======================================================
-- CHAT
-- ======================================================
CREATE TABLE Chat (
    chat_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    tasker_id INT NOT NULL,
    useris_active BOOLEAN DEFAULT TRUE NOT NULL,
    taskeris_active BOOLEAN DEFAULT TRUE NOT NULL,
    UNIQUE KEY unique_chat (user_id, tasker_id),
    FOREIGN KEY (user_id) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (tasker_id) REFERENCES Taskers(taskerID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_chat_user (user_id),
    INDEX idx_chat_tasker (tasker_id)
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
    finishDate TIMESTAMP NULL,
    chatID INT,
    bill FLOAT DEFAULT 0,
    status ENUM('inReview','Accepted','Inprogress','suspended','done') DEFAULT 'inReview' NOT NULL,
    startInprogress TIMESTAMP NULL,
    addressID INT NOT NULL,
    descriptionNotes VARCHAR(500),
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Taskers(taskerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (chatID) REFERENCES Chat(chat_id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (addressID) REFERENCES Address(addressID) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_task_user (userID),
    INDEX idx_task_tasker (taskerID),
    INDEX idx_task_service (serviceID),
    INDEX idx_task_status (status),
    INDEX idx_task_start_date (startDate),
    INDEX idx_task_finish_date (finishDate),
    INDEX idx_task_chat (chatID)
);

-- ======================================================
-- REPORT
-- ======================================================
CREATE TABLE Report (
    reportID INT AUTO_INCREMENT PRIMARY KEY,
    header VARCHAR(100) NOT NULL,
    body VARCHAR(500) NOT NULL,
    taskID INT NOT NULL,
    reporter BOOLEAN NOT NULL, -- true = user, false = tasker
    adminStatus ENUM('pending','done') DEFAULT 'pending' NOT NULL,
    FOREIGN KEY (taskID) REFERENCES Task(taskID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_report_task (taskID),
    INDEX idx_report_admin_status (adminStatus)
);

-- ======================================================
-- REVIEWS
-- ======================================================
CREATE TABLE Reviews (
    reviewID INT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(50),
    rate DECIMAL(3,1) NOT NULL CHECK (rate >= 0 AND rate <= 5),
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    taskID INT NOT NULL UNIQUE, -- One review per task
    FOREIGN KEY (taskID) REFERENCES Task(taskID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_review_task (taskID),
    INDEX idx_review_rate (rate),
    INDEX idx_review_time (time)
);

-- ======================================================
-- REVIEW IMAGE
-- ======================================================
CREATE TABLE Review_Image (
    imgId INT AUTO_INCREMENT PRIMARY KEY,
    format VARCHAR(50) NOT NULL,
    ImgFile LONGBLOB NOT NULL,
    ImgName VARCHAR(100) NOT NULL,
    review_id INT NOT NULL,
    FOREIGN KEY (review_id) REFERENCES Reviews(reviewID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_review_img_review (review_id)
);

-- ======================================================
-- MESSAGE
-- ======================================================
CREATE TABLE Message (
    messageId INT AUTO_INCREMENT PRIMARY KEY,
    chat_id INT NOT NULL,
    content VARCHAR(200),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    senderid INT NOT NULL,
    receiverid INT NOT NULL,
    isusersender BOOLEAN NOT NULL, -- true=user, false=tasker
    status ENUM('sent','received','seen') DEFAULT 'sent' NOT NULL,
    FOREIGN KEY (chat_id) REFERENCES Chat(chat_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_message_chat (chat_id),
    INDEX idx_message_timestamp (timestamp),
    INDEX idx_message_sender (senderid),
    INDEX idx_message_receiver (receiverid),
    INDEX idx_message_status (status)
);

-- ======================================================
-- MESSAGE IMAGE
-- ======================================================
CREATE TABLE message_img (
    imgId INT AUTO_INCREMENT PRIMARY KEY,
    messageID INT NOT NULL,
    format VARCHAR(50) NOT NULL,
    ImgFile LONGBLOB NOT NULL,
    ImgName VARCHAR(100) NOT NULL,
    FOREIGN KEY (messageID) REFERENCES Message(messageId) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_msg_img_message (messageID)
);

-- ======================================================
-- SUMMARY OF CHANGES
-- ======================================================
-- NOT NULL Constraints:
--   - Added to all critical fields (names, emails, usernames, passwords)
--   - Foreign keys marked as NOT NULL where relationships are required
--   - Status fields with defaults marked as NOT NULL
--
-- UNIQUE Constraints:
--   - User: username, email
--   - Tasker: username, email
--   - Service: name
--   - Chat: unique_chat (user_id, tasker_id) - prevents duplicate chats
--   - Reviews: taskID - ensures one review per task
--
-- CASCADE Rules:
--   - ON DELETE CASCADE: Address, Chat, Task (children), Report, Reviews, Review_Image, Message, message_img
--   - ON DELETE RESTRICT: Task (prevents deletion of users/taskers with active tasks)
--   - ON DELETE SET NULL: Task.chatID (preserves task if chat is deleted)
--   - ON UPDATE CASCADE: All foreign keys for data consistency
--
-- Indexes:
--   - Primary keys (automatic)
--   - Foreign keys (for join performance)
--   - Frequently queried fields (username, email, status, dates)
--   - Fields used in WHERE clauses and ORDER BY
--   - Composite unique index on Chat for business logic
--
-- Other Improvements:
--   - Password fields increased to VARCHAR(255) for hashed passwords
--   - CHECK constraint on Reviews.rate (0-5 range)
--   - DEFAULT values for timestamps, numeric fields, and status enums
-- ======================================================