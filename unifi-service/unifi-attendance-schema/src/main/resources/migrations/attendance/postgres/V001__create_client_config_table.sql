CREATE TABLE attendance.client_config(
  client_id                  CITEXT NOT NULL,
  vertical_id                VARCHAR(12) NOT NULL,
  grace_period_before_block  INTERVAL NULL,
  grace_period_after_block   INTERVAL NULL,
  since                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CONSTRAINT fk_client_config_to_client_vertical
    FOREIGN KEY (client_id, vertical_id)
    REFERENCES core.client_vertical,
  CHECK (vertical_id = 'attendance'),
  CHECK (grace_period_before_block >= INTERVAL '0 seconds'),
  CHECK (grace_period_after_block >= INTERVAL '0 seconds')
);
