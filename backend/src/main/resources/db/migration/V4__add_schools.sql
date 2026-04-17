-- V4__add_schools.sql
-- NOTE: When merging after feature/notatblokk (V4,V5) and feature/medaljer-og-dekobling (V6),
-- rename this file to V7__add_schools.sql to avoid version conflicts.

CREATE TABLE schools (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(200) NOT NULL,
    join_code  VARCHAR(20)  NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY schools_join_code_uq (join_code)
);

ALTER TABLE users
    ADD COLUMN school_id BIGINT NULL,
    ADD CONSTRAINT users_school_fk FOREIGN KEY (school_id) REFERENCES schools (id);

ALTER TABLE classrooms
    ADD COLUMN school_id BIGINT NULL,
    ADD CONSTRAINT classrooms_school_fk FOREIGN KEY (school_id) REFERENCES schools (id);
