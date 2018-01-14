CREATE TABLE attendance.processing_state(
  client_id        CITEXT NOT NULL,
  reader_sn        VARCHAR(64) NOT NULL,
  port_number      INTEGER NOT NULL,
  processed_up_to  TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, reader_sn, port_number),
  CONSTRAINT fk_processing_state_to_antenna
    FOREIGN KEY (client_id, reader_sn, port_number)
    REFERENCES core.antenna
);
