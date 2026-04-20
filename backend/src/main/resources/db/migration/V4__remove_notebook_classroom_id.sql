-- Notebooks are per-user. classroom_id FK name is MySQL auto-generated:
-- notebook_entries_ibfk_2 (second FK defined in V1 CREATE TABLE).
-- If this fails, look up the name:
--   SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
--   WHERE TABLE_NAME = 'notebook_entries' AND COLUMN_NAME = 'classroom_id';
ALTER TABLE notebook_entries DROP FOREIGN KEY notebook_entries_ibfk_2;
ALTER TABLE notebook_entries DROP COLUMN classroom_id;
