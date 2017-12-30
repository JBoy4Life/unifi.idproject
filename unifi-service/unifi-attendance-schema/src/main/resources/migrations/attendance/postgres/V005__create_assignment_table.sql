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
