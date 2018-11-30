CREATE TABLE city (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  name varchar(255) NOT NULL,
  latitude real(20) NOT NULL,
  longitude real(20) NOT NULL,
  cap int NOT NULL DEFAULT 2147483647,
  UNIQUE KEY (name)
);

CREATE TABLE batch (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  batch_id int NOT NULL,
  batch_count int NOT NULL,
  UNIQUE KEY (batch_id)
);

CREATE TABLE deployment (
  id bigint(20) NOT NULL auto_increment PRIMARY KEY,
  city_id bigint(20) NOT NULL,
  batch_id bigint(20) NOT NULL,
  start_date TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date TIMESTAMP WITH TIME ZONE NOT NULL,
  FOREIGN KEY (city_id) REFERENCES city(id),
  FOREIGN KEY (batch_id) REFERENCES batch(id)
);