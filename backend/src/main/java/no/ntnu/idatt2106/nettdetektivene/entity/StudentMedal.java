package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_medals",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "medal_id"}))
@Getter
@Setter
@NoArgsConstructor
public class StudentMedal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "medal_id", nullable = false)
    private Medal medal;

    @CreationTimestamp
    private LocalDateTime earnedAt;
}
