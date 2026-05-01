package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link Classroom} aggregate root.
 */
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    /**
     * Finds a classroom by its unique join code.
     *
     * @param joinCode the short human-readable code students use to join
     * @return the matching classroom, or empty if not found
     */
    Optional<Classroom> findByJoinCode(String joinCode);

    /**
     * Returns all active classrooms that the given teacher is associated with.
     *
     * @param teacherId the id of the teacher
     */
    @Query("""
        select ct.classroom
        from ClassroomTeacher ct
        where ct.teacher.id = :teacherId
          and ct.classroom.isActive = true
        """)
    List<Classroom> findByTeachers_Teacher_UserId(@Param("teacherId") Long teacherId);

    /**
     * Checks whether a classroom with the given join code already exists.
     */
    boolean existsByJoinCode(String joinCode);

    /**
     * Returns all classrooms that belong to the given school.
     */
    java.util.List<no.ntnu.idatt2106.nettdetektivene.entity.Classroom> findBySchool_Id(Long schoolId);

    /**
     * Returns all currently active classrooms.
     */
    List<Classroom> findByIsActiveTrue();

    /**
     * Checks whether the given teacher is associated with the given classroom.
     *
     * @param classroomId the classroom to check
     * @param teacherId   the teacher to check
     * @return {@code true} if the teacher owns or co-teaches this classroom
     */
    @Query("select count(ct) > 0 from ClassroomTeacher ct where ct.classroom.id = :classroomId and ct.teacher.id = :teacherId")
    boolean isTeacherOfClassroom(@Param("classroomId") Long classroomId, @Param("teacherId") Long teacherId);
}
