package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents a single interactive task inside a stop. The {@code taskType} enum
 * tells the frontend which task component to render, and {@code contentJson}
 * holds all type-specific content (questions, options, images, etc.) as a JSON
 * blob. The optional {@code classroom} link identifies teacher-created custom
 * tasks scoped to a specific classroom.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer difficulty = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private TaskType taskType;

    @Column(nullable = false, columnDefinition = "JSON")
    private String contentJson;

    @Column(name = "correct_answer_json", columnDefinition = "JSON")
    private String correctAnswerJson;

    @Column(columnDefinition = "TEXT")
    private String guidanceText;

    @Column(nullable = false)
    private Integer orderIndex = 1;
}
