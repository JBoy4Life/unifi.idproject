CREATE TABLE core.carrier(
  client_id    CITEXT NOT NULL,
  site_id      CITEXT NOT NULL,
  carrier_id   CITEXT NOT NULL,
  carrier_type VARCHAR(12) NOT NULL,
  active       INTEGER NOT NULL,
  since        TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, site_id, carrier_id, carrier_type),
  CONSTRAINT fk_carrier_to_site
    FOREIGN KEY (client_id, site_id)
    REFERENCES core.site,
  CHECK (LENGTH(carrier_id) <= 64),
  CHECK (carrier_type IN ('card'))
);
