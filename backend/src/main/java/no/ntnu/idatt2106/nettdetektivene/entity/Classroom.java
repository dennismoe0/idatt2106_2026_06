package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Represents a classroom created by a teacher. Students join a classroom via a
 * short {@code joinCode} and are approved or kicked by the owning teacher.
 * A classroom may belong to a school and optionally mutes background music for all students.
 */
@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
public class Classroom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, nullable = false, length = 20)
    private String joinCode;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean musicMuted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
