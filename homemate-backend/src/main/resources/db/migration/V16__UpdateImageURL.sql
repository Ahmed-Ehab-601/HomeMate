use HomeMate;

DELETE From MessageImage;
DELETE From ReviewImage;
UPDATE Tasker SET image = NULL;
UPDATE Service SET imageData = NULL;

ALTER TABLE Tasker
MODIFY COLUMN image VARCHAR(500) DEFAULT NULL;

ALTER TABLE Service
MODIFY COLUMN imageData VARCHAR(500) DEFAULT NULL;

ALTER TABLE ReviewImage
MODIFY COLUMN imageFile VARCHAR(500) NOT NULL;

ALTER TABLE MessageImage
MODIFY COLUMN imageFile VARCHAR(500) NOT NULL;


UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271837/homemate/services/house_cleaning.jpg'
WHERE serviceID = 1;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271838/homemate/services/plumbing.jpg'
WHERE serviceID = 2;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271840/homemate/services/electrical_work.jpg'
WHERE serviceID = 3;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271842/homemate/services/gardening.jpg'
WHERE serviceID = 4;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271843/homemate/services/painting.jpg'
WHERE serviceID = 5;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271845/homemate/services/carpentry.jpg'
WHERE serviceID = 6;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766274337/homemate/services/moving_delivery.jpg'
WHERE serviceID = 7;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271847/homemate/services/hvac_services.jpg'
WHERE serviceID = 8;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271849/homemate/services/pet_care.jpg'
WHERE serviceID = 9;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271851/homemate/services/tutoring.jpg'
WHERE serviceID = 10;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271853/homemate/services/cooking_services.jpg'
WHERE serviceID = 11;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271854/homemate/services/it_support.jpg'
WHERE serviceID = 12;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271856/homemate/services/car_washing.jpg'
WHERE serviceID = 13;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271857/homemate/services/home_repair.jpg'
WHERE serviceID = 14;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271859/homemate/services/fitness_training.jpg'
WHERE serviceID = 15;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271860/homemate/services/baby_sitting.jpg'
WHERE serviceID = 16;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271863/homemate/services/elder_care.jpg'
WHERE serviceID = 17;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271865/homemate/services/interior_design.jpg'
WHERE serviceID = 18;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271866/homemate/services/photography.jpg'
WHERE serviceID = 19;

UPDATE Service
SET imageData = 'https://res.cloudinary.com/ddcruu1sb/image/upload/v1766271868/homemate/services/home_security_installation.jpg'
WHERE serviceID = 20;

-- ============================================================
-- UPDATE TASKER IMAGES (UNIQUE, FULL-BODY PREFERRED)
-- ============================================================

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/4bmLaXHBRZi6Sn6XE1eCAM6KDs0.jpg' WHERE username = 'tanderson';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/jVnXWFJzO4ISX796tUiLCb4BnRo.jpg' WHERE username = 'mwhite';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/dCZgjQ42Wm7vbQWNGsJEGOMBeBB.jpg' WHERE username = 'jclark';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/aEHKI3KscJB3c5wUK9nIY7IntZ0.jpg' WHERE username = 'swalker';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/31CCvoNLwcJMtfYVF1jZ4Goxfn.jpg' WHERE username = 'dallen';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/knEdFFkDr0dvgBNC6DROsJN8F1a.jpg' WHERE username = 'oking';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/r0RMbUmnBuRbwLLFat60eBFq4B4.jpg' WHERE username = 'hmoore';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/gGR1ZmdJmRdhBXJkQG90yVPQWZ4.jpg' WHERE username = 'ereed';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/duDDRRuYjC3qvabxA4w1e1y7g6R.jpg' WHERE username = 'lwright';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/69ttY5VkABXQeTcFZBkxcxvaXjf.jpg' WHERE username = 'mevans';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/o1dNtPiLsTqznAal7WXsnbNs5AN.jpg' WHERE username = 'nscott';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/lZEwFrEsmz9hCaoqfNGVuqRbgFi.jpg' WHERE username = 'lward';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/7iGxBlHhjJNSQ4wilu50ptHAMWD.jpg' WHERE username = 'dbell';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/nRTT7nJ1ZwtpLmgkwCNF8r6YPZf.jpg' WHERE username = 'mcook';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/4pPNFB55e2WWY042yh4QMZNNdwN.jpg' WHERE username = 'jlee';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/mgQhox7AbzVsQ8xeTZvkK5Gvkd4.jpg' WHERE username = 'amitchell';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/wgcKLJhSVMOE9yOa4QQILDo966G.jpg' WHERE username = 'wperry';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/vYyvP5NHlVUNdKmUgX3e7X5sAW.jpg' WHERE username = 'ohoward';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/8uSJfakEstaPOBqCtn1oIzJm7VZ.jpg' WHERE username = 'cfleming';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/kbGkIR3pGj7bLlYko1AOKzLrjRI.jpg' WHERE username = 'gbryant';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/nLYYmE7KYZT4yVgr4dnoCRlFdfZ.jpg' WHERE username = 'lthompson';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/8t6l70q7XT8Mv1hQSFqHqqnVSgX.jpg' WHERE username = 'kharris';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/qZNsj0HCQOGlStJdVwm0Zf8in2g.jpg' WHERE username = 'nlewis';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/r3YpjAV9xAd0gKMqBM3I66bOtTg.jpg' WHERE username = 'phall';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/o8Cn0nzoUpsA6d8dORsZJWGVTCB.jpg' WHERE username = 'myoung';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/9cDjVg6P4EsBsIJClPWgPc8Gck9.jpg' WHERE username = 'egreen';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/q5tSanaqYG1ko6dR3PMWaFtN6tL.jpg' WHERE username = 'sbaker';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/uvPEfK58pZiy9SO3IjM1R9T9Qkn.jpg' WHERE username = 'gprice';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/nRTT7nJ1ZwtpLmgkwCNF8r6YPZf.jpg' WHERE username = 'vbrooks';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/wLVWybflDcrp8rDWAmovBqbN4qA.jpg' WHERE username = 'hrivera';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/oxX093z8VFpDUTDTR0T0WFaMIos.jpg' WHERE username = 'acooper';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/52e4H8J66b2jMBSt9SrlQgisirB.jpg' WHERE username = 'ccruz';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/uVXx3XnkYShPxXorPobnQgA94bM.jpg' WHERE username = 'emurphy';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/kNXX9aE5XpW2igRuEzAlbJ0r1yL.jpg' WHERE username = 'zmorgan';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/jx9SoUGkMlDwLtTkYQGkZuv6jTF.jpg' WHERE username = 'lturner';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/z2bEbhdv3gRKPeZ9aASshDgWDEP.jpg' WHERE username = 'nross';

UPDATE Tasker SET image = 'https://upload.wikimedia.org/wikipedia/commons/7/7d/Hala_shyha.jpg' WHERE username = 'bfoster';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/xwt3vXxlnn3ACC3pNAPVtffxYSP.jpg' WHERE username = 'award';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/no2jruBnomJjqEjSoUf3qhlHz5L.jpg' WHERE username = 'hsummers';

UPDATE Tasker SET image = 'https://image.tmdb.org/t/p/original/gz0vmtCF9uEvw5f1wjLsqfGUjRc.jpg' WHERE username = 'scole';




