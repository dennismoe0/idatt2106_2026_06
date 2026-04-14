package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "classroom_id", "task_id"}))
@Getter
@Setter
@NoArgsConstructor
public class StudentProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private boolean completed = false;
    @Column(nullable = false)
    private int score = 0;
    @Column(nullable = false)
    private int attempts = 0;
    private LocalDateTime completedAt;
}
