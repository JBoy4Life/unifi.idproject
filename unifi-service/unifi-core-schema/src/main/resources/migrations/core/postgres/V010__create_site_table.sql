CREATE TABLE core.site(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  description VARCHAR(64) NOT NULL,
  address     VARCHAR(256) NOT NULL,

  PRIMARY KEY (client_id, site_id),
  CONSTRAINT fk_site_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client
);
