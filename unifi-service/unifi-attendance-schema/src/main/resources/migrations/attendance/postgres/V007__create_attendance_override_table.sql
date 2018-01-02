CREATE TABLE attendance.attendance_override(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  schedule_id      CITEXT NOT NULL,
  block_id         CITEXT NOT NULL,
  status           VARCHAR(12) NOT NULL,
  operator         CITEXT NOT NULL,
  override_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, client_reference, schedule_id, block_id, override_time),
  CONSTRAINT fk_attendance_override_to_assignment
    FOREIGN KEY (client_id, client_reference, schedule_id)
    REFERENCES attendance.assignment,
  CONSTRAINT fk_attendance_override_to_block
    FOREIGN KEY (client_id, schedule_id, block_id)
    REFERENCES attendance.block,
  CONSTRAINT fk_attendance_override_to_operator
    FOREIGN KEY (client_id, operator)
    REFERENCES core.operator (client_id, username),
  CHECK (status IN ('present', 'absent', 'auth-absent'))
);
