CREATE TABLE core.assignment(
  client_id        CITEXT NOT NULL,
  carrier_id       CITEXT NOT NULL,
  carrier_type     VARCHAR(12) NOT NULL,
  client_reference CITEXT NOT NULL,
  since            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, carrier_id, carrier_type),
  CONSTRAINT fk_assignment_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder (client_id, client_reference),
  CONSTRAINT fk_assignment_to_carrier
    FOREIGN KEY (client_id, carrier_id, carrier_type)
    REFERENCES core.carrier
);
