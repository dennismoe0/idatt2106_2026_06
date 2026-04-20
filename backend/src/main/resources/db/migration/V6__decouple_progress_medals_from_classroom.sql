-- student_progress: remove classroom_id, tighten unique constraint to (student_id, task_id)
ALTER TABLE student_progress DROP FOREIGN KEY student_progress_ibfk_2;
ALTER TABLE student_progress DROP FOREIGN KEY student_progress_ibfk_1;
ALTER TABLE student_progress DROP INDEX student_id;
ALTER TABLE student_progress DROP COLUMN classroom_id;
ALTER TABLE student_progress ADD UNIQUE (student_id, task_id);
ALTER TABLE student_progress ADD CONSTRAINT student_progress_ibfk_1 FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

-- student_medals: remove classroom_id, tighten unique constraint to (student_id, medal_id)
ALTER TABLE student_medals DROP FOREIGN KEY student_medals_ibfk_3;
ALTER TABLE student_medals DROP FOREIGN KEY student_medals_ibfk_1;
ALTER TABLE student_medals DROP INDEX student_id;
ALTER TABLE student_medals DROP COLUMN classroom_id;
ALTER TABLE student_medals ADD UNIQUE (student_id, medal_id);
ALTER TABLE student_medals ADD CONSTRAINT student_medals_ibfk_1 FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;
