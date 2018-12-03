CREATE TABLE city (
id BIGINT(20) NOT NULL auto_increment PRIMARY KEY,
name varchar(255) NOT NULL,
latitude double NOT NULL,
longitude double NOT NULL,
cap int NOT NULL DEFAULT 2147483647
);

CREATE TABLE batch (
  id BIGINT(20) NOT NULL auto_increment PRIMARY KEY,
batch_number int NOT NULL,
size int NOT NULL
);

CREATE TABLE deployment (
  id BIGINT(20) NOT NULL auto_increment PRIMARY KEY,
city_id BIGINT(20) NOT NULL,
batch_id BIGINT(20) NOT NULL,
start_date TIMESTAMP NOT NULL,
end_date TIMESTAMP NOT NULL,
FOREIGN KEY (city_id) REFERENCES city(id),
FOREIGN KEY (batch_id) REFERENCES batch(id)
);

CREATE UNIQUE INDEX idx_unique_city_name ON city (name);
CREATE UNIQUE INDEX idx_unique_batch_number ON batch (batch_number);
CREATE INDEX idx_deployment_business_key ON deployment (city_id, batch_id);