CREATE TABLE core_agent.antenna(
  reader_sn   VARCHAR(64) NOT NULL,
  port_number INTEGER NOT NULL,

  PRIMARY KEY (reader_sn, port_number),
  CONSTRAINT fk_antenna_to_reader
    FOREIGN KEY (reader_sn)
    REFERENCES core_agent.reader
);
