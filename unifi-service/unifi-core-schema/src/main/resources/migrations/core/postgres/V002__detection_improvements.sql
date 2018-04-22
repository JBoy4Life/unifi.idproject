ALTER TABLE core.reader
  ADD COLUMN config JSONB NOT NULL DEFAULT '{}';

ALTER TABLE core.agent
  ADD COLUMN config JSONB NOT NULL DEFAULT '{}';

/* Record peak RSSI and detection count per roll-up. */
ALTER TABLE core.rfid_detection
  ADD COLUMN rssi NUMERIC(5, 2) NULL,
  ADD COLUMN count INTEGER NOT NULL DEFAULT 1;
