-- Make all past times time-zoned, so there's no ambiguity

ALTER TABLE attendance.client_config
  ALTER since TYPE TIMESTAMP WITH TIME ZONE USING since AT TIME ZONE 'UTC';

ALTER TABLE attendance.block_time
  ALTER since TYPE TIMESTAMP WITH TIME ZONE USING since AT TIME ZONE 'UTC';

ALTER TABLE attendance.attendance
  ALTER processed_time TYPE TIMESTAMP WITH TIME ZONE USING processed_time AT TIME ZONE 'UTC';

ALTER TABLE attendance.attendance_override
  ALTER override_time TYPE TIMESTAMP WITH TIME ZONE USING override_time AT TIME ZONE 'UTC';

ALTER TABLE attendance.processing_state
  ALTER processed_up_to TYPE TIMESTAMP WITH TIME ZONE USING processed_up_to AT TIME ZONE 'UTC';


-- Store block times as local, not UTC; hard-coded Europe/London is OK, no other time zone used at time of migration

UPDATE attendance.block_time SET
  start_time = start_time AT TIME ZONE 'UTC' AT TIME ZONE 'Europe/London',
  end_time = end_time AT TIME ZONE 'UTC' AT TIME ZONE 'Europe/London';
