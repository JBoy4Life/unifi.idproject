CREATE TABLE attendance.client_config(
  client_id                  CITEXT NOT NULL,
  vertical_id                VARCHAR(12) NOT NULL,
  grace_period_before_block  INTERVAL NULL,
  grace_period_after_block   INTERVAL NULL,
  since                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CONSTRAINT fk_client_config_to_client_vertical
    FOREIGN KEY (client_id, vertical_id)
    REFERENCES core.client_vertical,
  CHECK (vertical_id = 'attendance'),
  CHECK (grace_period_before_block >= INTERVAL '0 seconds'),
  CHECK (grace_period_after_block >= INTERVAL '0 seconds')
);

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

CREATE TABLE attendance.assignment(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  schedule_id      CITEXT NOT NULL,

  PRIMARY KEY (client_id, client_reference, schedule_id),
  CONSTRAINT fk_assignment_to_contact
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.contact,
  CONSTRAINT fk_assignment_to_schedule
    FOREIGN KEY (client_id, schedule_id)
    REFERENCES attendance.schedule
);

CREATE TABLE attendance.attendance(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  schedule_id      CITEXT NOT NULL,
  block_id         CITEXT NOT NULL,
  processed_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, client_reference, schedule_id, block_id),
  CONSTRAINT fk_attendance_to_assignment
    FOREIGN KEY (client_id, client_reference, schedule_id)
    REFERENCES attendance.assignment,
  CONSTRAINT fk_attendance_to_block
    FOREIGN KEY (client_id, schedule_id, block_id)
    REFERENCES attendance.block
);

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

CREATE TABLE attendance.processing_state(
  client_id        CITEXT NOT NULL,
  reader_sn        VARCHAR(64) NOT NULL,
  port_number      INTEGER NOT NULL,
  processed_up_to  TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, reader_sn, port_number),
  CONSTRAINT fk_processing_state_to_antenna
    FOREIGN KEY (client_id, reader_sn, port_number)
    REFERENCES core.antenna
);
