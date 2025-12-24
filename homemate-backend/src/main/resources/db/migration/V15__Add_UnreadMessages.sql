USE HomeMate;

-- Add unread message count columns to Chat table
ALTER TABLE Chat
ADD COLUMN userUnreadMessages INT DEFAULT 0 NOT NULL;

ALTER TABLE Chat
ADD COLUMN taskerUnreadMessages INT DEFAULT 0 NOT NULL;

-- Optional: Create indexes for better performance when querying unread counts
CREATE INDEX idx_chat_user_unread ON Chat(userID, userUnreadMessages);
CREATE INDEX idx_chat_tasker_unread ON Chat(taskerID, taskerUnreadMessages);

-- Optional: Update existing rows to have 0 unread (though DEFAULT handles new rows)
UPDATE Chat SET userUnreadMessages = 0 WHERE userUnreadMessages IS NULL;
UPDATE Chat SET taskerUnreadMessages = 0 WHERE taskerUnreadMessages IS NULL;