package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByJoinCode(String joinCode);

    @Query("""
        select ct.classroom
        from ClassroomTeacher ct
        where ct.teacher.id = :teacherId
        """)
    List<Classroom> findByTeachers_Teacher_UserId(@Param("teacherId") Long teacherId);

    boolean existsByJoinCode(String joinCode);

    java.util.List<no.ntnu.idatt2106.nettdetektivene.entity.Classroom> findBySchool_Id(Long schoolId);
}
