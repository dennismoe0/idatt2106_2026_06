package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Source { medal, purchase }
}
