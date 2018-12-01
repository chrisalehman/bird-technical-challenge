--DROP TABLE city;
--DROP TABLE batch;
--DROP TABLE deployment;

CREATE TABLE city (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  name varchar(255) NOT NULL,
  latitude real(20) NOT NULL,
  longitude real(20) NOT NULL,
  cap int NOT NULL DEFAULT 2147483647
);

CREATE UNIQUE INDEX idx_unique_city_name
ON city (name);

CREATE TABLE batch (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  batch_number int NOT NULL,
  size int NOT NULL
);

CREATE UNIQUE INDEX idx_unique_batch_number
ON batch (batch_number);

CREATE TABLE deployment (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  city_id bigint(20) NOT NULL,
  batch_id bigint(20) NOT NULL,
  start_date TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date TIMESTAMP WITH TIME ZONE NOT NULL,
  FOREIGN KEY (city_id) REFERENCES city(id),
  FOREIGN KEY (batch_id) REFERENCES batch(id),
  CHECK (deployment.start_date < deployment.end_date)
);

CREATE INDEX idx_deployment_business_key
ON deployment (city_id, batch_id);