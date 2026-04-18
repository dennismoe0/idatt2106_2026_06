package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long> {
    List<ClassroomStudent> findByClassroom_Id(Long classroomId);

    @Query("""
        select cs
        from ClassroomStudent cs
        where cs.classroom.id = :classroomId and cs.student.id = :studentId
        """)
    Optional<ClassroomStudent> findByClassroom_IdAndStudent_UserId(
        @Param("classroomId") Long classroomId,
        @Param("studentId") Long studentId
    );

    boolean existsByClassroom_IdAndStudent_Id(Long classroomId, Long studentId);

    @Query("select cs from ClassroomStudent cs where cs.student.id = :studentId and cs.status = :status")
    Optional<ClassroomStudent> findByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") ClassroomStudentStatus status
    );

    @Query(value = """
        SELECT cs.display_name AS displayName,
               COUNT(sp.id)    AS completedTasks
        FROM classroom_students cs
        LEFT JOIN student_progress sp
            ON sp.student_id = cs.student_id
            AND sp.completed = 1
        WHERE cs.classroom_id = :classroomId
          AND cs.status = 'APPROVED'
        GROUP BY cs.student_id, cs.display_name
        ORDER BY completedTasks DESC, cs.display_name ASC
        """, nativeQuery = true)
    List<LeaderboardRow> getLeaderboard(@Param("classroomId") Long classroomId);

    @Query("""
        select count(cs) > 0
        from ClassroomStudent cs
        join ClassroomTeacher ct on ct.classroom.id = cs.classroom.id
        where ct.teacher.id = :teacherId and cs.student.id = :studentId
        """)
    boolean existsStudentInTeacherClassroom(
        @Param("teacherId") Long teacherId,
        @Param("studentId") Long studentId
    );
}
