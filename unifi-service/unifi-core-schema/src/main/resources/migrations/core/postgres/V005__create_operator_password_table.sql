CREATE TABLE core.operator_password(
  client_id     CITEXT NOT NULL,
  username      CITEXT NOT NULL,
  password_hash BYTEA NOT NULL,
  algorithm     VARCHAR(12) NOT NULL,
  since         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username),
  CONSTRAINT fk_operator_password_to_operator
    FOREIGN KEY (client_id, username)
    REFERENCES core.operator,
  CHECK (LENGTH(password_hash) = 56),
  CHECK (algorithm IN ('scrypt'))
);
