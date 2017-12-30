CREATE TABLE core.contact(
  client_id        CITEXT NOT NULL,
  client_reference CITEXT NOT NULL,
  holder_type      VARCHAR(12) NOT NULL,

  PRIMARY KEY (client_id, client_reference, holder_type),
  UNIQUE (client_id, client_reference),
  CONSTRAINT fk_contact_to_holder
    FOREIGN KEY (client_id, client_reference, holder_type)
    REFERENCES core.holder,
  CHECK (holder_type = 'contact')
);
