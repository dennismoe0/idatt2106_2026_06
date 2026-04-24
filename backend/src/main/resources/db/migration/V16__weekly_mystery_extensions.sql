-- Extend weekly_mysteries for teacher-curated mysteries
ALTER TABLE weekly_mysteries
  ADD COLUMN mystery_type  VARCHAR(30)  DEFAULT 'REAL_OR_FAKE' NOT NULL,
  ADD COLUMN question_text TEXT,
  ADD COLUMN correct_answer VARCHAR(20),
  ADD COLUMN teacher_comment TEXT,
  ADD COLUMN reward_stars   INT DEFAULT 5  NOT NULL,
  ADD COLUMN reward_xp      INT DEFAULT 50 NOT NULL;

-- Track which students have completed which mysteries
CREATE TABLE student_mystery_completions (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id   BIGINT NOT NULL,
  mystery_id   BIGINT NOT NULL,
  classroom_id BIGINT NOT NULL,
  is_correct   BOOLEAN NOT NULL DEFAULT FALSE,
  completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id)   REFERENCES users(id)            ON DELETE CASCADE,
  FOREIGN KEY (mystery_id)   REFERENCES weekly_mysteries(id) ON DELETE CASCADE,
  FOREIGN KEY (classroom_id) REFERENCES classrooms(id)       ON DELETE CASCADE,
  UNIQUE (student_id, mystery_id)
);

CREATE INDEX idx_mystery_completions_student ON student_mystery_completions(student_id);
CREATE INDEX idx_mystery_completions_mystery ON student_mystery_completions(mystery_id);
