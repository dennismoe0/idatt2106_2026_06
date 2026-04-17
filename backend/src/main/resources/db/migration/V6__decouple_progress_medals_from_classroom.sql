-- Progress and medals belong to the student, not the classroom.
-- Dropping classroom_id lets students carry their progress across classroom changes.

ALTER TABLE student_progress DROP FOREIGN KEY student_progress_ibfk_2;
ALTER TABLE student_progress DROP INDEX student_id_2;
ALTER TABLE student_progress DROP COLUMN classroom_id;
ALTER TABLE student_progress ADD UNIQUE (student_id, task_id);

ALTER TABLE student_medals DROP FOREIGN KEY student_medals_ibfk_3;
ALTER TABLE student_medals DROP INDEX student_id_2;
ALTER TABLE student_medals DROP COLUMN classroom_id;
ALTER TABLE student_medals ADD UNIQUE (student_id, medal_id);
