CREATE TABLE core.client(
  client_id     CITEXT NOT NULL,
  display_name  VARCHAR(64) NOT NULL,
  logo          BYTEA NOT NULL,
  register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CHECK (LENGTH(client_id) <= 64),
  CHECK (LENGTH(logo) <= 100000)
);
