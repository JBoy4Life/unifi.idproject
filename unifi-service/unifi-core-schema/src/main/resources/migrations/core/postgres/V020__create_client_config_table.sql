CREATE TABLE core.client_config(
  client_id                       CITEXT NOT NULL,
  delete_detections_after         INTERVAL NULL,
  live_view_enabled               BOOLEAN NULL,
  live_view_zone_template         VARCHAR(256) NULL,
  live_view_all_template          VARCHAR(256) NULL,
  since                           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CONSTRAINT fk_client_config_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (delete_detections_after >= INTERVAL '0 seconds')
);
