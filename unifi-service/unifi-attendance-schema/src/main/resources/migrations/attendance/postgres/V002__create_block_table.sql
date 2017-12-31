CREATE TABLE attendance.block(
  client_id   CITEXT NOT NULL,
  schedule_id CITEXT NOT NULL,
  block_id    CITEXT NOT NULL,
  name        VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, schedule_id, block_id),
  CONSTRAINT fk_block_to_schedule
  FOREIGN KEY (client_id, schedule_id)
  REFERENCES attendance.schedule,
  CHECK (LENGTH(block_id) <= 64)
);
