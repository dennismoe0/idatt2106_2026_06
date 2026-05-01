package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Records a single avatar cosmetic option that a student has unlocked, either
 * by earning a medal ({@code Source.MEDAL}) or by purchasing it with stars
 * ({@code Source.PURCHASE}). The {@code optionType} and {@code optionValue}
 * together identify the specific cosmetic (e.g. type="hairColor", value="#FF0099").
 */
@Entity
@Table(name = "unlocked_avatar_options")
@Getter @Setter @NoArgsConstructor
public class UnlockedAvatarOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "option_type", nullable = false, length = 50)
    private String optionType;

    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Column(name = "stop_id")
    private Long stopId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Indicates how the cosmetic option was unlocked. */
    public enum Source { MEDAL, PURCHASE }
}
