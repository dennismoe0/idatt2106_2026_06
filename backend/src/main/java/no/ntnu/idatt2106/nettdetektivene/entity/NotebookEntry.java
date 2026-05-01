package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * A single entry in a student's in-game notebook.
 * Entries are either auto-generated (tips and clues produced by the system when
 * a student completes a task) or manually created by the student (reflections and
 * general notes). The optional {@code stop} link groups stop-specific entries.
 */
@Entity
@Table(name = "notebook_entries")
@Getter
@Setter
@NoArgsConstructor
public class NotebookEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = true)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "stop_id", nullable = true)
    private Stop stop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private EntryType entryType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /** Distinguishes system-generated entries from student-authored ones. */
    public enum EntryType { AUTO_TIP, AUTO_CLUE, REFLECTION, GENERAL_NOTE }
}
