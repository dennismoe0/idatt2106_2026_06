package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents one of the seven themed investigation stops on the game map
 * (e.g. Nyhetskvartalet, Postkontoret). Stops are unlocked sequentially
 * according to {@code orderIndex}. The final stop is marked with
 * {@code isFinalBoss = true} and uses a mixed-task format.
 */
@Entity
@Table(name = "stops")
@Getter
@Setter
@NoArgsConstructor
public class Stop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String theme;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean isFinalBoss = false;

    @Column(columnDefinition = "TEXT")
    private String autoTip;

    @Column(columnDefinition = "TEXT")
    private String clueText;
}
