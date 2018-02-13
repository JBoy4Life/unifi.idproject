CREATE TABLE attendance.schedule(
  client_id   CITEXT NOT NULL,
  schedule_id CITEXT NOT NULL,
  name        VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, schedule_id),
  CONSTRAINT fk_schedule_to_client_config
    FOREIGN KEY (client_id)
    REFERENCES attendance.client_config,
  CHECK (LENGTH(schedule_id) <= 64)
);
