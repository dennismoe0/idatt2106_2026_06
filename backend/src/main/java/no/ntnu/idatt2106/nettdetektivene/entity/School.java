package no.ntnu.idatt2106.nettdetektivene.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schools")
@Getter @Setter @NoArgsConstructor
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String joinCode;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
