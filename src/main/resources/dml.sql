-- create cityName
INSERT INTO city (name, latitude, longitude, cap)
VALUES ('Los Angeles', 34.048925, -118.428663, 2147483647),
       ('Austin', 30.305804, -97.728682, 500);

-- create batch
INSERT INTO batch (batch_number, size)
VALUES (1, 250),
       (2, 500),
       (3, 200);

-- create deployment
INSERT INTO deployment (city_id, batch_id, start_date, end_date)
VALUES (2, 2, '2018-08-31T00:44:40+00:00', '2018-09-24T00:44:40+00:00'),
       (2, 2, '2018-08-31T00:44:40+00:00', '2018-09-24T00:44:40+00:00'),
       (2, 2, '2018-08-31T00:44:40+00:00', '2018-09-24T00:44:40+00:00');

-- deployment for batch number, city and date
SELECT DISTINCT d.*
FROM deployment d
JOIN batch b ON b.id = d.batch_id
JOIN city c on c.id = d.city_id
WHERE b.batch_number = :1
AND c.name = :2
AND d.start_date <= :3
AND d.end_date >= :4

-- deployments for city
SELECT DISTINCT c.name, b.batch_number, b.size, d.start_date, d.end_date
FROM city c
JOIN deployment d ON c.id = d.city_id
JOIN batch b on b.id = d.batch_id
WHERE c.name = :city
ORDER BY d.start_date ASC, d.end_date ASC;

-- deployments for batch number
SELECT DISTINCT b.batch_number, c.name, d.start_date, d.end_date
FROM batch b
JOIN deployment d ON b.id = d.batch_id
JOIN city c on c.id = d.city_id
WHERE b.batch_number = :batchNumber
ORDER BY b.batch_number ASC, d.start_date ASC, d.end_date ASC;

-- deployments, grouped by city name
SELECT DISTINCT c.name, b.batch_number, b.size, d.start_date, d.end_date
FROM city c
LEFT OUTER JOIN deployment d ON c.id = d.city_id
LEFT OUTER JOIN batch b on b.id = d.batch_id
ORDER BY c.name ASC, d.start_date ASC, d.end_date ASC;

-- deployments, grouped by batch number
SELECT DISTINCT b.batch_number, c.name, d.start_date, d.end_date
FROM batch b
LEFT OUTER JOIN deployment d ON b.id = d.batch_id
LEFT OUTER JOIN city c on c.id = d.city_id
ORDER BY b.batch_number ASC, d.start_date ASC, d.end_date ASC;