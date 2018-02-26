CREATE TABLE core.client_image(
  client_id        CITEXT NOT NULL,
  image            BYTEA NOT NULL,

  PRIMARY KEY (client_id),
  CONSTRAINT fk_client_image_to_client
  FOREIGN KEY (client_id)
  REFERENCES core.client,
  CHECK (LENGTH(image) <= 1048576)
);
