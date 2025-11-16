INSERT INTO User (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended)
VALUES
('Mahmoud', 'Saleh', 'mahmoud01', 'pass123', 'mah01@mail.com', '2003-03-12', 'M', '0100000001', TRUE, FALSE),
('Sara', 'Ali', 'sara22', 'pass222', 'sara22@mail.com', '1999-07-20', 'F', '0100000002', FALSE, FALSE);

INSERT INTO Address (userID, country, city, street, apartment)
VALUES
(1, 'Egypt', 'Alexandria', 'Smouha', '12A'),
(2, 'Egypt', 'Cairo', 'Nasr City', '10/5');

INSERT INTO Service (name, description, imageName, imageType)
VALUES
('Cleaning', 'Home and office cleaning', 'clean.png', 'image/png'),
('Plumbing', 'Pipes, leaks, repairs', 'plumb.png', 'image/png');

INSERT INTO Tasker (firstName, lastName, username, password, email, birthDate, phone, gender,
                    availability, rating, hourrate, bio, serviceID, totalEarning, WorkedHours, addressCity)
VALUES
('Omar', 'Hassan', 'omar_task', 'pass111', 'omar@mail.com', '1995-04-10', '0100003333', 'M',
 'available', 4.8, 120, 'Professional cleaner', 1, 5000, 200, 'Cairo'),

('Lina', 'Youssef', 'lina_task', 'pass222', 'lina@mail.com', '1998-02-01', '0100004444', 'F',
 'available', 4.9, 140, 'Expert plumber', 2, 8000, 300, 'Alexandria');

INSERT INTO Chat (user_id, tasker_id, useris_active, taskeris_active)
VALUES
(1, 1, TRUE, TRUE),
(2, 2, TRUE, TRUE);

INSERT INTO Task (startDate, workedHours, userID, taskerID, serviceID, finishDate, chatID, bill,
                  status, startInprogress, addressID, descriptionNotes)
VALUES
('2025-01-01 10:00:00', 3.5, 1, 1, 1, '2025-01-01 13:30:00', 1, 420,
 'done', '2025-01-01 10:05:00', 1, 'Cleaned living room and kitchen'),

('2025-01-03 09:00:00', 2.0, 2, 2, 2, '2025-01-03 11:00:00', 2, 280,
 'done', '2025-01-03 09:10:00', 2, 'Fixed broken sink');

INSERT INTO Report (header, body, taskID, reporter, adminStatus)
VALUES
('Issue with Cleaning', 'Minor issues found', 1, TRUE, 'pending'),
('Plumbing Feedback', 'All good', 2, FALSE, 'done');

INSERT INTO Reviews (text, rate, time, taskID)
VALUES
('Great work!', 5.0, NOW(), 1),
('Very professional', 4.5, NOW(), 2);

INSERT INTO Review_Image (format, ImgName, review_id)
VALUES
('png', 'review1_img.png', 1),
('jpg', 'review2_img.jpg', 2);

INSERT INTO Message (chat_id, content, timestamp, senderid, receiverid, isusersender, status)
VALUES
(1, 'Hello Omar!', NOW(), 1, 1, TRUE, 'sent'),
(1, 'Hello Mahmoud, I’m on my way.', NOW(), 1, 1, FALSE, 'received'),

(2, 'Hi Lina, the sink is leaking badly.', NOW(), 2, 2, TRUE, 'sent'),
(2, 'I will arrive in 20 minutes.', NOW(), 2, 2, FALSE, 'received');

INSERT INTO message_img (messageID, format, ImgName)
VALUES
(1, 'png', 'chatimg1.png'),
(3, 'jpg', 'sink_leak.jpg');

