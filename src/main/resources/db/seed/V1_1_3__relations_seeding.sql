INSERT INTO atef_relation (atef_id, vehicle_id)
SELECT a.id, v.id
FROM atef a
JOIN vehicle v ON v.vehicle_type IN ('Scooter', 'Bike');

INSERT INTO wpef_relation (wpef_id, vehicle_id)
SELECT w.id, v.id
FROM wpef w
JOIN vehicle v ON v.vehicle_type IN ('Scooter', 'Bike');

INSERT INTO wsef_relation (wsef_id, vehicle_id)
SELECT w.id, v.id
FROM wsef w
JOIN vehicle v ON v.vehicle_type = 'Bike';