/* Antennae are transient. */
ALTER TABLE core.rfid_detection DROP CONSTRAINT fk_rfid_detection_to_antenna;
