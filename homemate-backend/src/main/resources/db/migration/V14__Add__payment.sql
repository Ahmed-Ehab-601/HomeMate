use HomeMate;
CREATE TABLE payments (
                          paymentID INT AUTO_INCREMENT PRIMARY KEY ,
                          taskId INT NOT NULL,
                          userId INT NOT NULL,
                          taskerId INT NOT NULL,
                          totalAmount  FLOAT DEFAULT 0,
                          platformFee FLOAT DEFAULT 0,
                          taskerAmount FLOAT DEFAULT 0,
                          stripePaymentIntentId VARCHAR(255),
                          status VARCHAR(30), -- CREATED, REQUIRES_PAYMENT, PAID, FAILED
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          paid_at TIMESTAMP,
                          FOREIGN KEY (taskId) REFERENCES Task(taskID)
);


ALTER TABLE Tasker
    ADD COLUMN stripe_account_id VARCHAR(255),
ADD COLUMN stripe_enabled BOOLEAN DEFAULT FALSE;

ALTER TABLE Users
    ADD COLUMN stripe_customer_id VARCHAR(255) NULL;
