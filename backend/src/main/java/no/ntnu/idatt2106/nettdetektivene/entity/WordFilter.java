package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * A single word or phrase that the teacher has blocked for a specific classroom.
 * Blocked words are checked against student-submitted free-text content
 * (e.g. display names, mystery submissions) before they are saved.
 */
@Entity
@Table(name = "word_filter")
@Getter
@Setter
@NoArgsConstructor
public class WordFilter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(nullable = false, length = 100)
    private String word;
}
