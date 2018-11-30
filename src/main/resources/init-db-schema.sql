CREATE TABLE city (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  name varchar(255) NOT NULL,
  UNIQUE KEY (name)
);

CREATE TABLE batch (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  batch_id int NOT NULL,
  UNIQUE KEY (batch_id)
);

CREATE TABLE deployment (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  city_id bigint(20) NOT NULL,
  batch_id bigint(20) NOT NULL,
  FOREIGN KEY (city_id) REFERENCES city(id),
  FOREIGN KEY (batch_id) REFERENCES batch(id)
);