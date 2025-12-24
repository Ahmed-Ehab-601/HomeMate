USE HomeMate;

ALTER TABLE Users
    ADD COLUMN createdTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- For users with tasks: set createdTime to before earliest task startDate by 30–180 random days
-- For users without tasks: set createdTime to 60–360 random days in the past
UPDATE Users u
SET u.createdTime = (
    CASE
        WHEN EXISTS (SELECT 1 FROM Task t WHERE t.userID = u.userID) THEN (
            SELECT DATE_SUB(MIN(t.startDate), INTERVAL FLOOR(30 + RAND() * 150) DAY)
            FROM Task t
            WHERE t.userID = u.userID
        )
        ELSE DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(60 + RAND() * 300) DAY)
    END
);
