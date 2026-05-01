package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link ClassroomStudent} join entity, managing student
 * memberships and approval state within classrooms.
 */
public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long> {

    /**
     * Returns all membership records for a given classroom, regardless of status.
     */
    List<ClassroomStudent> findByClassroom_Id(Long classroomId);

    /**
     * Finds the membership record for a specific student in a specific classroom.
     *
     * @param classroomId the classroom id
     * @param studentId   the student user id
     */
    @Query("""
        select cs
        from ClassroomStudent cs
        where cs.classroom.id = :classroomId and cs.student.id = :studentId
        """)
    Optional<ClassroomStudent> findByClassroom_IdAndStudent_UserId(
        @Param("classroomId") Long classroomId,
        @Param("studentId") Long studentId
    );

    /**
     * Checks whether a student has any membership record in the given classroom.
     */
    boolean existsByClassroom_IdAndStudent_Id(Long classroomId, Long studentId);

    /**
     * Checks whether a student has a membership record with the given status in the classroom.
     */
    boolean existsByClassroom_IdAndStudent_IdAndStatus(Long classroomId, Long studentId, ClassroomStudentStatus status);

    /**
     * Finds the membership record for a student that has the given status.
     *
     * @param studentId the student user id
     * @param status    the required status
     */
    @Query("select cs from ClassroomStudent cs where cs.student.id = :studentId and cs.status = :status")
    Optional<ClassroomStudent> findByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") ClassroomStudentStatus status
    );

    /**
     * Finds the membership record for a student that does NOT have the given status.
     *
     * @param studentId the student user id
     * @param status    the status to exclude
     */
    @Query("select cs from ClassroomStudent cs where cs.student.id = :studentId and cs.status <> :status")
    Optional<ClassroomStudent> findByStudentIdAndStatusNot(
        @Param("studentId") Long studentId,
        @Param("status") ClassroomStudentStatus status
    );

    /**
     * Returns leaderboard data for all approved students in the given classroom,
     * ranked by number of completed tasks (descending).
     *
     * @param classroomId the classroom to build the leaderboard for
     * @return projected rows containing display name and completed-task count
     */
    @Query(value = """
        SELECT cs.display_name AS displayName,
               COUNT(sp.id)    AS completedTasks
        FROM classroom_students cs
        LEFT JOIN student_progress sp
            ON sp.student_id = cs.student_id
            AND sp.completed = true
        WHERE cs.classroom_id = :classroomId
          AND cs.status = 'APPROVED'
        GROUP BY cs.student_id, cs.display_name
        ORDER BY completedTasks DESC, cs.display_name ASC
        """, nativeQuery = true)
    List<LeaderboardRow> getLeaderboard(@Param("classroomId") Long classroomId);

    /**
     * Returns school-wide leaderboard data for all approved students across the given
     * classrooms, including avatar fields for visual display. Used to build
     * cross-classroom rankings within a school.
     *
     * @param classroomIds the list of classroom ids belonging to the school
     * @return projected rows with student, classroom, school, and avatar data
     */
    @Query(value = """
        SELECT cs.student_id         AS studentId,
               cs.display_name       AS displayName,
               c.id                  AS classroomId,
               c.name                AS classroomName,
               s.name                AS schoolName,
               COUNT(sp.id)          AS completedTasks,
               a.gender              AS avatarGender,
               a.eye_color           AS avatarEyeColor,
               a.eye_style           AS avatarEyeStyle,
               a.skin_color          AS avatarSkinColor,
               a.hair_color          AS avatarHairColor,
               a.hair_style          AS avatarHairStyle,
               a.outfit              AS avatarOutfit,
               a.outfit_color        AS avatarOutfitColor,
               a.hat_color           AS avatarHatColor,
               a.accessory           AS avatarAccessory
        FROM classroom_students cs
        JOIN classrooms c ON c.id = cs.classroom_id
        LEFT JOIN schools s ON s.id = c.school_id
        LEFT JOIN avatars a ON a.student_id = cs.student_id
        LEFT JOIN student_progress sp
            ON sp.student_id = cs.student_id
            AND sp.completed = true
        WHERE cs.classroom_id IN :classroomIds
          AND cs.status = 'APPROVED'
        GROUP BY cs.student_id, cs.display_name, c.id, c.name, s.name,
                 a.gender, a.eye_color, a.eye_style, a.skin_color,
                 a.hair_color, a.hair_style, a.outfit, a.outfit_color,
                 a.hat_color, a.accessory
        ORDER BY completedTasks DESC, cs.display_name ASC
        """, nativeQuery = true)
    List<SchoolLeaderboardRow> getSchoolLeaderboard(@Param("classroomIds") List<Long> classroomIds);

    /**
     * Checks whether the given student is enrolled (in any status) in any classroom
     * taught by the given teacher. Used for access-control checks in teacher-facing APIs.
     *
     * @param teacherId the teacher user id
     * @param studentId the student user id
     */
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
