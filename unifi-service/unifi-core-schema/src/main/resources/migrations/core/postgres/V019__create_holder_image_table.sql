CREATE TABLE core.holder_image(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  image            BYTEA NOT NULL,

  PRIMARY KEY (client_id, client_reference),
  CONSTRAINT fk_holder_image_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder,
  CHECK (LENGTH(image) <= 1048576)
);
