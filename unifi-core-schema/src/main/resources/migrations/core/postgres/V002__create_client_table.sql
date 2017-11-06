CREATE TABLE client(
  client_id    CITEXT NOT NULL,
  display_name VARCHAR(64),
  logo         BYTEA,
  since        TIMESTAMP DEFAULT current_timestamp,

  PRIMARY KEY (client_id),
  CHECK (length(client_id) <= 64),
  CHECK (length(logo) <= 100000)
);
