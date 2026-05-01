package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Represents a registered user in the system, which can be either a student or a teacher.
 * Students accumulate XP and star currency through gameplay; teachers manage classrooms.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    /** In-game currency balance earned by completing stops and mysteries. */
    @Column(nullable = false)
    private int starBalance = 0;

    /** Accumulated experience points used for progression tracking. */
    @Column(nullable = false)
    private int xp = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /** Roles that determine access level and available features. */
    public enum Role { STUDENT, TEACHER }
}
