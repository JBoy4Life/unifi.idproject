CREATE TABLE core.holder(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  holder_type      VARCHAR(12) NOT NULL,
  name             VARCHAR(64) NOT NULL,
  active           BOOLEAN NOT NULL,

  PRIMARY KEY (client_id, client_reference),
  UNIQUE (client_id, client_reference, holder_type),
  CONSTRAINT fk_holder_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (LENGTH(client_reference) <= 64),
  CHECK (holder_type IN ('contact', 'asset'))
);
