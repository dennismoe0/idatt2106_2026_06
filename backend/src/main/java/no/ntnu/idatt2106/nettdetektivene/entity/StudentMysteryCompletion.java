package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Records a student's attempt at the weekly mystery challenge within a classroom.
 * A student may only submit one answer per mystery. The {@code correct} flag
 * indicates whether the submitted answer matched the expected answer.
 */
@Entity
@Table(name = "student_mystery_completions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "mystery_id"}))
@Getter @Setter @NoArgsConstructor
public class StudentMysteryCompletion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mystery_id", nullable = false)
    private WeeklyMystery mystery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    private LocalDateTime completedAt = LocalDateTime.now();
}
