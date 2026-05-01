package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Join table that links a teacher to a classroom they own or co-teach.
 * A teacher may be associated with multiple classrooms; each
 * classroom–teacher pair is unique.
 */
@Entity
@Table(name = "classroom_teachers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "teacher_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ClassroomTeacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
}
