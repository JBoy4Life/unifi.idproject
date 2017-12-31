CREATE TABLE core.uhf_detection(
  client_id       CITEXT NOT NULL,
  detectable_id   CITEXT NOT NULL,
  detectable_type VARCHAR(12) NOT NULL,
  reader_sn       CITEXT NOT NULL,
  port_number     INTEGER NOT NULL,
  detection_time  TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, detectable_id, detectable_type, reader_sn, port_number, detection_time),
  CONSTRAINT fk_uhf_detection_to_detectable
    FOREIGN KEY (client_id, detectable_id, detectable_type)
    REFERENCES core.detectable,
  CONSTRAINT fk_uhf_detection_to_antenna
    FOREIGN KEY (client_id, reader_sn, port_number)
    REFERENCES core.antenna
);
