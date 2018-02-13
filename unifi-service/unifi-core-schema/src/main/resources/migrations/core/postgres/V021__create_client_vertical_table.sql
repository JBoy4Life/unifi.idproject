CREATE TABLE core.client_vertical(
  client_id   CITEXT NOT NULL,
  vertical_id VARCHAR(12) NOT NULL,

  PRIMARY KEY (client_id, vertical_id),
  CONSTRAINT fk_client_vertical_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client
);
