USE HomeMate;

ALTER TABLE Tasker
    ADD COLUMN createdTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- For taskers with tasks: set createdTime to before earliest task startDate by 30–180 random days
-- For taskers without tasks: set createdTime to 60–360 random days in the past
UPDATE Tasker t
SET t.createdTime = (
    CASE
        WHEN EXISTS (SELECT 1 FROM Task WHERE taskerID = t.taskerID) THEN
            DATE_SUB(
                (SELECT MIN(startDate) FROM Task WHERE taskerID = t.taskerID),
                INTERVAL FLOOR(30 + RAND() * 150) DAY
            )
        ELSE
            DATE_SUB(NOW(), INTERVAL FLOOR(60 + RAND() * 300) DAY)
    END
);
