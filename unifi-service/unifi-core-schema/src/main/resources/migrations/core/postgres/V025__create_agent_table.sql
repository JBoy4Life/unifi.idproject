CREATE TABLE core.agent(
  client_id     CITEXT NOT NULL,
  agent_id      CITEXT NOT NULL,

  PRIMARY KEY (client_id, agent_id),
  CONSTRAINT fk_agent_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (LENGTH(agent_id) <= 64)
);

CREATE TABLE core.agent_password(
  client_id     CITEXT NOT NULL,
  agent_id      CITEXT NOT NULL,
  password_hash BYTEA NOT NULL,
  algorithm     VARCHAR(12) NOT NULL,
  since         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, agent_id),
  CONSTRAINT fk_agent_password_to_agent
    FOREIGN KEY (client_id, agent_id)
    REFERENCES core.agent,
  CHECK (LENGTH(password_hash) = 56),
  CHECK (algorithm IN ('scrypt'))
);

ALTER TABLE core.reader
  ADD COLUMN agent_id CITEXT,
  ADD CONSTRAINT fk_reader_to_agent
    FOREIGN KEY (client_id, agent_id)
    REFERENCES core.agent;
