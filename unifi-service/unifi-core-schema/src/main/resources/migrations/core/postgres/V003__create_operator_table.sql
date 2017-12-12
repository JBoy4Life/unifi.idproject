CREATE TABLE core.operator(
  client_id     CITEXT NOT NULL,
  username      CITEXT NOT NULL,
  email         VARCHAR(64) NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  since         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username),
  CONSTRAINT fk_operator_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (LENGTH(username) >= 1 AND LENGTH(username) <= 64)
);
