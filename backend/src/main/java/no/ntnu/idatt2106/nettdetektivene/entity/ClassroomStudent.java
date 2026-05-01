package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Join table that tracks the membership of a student in a classroom.
 * The {@code status} field progresses from PENDING (awaiting teacher approval)
 * to APPROVED or KICKED. A student may only appear once per classroom.
 */
@Entity
@Table(name = "classroom_students",
       uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ClassroomStudent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassroomStudentStatus status = ClassroomStudentStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
