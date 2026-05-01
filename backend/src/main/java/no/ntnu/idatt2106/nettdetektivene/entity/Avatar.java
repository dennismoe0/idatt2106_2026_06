package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents the visual appearance (paper-doll avatar) of a student.
 * Each student has exactly one avatar. Fields correspond to customisable
 * SVG layers rendered on the frontend.
 */
@Entity
@Table(name = "avatars")
@Getter
@Setter
@NoArgsConstructor
public class Avatar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true, nullable = false)
    private User student;

    private String gender;
    private String eyeColor;
    private String eyeStyle;
    private String skinColor;
    private String hairColor;
    private String hairStyle;
    private String outfit;
    private String outfitColor;
    private String hatColor;
    private String accessory;
}
