--TRUNCATE TABLE city;
--TRUNCATE TABLE batch;
--TRUNCATE TABLE deployment;

INSERT INTO city (name, latitude, longitude, cap)
VALUES ('Los Angeles', 34.048925, -118.428663, 2147483647),
       ('Austin', 30.305804, -97.728682, 500);

INSERT INTO batch (batch_number, size)
VALUES (1, 250),
       (2, 500),
       (3, 200);

INSERT INTO deployment (city_id, batch_id, start_date, end_date)
VALUES (2, 2, '2018-08-31T00:44:40+00:00', '2018-09-24T00:44:40+00:00'),
       (2, 2, '2018-08-31T00:44:40+00:00', '2018-09-24T00:44:40+00:00'),
       (2, 2, '2018-08-31T00:44:40+00:00', '2018-09-24T00:44:40+00:00');

-- deployments, grouped by city name
SELECT c.name, b.batch_number, d.start_date, d.end_date
FROM city c
LEFT OUTER JOIN deployment d ON c.id = d.city_id
LEFT OUTER JOIN batch b on b.id = d.batch_id
ORDER BY c.name ASC, d.start_date ASC, d.end_date ASC

-- deployments, grouped by batch number
SELECT b.batch_number, d.start_date, d.end_date
FROM batch b
LEFT OUTER JOIN deployment d ON b.id = d.batch_id
ORDER BY b.batch_number ASC, d.start_date ASC, d.end_date ASC;