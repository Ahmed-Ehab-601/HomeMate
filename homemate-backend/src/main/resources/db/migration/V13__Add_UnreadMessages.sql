use HomeMate;

ALTER TABLE Chat
ADD COLUMN userUnreadMessages Int DEFAULT 0;
ALTER TABLE Chat
ADD COLUMN taskerUnreadMessages Int DEFAULT 0;

