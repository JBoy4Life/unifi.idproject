CREATE TABLE core.antenna(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  reader_sn   VARCHAR(64) NOT NULL,
  port_number INTEGER NOT NULL,
  zone_id     CITEXT NOT NULL,

  PRIMARY KEY (client_id, site_id, reader_sn, port_number),
  CONSTRAINT fk_antenna_to_reader
    FOREIGN KEY (client_id, site_id, reader_sn)
    REFERENCES core.reader
);
