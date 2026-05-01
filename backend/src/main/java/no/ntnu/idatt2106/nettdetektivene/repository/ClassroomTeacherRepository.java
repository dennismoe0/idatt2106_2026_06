package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import no.ntnu.idatt2106.nettdetektivene.entity.User;

/**
 * Repository for the {@link ClassroomTeacher} join entity, managing teacher
 * ownership and co-teaching relationships for classrooms.
 */
public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, Long> {

    /**
     * Checks whether the given teacher is associated with the given classroom.
     *
     * @param classroomId the classroom id
     * @param teacherId   the teacher user id
     */
    @Query("""
        select count(ct) > 0
        from ClassroomTeacher ct
        where ct.classroom.id = :classroomId and ct.teacher.id = :teacherId
        """)
    boolean existsByClassroom_IdAndTeacher_UserId(
        @Param("classroomId") Long classroomId,
        @Param("teacherId") Long teacherId
    );

    /**
     * Resolves the school for a classroom by looking up the school of its first
     * teacher that has a non-null school association.
     *
     * @param classroomId the classroom id
     * @return the school, or empty if no teacher in this classroom has a school
     */
    @Query("""
        select ct.teacher.school
        from ClassroomTeacher ct
        where ct.classroom.id = :classroomId
          and ct.teacher.school is not null
        """)
    Optional<School> findSchoolByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * Returns all teacher users associated with the given classroom.
     *
     * @param classroomId the classroom id
     */
    @Query("""
        select ct.teacher
        from ClassroomTeacher ct
        where ct.classroom.id = :classroomId
        """)
    List<User> findTeachersByClassroomId(@Param("classroomId") Long classroomId);
}
