CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('STUDENT','TEACHER') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE classrooms (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    join_code   VARCHAR(20) UNIQUE NOT NULL,
    is_active   BOOLEAN DEFAULT TRUE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE classroom_teachers (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    teacher_id   BIGINT NOT NULL,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id)   REFERENCES users(id)      ON DELETE CASCADE,
    UNIQUE (classroom_id, teacher_id)
);

CREATE TABLE classroom_students (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    student_id   BIGINT NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    status       ENUM('PENDING','APPROVED','KICKED') DEFAULT 'PENDING' NOT NULL,
    joined_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)   REFERENCES users(id)      ON DELETE CASCADE,
    UNIQUE (classroom_id, student_id)
);

CREATE TABLE avatars (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT UNIQUE NOT NULL,
    gender       VARCHAR(20),
    eye_color    VARCHAR(20),
    skin_color   VARCHAR(20),
    hair_color   VARCHAR(20),
    hair_style   VARCHAR(30),
    outfit       VARCHAR(30),
    outfit_color VARCHAR(20),
    hat_color    VARCHAR(20),
    accessory    VARCHAR(30),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE stops (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    theme         VARCHAR(50) NOT NULL,
    order_index   INT NOT NULL,
    is_final_boss BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE tasks (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    stop_id       BIGINT NOT NULL,
    classroom_id  BIGINT,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    difficulty    INT NOT NULL DEFAULT 1,
    task_type     VARCHAR(50) NOT NULL COLLATE utf8mb4_general_ci,
    content_json  JSON NOT NULL,
    guidance_text TEXT,
    order_index   INT NOT NULL DEFAULT 1,
    FOREIGN KEY (stop_id)      REFERENCES stops(id)      ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE SET NULL
);

CREATE TABLE student_progress (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    stop_id      BIGINT NOT NULL,
    task_id      BIGINT NOT NULL,
    completed    BOOLEAN DEFAULT FALSE NOT NULL,
    score        INT DEFAULT 0 NOT NULL,
    attempts     INT DEFAULT 0 NOT NULL,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (student_id)   REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (stop_id)      REFERENCES stops(id)      ON DELETE CASCADE,
    FOREIGN KEY (task_id)      REFERENCES tasks(id)      ON DELETE CASCADE,
    UNIQUE (student_id, classroom_id, task_id)
);

CREATE TABLE medals (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    stop_id     BIGINT,
    image_url   VARCHAR(255),
    FOREIGN KEY (stop_id) REFERENCES stops(id) ON DELETE SET NULL
);

CREATE TABLE student_medals (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    medal_id     BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    earned_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id)   REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (medal_id)     REFERENCES medals(id)     ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    UNIQUE (student_id, medal_id, classroom_id)
);

CREATE TABLE notebook_entries (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    stop_id      BIGINT NOT NULL,
    entry_type   ENUM('AUTO_TIP','REFLECTION') NOT NULL,
    content      TEXT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id)   REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (stop_id)      REFERENCES stops(id)      ON DELETE CASCADE
);

CREATE TABLE notifications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id   BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    type         VARCHAR(50) NOT NULL,
    message      TEXT NOT NULL,
    is_read      BOOLEAN DEFAULT FALSE NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id)   REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE
);

CREATE TABLE weekly_mysteries (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    student_id   BIGINT NOT NULL,
    title        VARCHAR(200),
    description  TEXT,
    image_url    VARCHAR(500),
    status       ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING' NOT NULL,
    featured     BOOLEAN DEFAULT FALSE NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)   REFERENCES users(id)      ON DELETE CASCADE
);

CREATE TABLE word_filter (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    word         VARCHAR(100) NOT NULL,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE
);

-- Performance indexes for common queries
CREATE INDEX idx_student_progress_student_id ON student_progress(student_id);
CREATE INDEX idx_tasks_stop_id ON tasks(stop_id);
CREATE INDEX idx_notifications_teacher_id ON notifications(teacher_id);
CREATE INDEX idx_weekly_mysteries_classroom_student ON weekly_mysteries(classroom_id, student_id);
