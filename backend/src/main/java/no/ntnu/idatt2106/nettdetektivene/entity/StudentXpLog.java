package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Audit log of XP awarded to a student for completing a stop.
 * One row is written per stop-completion event, enabling the service layer
 * to detect and prevent duplicate XP grants.
 */
@Entity
@Table(name = "student_xp_log")
@Getter
@Setter
@NoArgsConstructor
public class StudentXpLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(nullable = false)
    private int xpAmount;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime awardedAt;
}
