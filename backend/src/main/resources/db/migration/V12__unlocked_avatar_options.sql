CREATE TABLE unlocked_avatar_options (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id   BIGINT       NOT NULL,
  option_type  VARCHAR(50)  NOT NULL,
  option_value VARCHAR(100) NOT NULL,
  source       ENUM('MEDAL','PURCHASE') NOT NULL,
  stop_id      BIGINT       NULL,
  created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_uao_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_uao_stop    FOREIGN KEY (stop_id)    REFERENCES stops(id)  ON DELETE SET NULL,
  CONSTRAINT uq_student_option UNIQUE (student_id, option_type, option_value)
);
