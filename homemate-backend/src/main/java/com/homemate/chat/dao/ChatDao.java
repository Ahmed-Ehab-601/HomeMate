package com.homemate.chat.dao;

import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.rowMapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.chrono.JapaneseDate;

//CREATE TABLE Chat (
//    chatID INT AUTO_INCREMENT PRIMARY KEY,
//    userID INT NOT NULL,
//    taskerID INT NOT NULL,
//    userIsActive BOOLEAN DEFAULT TRUE NOT NULL,
//    taskerIsActive BOOLEAN DEFAULT TRUE NOT NULL,
//    UNIQUE KEY unique_chat (userID, taskerID),
//    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE ON UPDATE CASCADE,
//    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE CASCADE ON UPDATE CASCADE,
//    INDEX idx_chat_user (userID),
//    INDEX idx_chat_tasker (taskerID)
//);
//CREATE TABLE Users (
//    userID INT AUTO_INCREMENT PRIMARY KEY,
//    firstName VARCHAR(50) NOT NULL,
//    lastName VARCHAR(50) NOT NULL,
//    username VARCHAR(50) NOT NULL UNIQUE,
//    password VARCHAR(255) NOT NULL, -- Increased for hashed passwords
//    email VARCHAR(100) NOT NULL UNIQUE,
//    birthDate TIMESTAMP NULL,
//    gender ENUM('M', 'F'),
//    phone VARCHAR(50),
//    admin BOOLEAN DEFAULT FALSE NOT NULL,
//    suspended BOOLEAN DEFAULT FALSE NOT NULL,
//    INDEX idx_username (username),
//    INDEX idx_email (email),
//    INDEX idx_suspended (suspended)
//);
@Component
public class ChatDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String getUserPhone(Long id)throws SQLException {
        String sql="SELECT phone FROM Users u INNER JOIN Chat c on c.userID = u.userID WHERE c.chatID = ? ";
        return jdbcTemplate.queryForObject(sql,String.class,id);
    }
    public String getTaskerPhone(Long id) throws SQLException{
        String sql="SELECT phone FROM Tasker t INNER JOIN Chat c on c.taskerID = t.taskerID WHERE c.chatID = ? ";
        return jdbcTemplate.queryForObject(sql,String.class,id);
    }

    public ChatDto getChat(Long chatId) throws SQLException{
        String sql="SELECT * FROM Chat WHERE chatID = ?";
        return jdbcTemplate.queryForObject(sql,new ChatMapper(),chatId);
    }

    public String getNameTasker(Long taskerId) {
        String sql="SELECT username FROM Tasker WHERE taskerID = ?";
        return jdbcTemplate.queryForObject(sql,String.class,taskerId);
    }

    public String getNameUser(Long userId) {
        String sql="SELECT username FROM Users WHERE userID = ?";
        return jdbcTemplate.queryForObject(sql,String.class,userId);
    }
}
