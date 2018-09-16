CREATE TABLE core.visit(
  client_id          CITEXT NOT NULL,
  client_reference   CITEXT NOT NULL,
  start_time         TIMESTAMP NOT NULL,
  end_time           TIMESTAMP NOT NULL,
  calculation_method VARCHAR(25) NOT NULL,
  site_id            CITEXT NOT NULL,

  PRIMARY KEY (client_id, client_reference, site_id, start_time),
  CONSTRAINT fk_visit_to_site
    FOREIGN KEY (client_id, site_id)
    REFERENCES core.site,
  CONSTRAINT fk_visit_to_contact
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.contact,

CHECK(calculation_method IN ('interpolated-day',
 'interpolated-month', 'interpolated-site',
 'interpolated-night', 'measured-day', 'measured-night'))
);
