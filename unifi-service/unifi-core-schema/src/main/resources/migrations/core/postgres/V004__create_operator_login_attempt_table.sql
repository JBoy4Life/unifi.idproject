CREATE TABLE core.operator_login_attempt(
  client_id     CITEXT NOT NULL,
  username      CITEXT NOT NULL,
  successful    BOOLEAN,
  attempt_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username, attempt_time),
  CONSTRAINT fk_operator_login_attempt_to_operator
    FOREIGN KEY (client_id, username)
    REFERENCES core.operator
);
