CREATE TABLE attendance.block_zone(
  client_id   CITEXT NOT NULL,
  schedule_id CITEXT NOT NULL,
  block_id    CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  zone_id     CITEXT NOT NULL,
  since       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, schedule_id, block_id),
  CONSTRAINT fk_block_zone_to_block
    FOREIGN KEY (client_id, schedule_id, block_id)
    REFERENCES attendance.block,
  CONSTRAINT fk_block_zone_to_zone
    FOREIGN KEY (client_id, site_id, zone_id)
    REFERENCES core.zone
);
