use HomeMate;

ALTER TABLE Chat
ADD COLUMN userUnreadMessages Int DEFAULT 0;
ADD COLUMN taskerUnreadMessages Int DEFAULT 0;

