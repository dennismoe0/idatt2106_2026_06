package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_mysteries")
@Getter @Setter @NoArgsConstructor
public class WeeklyMystery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(length = 30, nullable = false)
    private String mysteryType = "REAL_OR_FAKE";

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(length = 20)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String teacherComment;

    @Column(nullable = false)
    private int rewardStars = 5;

    @Column(nullable = false)
    private int rewardXp = 50;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Status { PENDING, APPROVED, REJECTED }
}
