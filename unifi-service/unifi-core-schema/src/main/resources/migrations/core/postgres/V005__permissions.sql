CREATE TABLE core.operation (
  operation    VARCHAR(64) NOT NULL,
  description  VARCHAR(256) NOT NULL DEFAULT '',

  PRIMARY KEY (operation)
);

CREATE TABLE core.permission (
  client_id     CITEXT NOT NULL,
  username      CITEXT NOT NULL,
  operation     VARCHAR(64) NOT NULL,
  granted_by    CITEXT NOT NULL,
  since         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (client_id, username, operation),
  CONSTRAINT fk_permission_to_operator
    FOREIGN KEY (client_id, username)
    REFERENCES core.operator,
  CONSTRAINT fk_permission_to_granting_operator
    FOREIGN KEY (client_id, granted_by)
    REFERENCES core.operator (client_id, username),
  CONSTRAINT fk_permission_to_operation
    FOREIGN KEY (operation)
    REFERENCES core.operation
);

CREATE TABLE core.permission_history (
  client_id     CITEXT NOT NULL,
  username      CITEXT NOT NULL,
  operation     VARCHAR(64) NOT NULL,
  granted_by    CITEXT NOT NULL,
  revoked_by    CITEXT NOT NULL,
  since         TIMESTAMP NOT NULL,
  until         TIMESTAMP NOT NULL,

  PRIMARY KEY (client_id, username, operation, since),
  CONSTRAINT fk_permission_to_operator
    FOREIGN KEY (client_id, username)
    REFERENCES core.operator,
  CONSTRAINT fk_permission_to_granting_operator
    FOREIGN KEY (client_id, granted_by)
    REFERENCES core.operator (client_id, username),
  CONSTRAINT fk_permission_to_revoking_operator
    FOREIGN KEY (client_id, revoked_by)
    REFERENCES core.operator (client_id, username)
);

CREATE TABLE core.role (
  role         VARCHAR(64) NOT NULL,
  description  VARCHAR(256) NOT NULL DEFAULT '',

  PRIMARY KEY (role)
);

CREATE TABLE core.role_operation (
  role         VARCHAR(64) NOT NULL,
  operation    VARCHAR(64) NOT NULL,

  PRIMARY KEY (role, operation),

  CONSTRAINT fk_role_operation_to_operation
    FOREIGN KEY (operation)
    REFERENCES core.operation
);
