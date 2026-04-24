package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private boolean correct = false;

    private LocalDateTime completedAt = LocalDateTime.now();
}
