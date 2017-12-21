CREATE TABLE core.carrier(
  client_id    CITEXT NOT NULL,
  carrier_id   CITEXT NOT NULL,
  carrier_type VARCHAR(12) NOT NULL,
  active       BOOLEAN NOT NULL,
  since        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, carrier_id, carrier_type),
  CONSTRAINT fk_carrier_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (LENGTH(carrier_id) <= 64),
  CHECK (carrier_type IN ('card'))
);
