-- ======================================================
-- HOMEMATE DATABASE MIGRATION V3
-- ======================================================
-- This migration adds additional messages and message images
-- to the existing chat data

USE HomeMate;

-- ======================================================
-- ADD NEW MESSAGES TO EXISTING CHATS
-- ======================================================
-- These messages expand the conversation history between users and taskers
-- Note: The senderID and receiverID follow the pattern from the original data
-- where they both reference the same IDs (user or tasker) from their respective tables

INSERT INTO Message (chatID, content, timestamp, senderID, receiverID, isUserSender, status) VALUES
-- Chat 1: User 1 and Tasker 1 (additional messages)
(1, 'Here is a photo of the apartment', '2024-10-31 08:30:00', 1, 1, TRUE, 'seen'),
(1, 'Thanks for sharing! I can see what needs to be done', '2024-10-31 08:35:00', 1, 1, FALSE, 'seen'),
(1, 'Great! See you Friday', '2024-10-31 08:40:00', 1, 1, TRUE, 'seen'),
(1, NULL, '2024-10-31 08:50:00', 1, 1, TRUE, 'seen'), -- Image-only message with NULL content

-- Chat 2: User 2 and Tasker 2 (additional messages)
(2, 'Check out this leak', '2024-11-02 10:15:00', 2, 2, TRUE, 'seen'),
(2, 'I see the issue. I will bring the right parts', '2024-11-02 10:20:00', 2, 2, FALSE, 'seen'),
(2, NULL, '2024-11-02 10:30:00', 2, 2, TRUE, 'seen'), -- Image-only message with NULL content

-- Chat 3: User 3 and Tasker 3 (additional messages)
(3, 'Here is the current setup', '2024-11-04 10:25:00', 3, 3, TRUE, 'seen'),
(3, 'Perfect! I will bring modern LED fixtures', '2024-11-04 10:30:00', 3, 3, FALSE, 'seen'),
(3, 'Sounds good!', '2024-11-04 10:35:00', 3, 3, TRUE, 'seen'),
(3, NULL, '2024-11-04 10:40:00', 3, 3, TRUE, 'seen'); -- Image-only message with NULL content

-- ======================================================
-- ADD MESSAGE IMAGES
-- ======================================================
-- These images are attached to specific messages sent by users
-- The messageID values will be auto-generated, so we need to find them first
-- We'll use the message content and timestamp to identify the correct messages

-- For production use, you might want to verify these IDs match your actual data
-- The IDs below assume the messages were inserted in order starting from ID 13

INSERT INTO MessageImage (messageID, format, ImageFile, ImageName) VALUES
-- Image for "Here is a photo of the apartment" message (Chat 1)
-- This should be message ID 13 (first new message in this migration)
(13, 'jpg', 0x89504E470D0A1A0A, 'apartment_photo.jpg'),

-- Image for NULL content message (Chat 1) - image-only message
-- This should be message ID 16 (fourth new message in this migration)
(16, 'jpg', 0x89504E470D0A1A0A, 'apartment_detail.jpg'),

-- Image for "Check out this leak" message (Chat 2)
-- This should be message ID 17 (fifth new message in this migration)
(17, 'jpg', 0x89504E470D0A1A0A, 'leaking_faucet_detail.jpg'),

-- Image for NULL content message (Chat 2) - image-only message
-- This should be message ID 19 (seventh new message in this migration)
(19, 'jpg', 0x89504E470D0A1A0A, 'leak_closeup.jpg'),

-- Image for "Here is the current setup" message (Chat 3)
-- This should be message ID 20 (eighth new message in this migration)
(20, 'jpg', 0x89504E470D0A1A0A, 'electrical_setup.jpg'),

-- Image for NULL content message (Chat 3) - image-only message
-- This should be message ID 23 (eleventh new message in this migration)
(23, 'jpg', 0x89504E470D0A1A0A, 'wiring_diagram.jpg');

-- ======================================================
-- VERIFICATION QUERIES (COMMENTED OUT)
-- ======================================================
-- Use these queries to verify the data was inserted correctly:

-- Check new messages:
-- SELECT m.messageId, m.chatID, m.content, m.timestamp, m.isUserSender, m.status
-- FROM Message m
-- WHERE m.messageId >= 13
-- ORDER BY m.chatID, m.timestamp;

-- Check message images with their associated messages:
-- SELECT mi.imageID, mi.messageID, m.content, mi.imageName, mi.format
-- FROM MessageImage mi
-- JOIN Message m ON mi.messageID = m.messageId
-- WHERE mi.imageID >= 4
-- ORDER BY mi.messageID;

-- Check messages with their images (LEFT JOIN to see all messages):
-- SELECT
--     m.messageId,
--     m.chatID,
--     m.content,
--     m.timestamp,
--     m.isUserSender,
--     mi.imageID,
--     mi.imageName
-- FROM Message m
-- LEFT JOIN MessageImage mi ON m.messageId = mi.messageID
-- WHERE m.messageId >= 13
-- ORDER BY m.chatID, m.timestamp;

-- ======================================================
-- MIGRATION SUMMARY
-- ======================================================
-- Data added:
-- - 12 new Messages across 3 existing chats
--   * Chat 1 (User 1, Tasker 1): 4 messages (1 with text+image, 1 image-only with NULL content)
--   * Chat 2 (User 2, Tasker 2): 4 messages (1 with text+image, 1 image-only with NULL content)
--   * Chat 3 (User 3, Tasker 3): 4 messages (1 with text+image, 1 image-only with NULL content)
-- - 6 new MessageImages attached to specific messages
--
-- MESSAGE ID MAPPING (assuming continuation from V2's 12 messages):
-- ID 13: "Here is a photo of the apartment" (Chat 1) + image
-- ID 14: "Thanks for sharing! I can see what needs to be done" (Chat 1)
-- ID 15: "Great! See you Friday" (Chat 1)
-- ID 16: NULL [image-only message] (Chat 1) + image ⭐
-- ID 17: "Check out this leak" (Chat 2) + image
-- ID 18: "I see the issue. I will bring the right parts" (Chat 2)
-- ID 19: NULL [image-only message] (Chat 2) + image ⭐
-- ID 20: "Here is the current setup" (Chat 3) + image
-- ID 21: "Perfect! I will bring modern LED fixtures" (Chat 3)
-- ID 22: "Sounds good!" (Chat 3)
-- ID 23: NULL [image-only message] (Chat 3) + image ⭐
--
-- IMPORTANT NOTES:
-- 1. Message IDs are auto-incremented. The IDs in MessageImage inserts
--    assume messages start at ID 13 (continuing from V2's 12 messages)
-- 2. If you've modified V2 data, adjust the messageID values in
--    MessageImage inserts accordingly
-- 3. The binary data (0x89504E470D0A1A0A) is placeholder - replace with
--    actual image data in production
-- 4. Three messages have NULL content to represent image-only messages (marked with ⭐)
-- 5. Your MessageRowMapper must handle NULL content properly:
--    - message.setContent(rs.getString("content")) will set NULL if content is NULL
--    - Frontend must handle NULL content gracefully
-- ======================================================