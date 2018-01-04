CREATE TABLE core.client(
  client_id     CITEXT NOT NULL,
  display_name  VARCHAR(64) NOT NULL,
  register_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CHECK (LENGTH(client_id) <= 64)
);
