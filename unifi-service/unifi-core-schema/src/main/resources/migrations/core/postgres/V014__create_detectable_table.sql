CREATE TABLE core.detectable(
  client_id        CITEXT NOT NULL,
  detectable_id    CITEXT NOT NULL,
  detectable_type  VARCHAR(12) NOT NULL,
  description      VARCHAR(64) NOT NULL,
  active           BOOLEAN NOT NULL DEFAULT TRUE,

  PRIMARY KEY (client_id, detectable_id, detectable_type),
  CHECK (LENGTH(detectable_id) <= 64),
  CHECK (detectable_type IN ('uhf-epc', 'uhf-tid'))
);
