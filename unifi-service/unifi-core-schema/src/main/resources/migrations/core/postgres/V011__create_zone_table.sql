CREATE TABLE core.zone(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  zone_id     CITEXT NOT NULL,
  name        VARCHAR(64) NOT NULL,
  description VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, site_id, zone_id),
  CONSTRAINT fk_zone_to_site
    FOREIGN KEY (client_id, site_id)
    REFERENCES core.site
);
