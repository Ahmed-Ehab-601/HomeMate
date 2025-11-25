package com.homemate.UserProfile.DAO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.DTO.UserRequestTaskerDTO;
import com.homemate.UserProfile.Mappers.UserProfileDTORowMapper;
import com.homemate.UserProfile.Mappers.UserRequestTaskerDTORowMapper;
import com.homemate.UserProfile.Mappers.UserRowMapper;
import com.homemate.UserProfile.Models.User;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private final UserProfileDTORowMapper userProfileRowMapper;
    private final UserRequestTaskerDTORowMapper userRequestTaskerRowMapper;

    public UserDao(JdbcTemplate jdbcTemplate,
                   UserRowMapper userRowMapper,
                   UserProfileDTORowMapper userProfileRowMapper,
                   UserRequestTaskerDTORowMapper userRequestTaskerRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
        this.userProfileRowMapper = userProfileRowMapper;
        this.userRequestTaskerRowMapper = userRequestTaskerRowMapper;
    }
    /**
     * SQL query to get a user by their ID
     */
    private static final String GET_BY_ID_SQL =
    "SELECT userID, username, password, firstName, lastName, email, birthDate, gender, phone, admin, suspended " +
    "FROM users WHERE userID = ?";

    /**
     * Get a user by their ID
     * @param userID The ID of the user to get
     * @return The user with the given ID
     */
    @SuppressWarnings("null")
    public User getByID(Long userID) {
        return jdbcTemplate.queryForObject(
            GET_BY_ID_SQL,
            (RowMapper<User>) this.userRowMapper,
            userID
        );
    }
    /**
     * SQL query to get a user by their email
     */

    private static final String GET_BY_EMAIL_SQL =
    "SELECT userID, username, password, firstName, lastName, email, birthDate, gender, phone, admin, suspended " +
    "FROM users WHERE email = ?";

    /**
     * Get a user by their email
     * @param email The email of the user to get
     * @return The user with the given email
     */
    @SuppressWarnings("null")
    public User getByEmail(String email) {
        return jdbcTemplate.queryForObject(
            GET_BY_EMAIL_SQL,
            (RowMapper<User>) this.userRowMapper,
            email
        );
    }

    /**
     * SQL query to update a user
     */
    private static final String UPDATE_USER_SQL =
    "UPDATE users SET username = ?, email = ?, birthDate = ?, gender = ?, phone = ?, admin = ?, suspended = ?, password = ?, firstName = ?, lastName = ? WHERE userID = ?";

    /**
     * Update a user
     * @param user The user to update
     */
    public void update(User user) {
        jdbcTemplate.update(
            UPDATE_USER_SQL,
            user.getUsername(),
            user.getEmail(),
            user.getBirthDate(),
            user.getGender() != null ? user.getGender().toString() : null,
            user.getPhone(),
            user.getIsAdmin(),
            user.getIsSuspended(),
            user.getPassword(),
            user.getFirstName(),
            user.getLastName(),
            user.getUserID()
        );
    }
    /**
     * SQL query to delete a user
     */
    private static final String DELETE_USER_SQL =
    "DELETE FROM users WHERE userID = ?";

    /**
     * Delete a user
     * @param userID The ID of the user to delete
     */
    public void delete(Long userID) {
        jdbcTemplate.update(DELETE_USER_SQL, userID);
    }

    /**
     * SQL query to sign up a user
     */
    private static final String SIGNUP_USER_SQL =
    "INSERT INTO users (username, firstName, lastName, email, password, birthDate, gender, phone, admin, suspended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    /**
     * Sign up a user
     * @param user The user to sign up
     */
    public void signup(User user) {
        jdbcTemplate.update(SIGNUP_USER_SQL,
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPassword(),
            user.getBirthDate(),
            user.getGender() != null ? user.getGender().toString() : null,
            user.getPhone(),
            user.getIsAdmin(),
            user.getIsSuspended());
    }

    private static final String GET_PROFILE_SQL =
    "SELECT userID, username, password, firstName, lastName, email, birthDate, gender, phone, admin, suspended " +
    "FROM users WHERE userID = ? ";

    /**
     * Get a user's profile
     * @param userID The ID of the user to get the profile of
     * @return The user's profile
     */
    
    @SuppressWarnings("null")
    public User getUserProfile(Long userID) {
        return jdbcTemplate.queryForObject(GET_PROFILE_SQL, (RowMapper<User>) this.userRowMapper, userID);
    }

    private static final String GET_PROFILE_DTO_SQL =
    "SELECT userID, username, firstName, lastName, email, birthDate, gender, phone, admin AS isAdmin, suspended AS isSuspended " +
    "FROM users WHERE userID = ?";

    @SuppressWarnings("null")
    public UserProfileDTO getProfile(Long id) {
        return jdbcTemplate.queryForObject(GET_PROFILE_DTO_SQL, (RowMapper<UserProfileDTO>) this.userProfileRowMapper, id);
    }

    private static final String GET_TASKER_PROFILE_SQL =
    "SELECT taskerID, firstName, lastName, email, phone, rating " +
    "FROM Tasker WHERE taskerID = ?";

    @SuppressWarnings("null")
    public UserRequestTaskerDTO getTaskerProfile(Long id) {
        return jdbcTemplate.queryForObject(
            GET_TASKER_PROFILE_SQL,
            (RowMapper<UserRequestTaskerDTO>) this.userRequestTaskerRowMapper,
            id
        );
    }
}

