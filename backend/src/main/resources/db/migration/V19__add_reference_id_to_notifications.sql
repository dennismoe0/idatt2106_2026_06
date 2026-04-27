ALTER TABLE notifications
    ADD COLUMN reference_id BIGINT NULL;

CREATE INDEX idx_notifications_teacher_read_created
    ON notifications(teacher_id, is_read, created_at);

CREATE INDEX idx_notifications_classroom_id
    ON notifications(classroom_id);
