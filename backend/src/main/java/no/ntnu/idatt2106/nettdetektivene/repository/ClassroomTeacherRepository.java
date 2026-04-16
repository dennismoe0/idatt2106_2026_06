package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
