CREATE TABLE core.assignment(
  client_id        CITEXT NOT NULL,
  detectable_id    CITEXT NOT NULL,
  detectable_type  VARCHAR(12) NOT NULL,
  client_reference CITEXT NOT NULL,
  since            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, detectable_id, detectable_type),
  CONSTRAINT fk_assignment_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder (client_id, client_reference),
  CONSTRAINT fk_assignment_to_detectable
    FOREIGN KEY (client_id, detectable_id, detectable_type)
    REFERENCES core.detectable
);
