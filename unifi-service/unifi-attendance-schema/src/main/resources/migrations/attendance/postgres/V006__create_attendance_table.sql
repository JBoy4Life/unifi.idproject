CREATE TABLE attendance.attendance(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  schedule_id      CITEXT NOT NULL,
  block_id         CITEXT NOT NULL,

  PRIMARY KEY (client_id, client_reference, schedule_id, block_id),
  CONSTRAINT fk_attendance_to_assignment
    FOREIGN KEY (client_id, client_reference, schedule_id)
    REFERENCES attendance.assignment,
  CONSTRAINT fk_attendance_to_block
    FOREIGN KEY (client_id, schedule_id, block_id)
    REFERENCES attendance.block
);
