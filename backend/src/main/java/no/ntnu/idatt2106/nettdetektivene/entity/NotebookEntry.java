package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

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

    public enum EntryType { AUTO_TIP, AUTO_CLUE, REFLECTION, GENERAL_NOTE }
}
