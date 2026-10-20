INSERT INTO rbf (city_id, vehicle_type_id, fee)
SELECT c.id, v.id, f.fee
FROM (VALUES
    ('Tallinn', 'Car',     4.0),
    ('Tallinn', 'Scooter', 3.5),
    ('Tallinn', 'Bike',    3.0),
    ('Tartu',   'Car',     3.5),
    ('Tartu',   'Scooter', 3.0),
    ('Tartu',   'Bike',    2.5),
    ('Pärnu',   'Car',     3.0),
    ('Pärnu',   'Scooter', 2.5),
    ('Pärnu',   'Bike',    2.0)
) AS f(city_name, vehicle_type, fee)
JOIN city c    ON c.city_name = f.city_name
JOIN vehicle v ON v.vehicle_type = f.vehicle_type;

INSERT INTO atef (condition, temperature, max_temperature, fee) VALUES ('less', -10.0, NULL, 1.0);
INSERT INTO atef (condition, temperature, max_temperature, fee) VALUES ('inRange', -10.0, 0.0, 0.5);

INSERT INTO wsef (condition, wind_speed, max_wind_speed, fee) VALUES ('inRange', 10.0, 20.0, 0.5);
INSERT INTO wsef (condition, wind_speed, max_wind_speed, fee) VALUES ('greater', 20.0, NULL, NULL);

INSERT INTO wpef (condition, phenomenon, fee) VALUES ('related', 'snow',    1.0);
INSERT INTO wpef (condition, phenomenon, fee) VALUES ('related', 'sleet',   1.0);
INSERT INTO wpef (condition, phenomenon, fee) VALUES ('related', 'rain',    0.5);
INSERT INTO wpef (condition, phenomenon, fee) VALUES ('related', 'glaze',   NULL);
INSERT INTO wpef (condition, phenomenon, fee) VALUES ('related', 'hail',    NULL);
INSERT INTO wpef (condition, phenomenon, fee) VALUES ('related', 'thunder', NULL);