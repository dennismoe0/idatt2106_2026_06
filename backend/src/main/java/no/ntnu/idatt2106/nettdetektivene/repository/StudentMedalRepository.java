package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for the {@link StudentMedal} aggregate root, tracking which medals
 * each student has earned.
 */
public interface StudentMedalRepository extends JpaRepository<StudentMedal, Long> {

    /**
     * Returns all medal records for the given student with the medal entity eagerly
     * fetched to avoid N+1 queries when rendering the medal collection.
     *
     * @param studentId the student user id
     */
    @EntityGraph(attributePaths = "medal")
    List<StudentMedal> findByStudent_Id(Long studentId);

    /**
     * Checks whether the student has already earned the given medal.
     *
     * @param studentId the student user id
     * @param medalId   the medal id
     */
    boolean existsByStudent_IdAndMedal_Id(Long studentId, Long medalId);

    /**
     * Returns the total number of medals earned by the student.
     */
    long countByStudent_Id(Long studentId);
}
