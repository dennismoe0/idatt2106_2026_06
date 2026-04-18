ALTER TABLE users
    ADD COLUMN star_balance INT NOT NULL DEFAULT 0,
    ADD COLUMN xp           INT NOT NULL DEFAULT 0;

CREATE TABLE student_xp_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    student_id   BIGINT       NOT NULL,
    stop_id      BIGINT       NOT NULL,
    xp_amount    INT          NOT NULL,
    awarded_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_xp_log_student FOREIGN KEY (student_id) REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_xp_log_stop    FOREIGN KEY (stop_id)    REFERENCES stops(id)   ON DELETE CASCADE,
    INDEX ix_xp_log_student_stop (student_id, stop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
