CREATE TABLE core.detectable(
  client_id        CITEXT NOT NULL,
  carrier_id       CITEXT NOT NULL,
  carrier_type     VARCHAR(12) NOT NULL,
  detectable_id    CITEXT NOT NULL,
  detectable_type  VARCHAR(12) NOT NULL,

  PRIMARY KEY (client_id, detectable_id, detectable_type),
  CONSTRAINT fk_detectable_to_carrier
    FOREIGN KEY (client_id, carrier_id, carrier_type)
    REFERENCES core.carrier,
  CHECK (LENGTH(detectable_id) <= 64),
  CHECK (detectable_type IN ('uhf-epc', 'uhf-tid'))
);
