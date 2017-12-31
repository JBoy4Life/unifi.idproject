CREATE TABLE attendance.block_time(
  client_id   CITEXT NOT NULL,
  schedule_id CITEXT NOT NULL,
  block_id    CITEXT NOT NULL,
  start_time  TIMESTAMP NOT NULL,
  end_time    TIMESTAMP NOT NULL,
  since       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, schedule_id, block_id),
  CONSTRAINT fk_block_time_to_block
    FOREIGN KEY (client_id, schedule_id, block_id)
    REFERENCES attendance.block,
  CHECK (end_time > start_time)
);
