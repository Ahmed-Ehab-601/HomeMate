use HomeMate;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS MessageImage;
DROP TABLE IF EXISTS Message;
DROP TABLE IF EXISTS Chat;
DROP TABLE IF EXISTS ReviewImage;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS Report;
DROP TABLE IF EXISTS Task;
DROP TABLE IF EXISTS Address;
DROP TABLE IF EXISTS Tasker;
DROP TABLE IF EXISTS Service;
DROP TABLE IF EXISTS Users;

SET FOREIGN_KEY_CHECKS = 1;

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

CREATE TABLE Service (
    serviceID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    imageData LONGBLOB,
    imageName VARCHAR(200),
    imageType VARCHAR(200),
    INDEX idx_service_name (name)
);

CREATE TABLE Tasker (
    taskerID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    birthDate TIMESTAMP NULL,
    phone VARCHAR(50),
    gender ENUM('M', 'F'),
    image LONGBLOB,
    availability ENUM('available','unavailable') DEFAULT 'available' NOT NULL,
    rating DECIMAL(5,2) DEFAULT 0.00,
    hourRate DECIMAL(10,2) NOT NULL,
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

CREATE TABLE Chat (
    chatID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    taskerID INT NOT NULL,
    userIsActive BOOLEAN DEFAULT TRUE NOT NULL,
    taskerIsActive BOOLEAN DEFAULT TRUE NOT NULL,
    UNIQUE KEY unique_chat (userID, taskerID),
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_chat_user (userID),
    INDEX idx_chat_tasker (taskerID)
);

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
    status ENUM('InReview','Accepted','InProgress','Suspended','Done','Rejected') DEFAULT 'InReview' NOT NULL,
    startInProgress TIMESTAMP NULL,
    addressID INT NOT NULL,
    description VARCHAR(500),
    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (addressID) REFERENCES Address(addressID) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_task_user (userID),
    INDEX idx_task_tasker (taskerID),
    INDEX idx_task_service (serviceID),
    INDEX idx_task_status (status),
    INDEX idx_task_start_date (startDate),
    INDEX idx_task_finish_date (endDate),
    INDEX idx_task_chat (chatID)
);

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

CREATE TABLE ReviewImage (
    imageID INT AUTO_INCREMENT PRIMARY KEY,
    format VARCHAR(50) NOT NULL,
    imageFile LONGBLOB NOT NULL,
    imageName VARCHAR(100) NOT NULL,
    reviewID INT NOT NULL,
    FOREIGN KEY (reviewID) REFERENCES Reviews(reviewID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_review_img_review (reviewID)
);

CREATE TABLE Message (
    messageId INT AUTO_INCREMENT PRIMARY KEY,
    chatID INT NOT NULL,
    content VARCHAR(200),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    senderID INT NOT NULL,
    receiverID INT NOT NULL,
    isUserSender BOOLEAN NOT NULL, -- true=user, false=tasker
    status ENUM('sent','received','seen') DEFAULT 'sent' NOT NULL,
    FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_message_chat (chatID),
    INDEX idx_message_timestamp (timestamp),
    INDEX idx_message_sender (senderID),
    INDEX idx_message_receiver (receiverID),
    INDEX idx_message_status (status)
);

CREATE TABLE MessageImage (
    imageID INT AUTO_INCREMENT PRIMARY KEY,
    messageID INT NOT NULL,
    format VARCHAR(50) NOT NULL,
    imageFile LONGBLOB NOT NULL,
    imageName VARCHAR(100) NOT NULL,
    FOREIGN KEY (messageID) REFERENCES Message(messageID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_msg_img_message (messageID)
);