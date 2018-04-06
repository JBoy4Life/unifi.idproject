CREATE EXTENSION IF NOT EXISTS citext SCHEMA public;

CREATE TABLE core.client(
  client_id     CITEXT NOT NULL,
  display_name  VARCHAR(64) NOT NULL,
  register_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CHECK (LENGTH(client_id) <= 64)
);

CREATE TABLE core.operator(
  client_id     CITEXT NOT NULL,
  username      CITEXT NOT NULL,
  name          VARCHAR(64) NOT NULL,
  email         VARCHAR(64) NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  since         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username),
  CONSTRAINT fk_operator_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (LENGTH(username) >= 1 AND LENGTH(username) <= 64)
);

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

CREATE TABLE core.operator_password_reset(
  client_id           CITEXT NOT NULL,
  username            CITEXT NOT NULL,
  token_hash          BYTEA NOT NULL,
  algorithm           VARCHAR(12) NOT NULL,
  expiry_date         TIMESTAMP NOT NULL,
  since               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username, since),
  CONSTRAINT fk_operator_password_reset_to_operator
    FOREIGN KEY (client_id, username)
    REFERENCES core.operator,
  CHECK (LENGTH(token_hash) = 56),
  CHECK (algorithm IN ('scrypt'))
);

CREATE TABLE core.operator_password_reset_history(
  client_id           CITEXT NOT NULL,
  username            CITEXT NOT NULL,
  token_hash          BYTEA NOT NULL,
  algorithm           VARCHAR(12) NOT NULL,
  expiry_date         TIMESTAMP NOT NULL,
  deletion_reason     VARCHAR(12) NOT NULL,
  since               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username, since),
  CONSTRAINT fk_operator_password_reset_to_operator
    FOREIGN KEY (client_id, username)
    REFERENCES core.operator,
  CHECK (LENGTH(token_hash) = 56),
  CHECK (deletion_reason IN ('used', 'expired', 'cancelled'))
);

CREATE TABLE core.holder(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  holder_type      VARCHAR(12) NOT NULL,
  name             VARCHAR(64) NOT NULL,
  active           BOOLEAN NOT NULL DEFAULT TRUE,

  PRIMARY KEY (client_id, client_reference),
  UNIQUE (client_id, client_reference, holder_type),
  CONSTRAINT fk_holder_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (LENGTH(client_reference) <= 64),
  CHECK (holder_type IN ('contact', 'asset'))
);

CREATE TABLE core.contact(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  holder_type      VARCHAR(12) NOT NULL,

  PRIMARY KEY (client_id, client_reference),
  UNIQUE (client_id, client_reference, holder_type),
  CONSTRAINT fk_contact_to_holder
    FOREIGN KEY (client_id, client_reference, holder_type)
    REFERENCES core.holder (client_id, client_reference, holder_type),
  CHECK (holder_type = 'contact')
);

CREATE TABLE core.site(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  description VARCHAR(64) NOT NULL,
  address     VARCHAR(256) NOT NULL,
  time_zone   VARCHAR(64) NOT NULL DEFAULT 'Europe/London',

  PRIMARY KEY (client_id, site_id),
  CONSTRAINT fk_site_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client
);

CREATE TABLE core.zone(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  zone_id     CITEXT NOT NULL,
  name        VARCHAR(64) NOT NULL,
  description VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, site_id, zone_id),
  CONSTRAINT fk_zone_to_site
    FOREIGN KEY (client_id, site_id)
    REFERENCES core.site
);

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

CREATE TABLE core.reader(
  client_id   CITEXT NOT NULL,
  site_id     CITEXT NOT NULL,
  reader_sn   VARCHAR(64) NOT NULL,
  agent_id    CITEXT NOT NULL,
  endpoint    VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, reader_sn),
  UNIQUE (client_id, site_id, reader_sn),
  CONSTRAINT fk_reader_to_site
    FOREIGN KEY (client_id, site_id)
    REFERENCES core.site,
  CONSTRAINT fk_reader_to_agent
    FOREIGN KEY (client_id, agent_id)
    REFERENCES core.agent
);

CREATE TABLE core.antenna(
  client_id   CITEXT NOT NULL,
  reader_sn   VARCHAR(64) NOT NULL,
  port_number INTEGER NOT NULL,
  site_id     CITEXT NOT NULL,
  zone_id     CITEXT NOT NULL,
  active      BOOLEAN NOT NULL DEFAULT TRUE,

  PRIMARY KEY (client_id, reader_sn, port_number),
  CONSTRAINT fk_antenna_to_reader
    FOREIGN KEY (client_id, site_id, reader_sn)
    REFERENCES core.reader (client_id, site_id, reader_sn),
  CONSTRAINT fk_antenna_to_zone
    FOREIGN KEY (client_id, site_id, zone_id)
    REFERENCES core.zone
);

CREATE TABLE core.detectable(
  client_id        CITEXT NOT NULL,
  detectable_id    CITEXT NOT NULL,
  detectable_type  VARCHAR(12) NOT NULL,
  description      VARCHAR(64) NOT NULL,
  active           BOOLEAN NOT NULL DEFAULT TRUE,

  PRIMARY KEY (client_id, detectable_id, detectable_type),
  CHECK (LENGTH(detectable_id) <= 64),
  CHECK (detectable_type IN ('uhf-epc', 'uhf-tid', 'mifare-csn', 'prox-id'))
);

CREATE TABLE core.assignment(
  client_id        CITEXT NOT NULL,
  detectable_id    CITEXT NOT NULL,
  detectable_type  VARCHAR(12) NOT NULL,
  client_reference CITEXT NOT NULL,
  since            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, detectable_id, detectable_type),
  CONSTRAINT fk_assignment_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder (client_id, client_reference),
  CONSTRAINT fk_assignment_to_detectable
    FOREIGN KEY (client_id, detectable_id, detectable_type)
    REFERENCES core.detectable
);

CREATE TABLE core.rfid_detection(
  client_id       CITEXT NOT NULL,
  detectable_id   CITEXT NOT NULL,
  detectable_type VARCHAR(12) NOT NULL,
  reader_sn       VARCHAR(64) NOT NULL,
  port_number     INTEGER NOT NULL,
  detection_time  TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, detectable_id, detectable_type, reader_sn, port_number, detection_time),
  CONSTRAINT fk_rfid_detection_to_detectable
    FOREIGN KEY (client_id, detectable_id, detectable_type)
    REFERENCES core.detectable,
  CONSTRAINT fk_rfid_detection_to_antenna
    FOREIGN KEY (client_id, reader_sn, port_number)
    REFERENCES core.antenna
);
CREATE TABLE core.holder_metadata(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  metadata         JSONB NOT NULL DEFAULT '{}',

  PRIMARY KEY (client_id, client_reference),
  CONSTRAINT fk_holder_metadata_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder
);

CREATE INDEX ON core.rfid_detection (detection_time);

CREATE TABLE core.holder_image(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  image            BYTEA NOT NULL,
  mime_type        VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id, client_reference),
  CONSTRAINT fk_holder_image_to_holder
    FOREIGN KEY (client_id, client_reference)
    REFERENCES core.holder,
  CHECK (LENGTH(image) <= 1048576),
  CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/gif', 'image/svg+xml'))
);

CREATE TABLE core.client_config(
  client_id                       CITEXT NOT NULL,
  delete_detections_after         INTERVAL NULL,
  live_view_enabled               BOOLEAN NULL,
  live_view_zone_template         VARCHAR(256) NULL,
  live_view_all_template          VARCHAR(256) NULL,
  since                           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id),
  CONSTRAINT fk_client_config_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client,
  CHECK (delete_detections_after >= INTERVAL '0 seconds')
);

CREATE TABLE core.client_vertical(
  client_id   CITEXT NOT NULL,
  vertical_id VARCHAR(12) NOT NULL,

  PRIMARY KEY (client_id, vertical_id),
  CONSTRAINT fk_client_vertical_to_client
    FOREIGN KEY (client_id)
    REFERENCES core.client
);

CREATE TABLE core.client_image(
  client_id        CITEXT NOT NULL,
  image            BYTEA NOT NULL,
  mime_type        VARCHAR(64) NOT NULL,

  PRIMARY KEY (client_id),
  CONSTRAINT fk_client_image_to_client
  FOREIGN KEY (client_id)
  REFERENCES core.client,
  CHECK (LENGTH(image) <= 1048576),
  CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/gif', 'image/svg+xml'))
);
