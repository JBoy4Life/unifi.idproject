CREATE TABLE core.reader(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  reader_sn   VARCHAR(64) NOT NULL,
  endpoint    VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, site_id, reader_sn),
  CONSTRAINT fk_reader_to_site
    FOREIGN KEY (client_id, site_id)
    REFERENCES core.site
);
