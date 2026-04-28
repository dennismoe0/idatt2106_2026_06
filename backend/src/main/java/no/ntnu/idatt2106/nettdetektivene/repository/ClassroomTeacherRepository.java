package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import no.ntnu.idatt2106.nettdetektivene.entity.User;

public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, Long> {
    @Query("""
        select count(ct) > 0
        from ClassroomTeacher ct
        where ct.classroom.id = :classroomId and ct.teacher.id = :teacherId
        """)
    boolean existsByClassroom_IdAndTeacher_UserId(
        @Param("classroomId") Long classroomId,
        @Param("teacherId") Long teacherId
    );

    @Query("""
        select ct.teacher.school
        from ClassroomTeacher ct
        where ct.classroom.id = :classroomId
          and ct.teacher.school is not null
        """)
    Optional<School> findSchoolByClassroomId(@Param("classroomId") Long classroomId);

    @Query("""
        select ct.teacher
        from ClassroomTeacher ct
        where ct.classroom.id = :classroomId
        """)
    List<User> findTeachersByClassroomId(@Param("classroomId") Long classroomId);
}
