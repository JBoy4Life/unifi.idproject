CREATE TABLE core.holder_metadata(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  metadata         JSONB NOT NULL DEFAULT '{}',

  PRIMARY KEY (client_id, client_reference),
  CONSTRAINT fk_holder_metadata_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder
);
