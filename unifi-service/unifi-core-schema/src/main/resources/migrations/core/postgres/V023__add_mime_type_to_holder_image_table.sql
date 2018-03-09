ALTER TABLE core.holder_image
  ADD COLUMN mime_type VARCHAR(64) NOT NULL DEFAULT 'image/jpeg';