package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.school.CreateSchoolRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.school.JoinSchoolRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.school.SchoolClassroomSummary;
import no.ntnu.idatt2106.nettdetektivene.dto.school.SchoolResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.School;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.LeaderboardRow;
import no.ntnu.idatt2106.nettdetektivene.repository.SchoolRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.util.SchoolCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {
    private static final Logger log = LoggerFactory.getLogger(SchoolService.class);

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final TaskRepository taskRepository;
    private final SchoolCodeGenerator schoolCodeGenerator;

    @Transactional
    public SchoolResponse createSchool(Long teacherId, CreateSchoolRequest req) {
        log.info("[SchoolService] createSchool teacherId={} name={}", teacherId, req.name());
        User teacher = findTeacher(teacherId);
        if (teacher.getSchool() != null) {
            log.warn("[SchoolService] Teacher {} already belongs to school {}", teacherId, teacher.getSchool().getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher already belongs to a school");
        }
        School school = new School();
        school.setName(req.name());
        school.setJoinCode(schoolCodeGenerator.generate(schoolRepository));
        school = schoolRepository.save(school);
        teacher.setSchool(school);
        userRepository.save(teacher);
        syncTeacherClassroomsToSchool(teacherId, school);
        log.info("[SchoolService] School created: schoolId={} teacherId={} joinCode={}", school.getId(), teacherId, school.getJoinCode());
        return toSchoolResponse(school);
    }

    @Transactional
    public SchoolResponse joinSchool(Long teacherId, JoinSchoolRequest req) {
        log.info("[SchoolService] joinSchool teacherId={} code={}", teacherId, req.code());
        User teacher = findTeacher(teacherId);
        if (teacher.getSchool() != null) {
            log.warn("[SchoolService] Teacher {} already belongs to school {} — cannot join another", teacherId, teacher.getSchool().getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher already belongs to a school");
        }
        School school = schoolRepository.findByJoinCode(req.code())
            .orElseThrow(() -> {
                log.warn("[SchoolService] Invalid school code: {} teacherId={}", req.code(), teacherId);
                return new ResourceNotFoundException("School not found");
            });
        teacher.setSchool(school);
        userRepository.save(teacher);
        syncTeacherClassroomsToSchool(teacherId, school);
        log.info("[SchoolService] Teacher {} joined school {}", teacherId, school.getId());
        return toSchoolResponse(school);
    }

    @Transactional(readOnly = true)
    public SchoolResponse getMySchool(Long teacherId) {
        log.info("[SchoolService] getMySchool teacherId={}", teacherId);
        User teacher = findTeacher(teacherId);
        if (teacher.getSchool() == null) {
            log.warn("[SchoolService] Teacher {} has no school", teacherId);
            throw new ResourceNotFoundException("Teacher does not belong to a school");
        }
        return toSchoolResponse(teacher.getSchool());
    }

    @Transactional(readOnly = true)
    public List<SchoolClassroomSummary> getSchoolClassrooms(Long teacherId) {
        log.info("[SchoolService] getSchoolClassrooms teacherId={}", teacherId);
        User teacher = findTeacher(teacherId);
        if (teacher.getSchool() == null) {
            log.warn("[SchoolService] Teacher {} has no school — returning empty list", teacherId);
            return List.of();
        }
        Long schoolId = teacher.getSchool().getId();
        int totalTasks = Math.toIntExact(taskRepository.count());
        return classroomRepository.findBySchool_Id(schoolId).stream()
            .map(classroom -> toSchoolClassroomSummary(classroom, totalTasks))
            .toList();
    }

    private SchoolClassroomSummary toSchoolClassroomSummary(Classroom classroom, int totalTasks) {
        List<LeaderboardRow> rows = classroomStudentRepository.getLeaderboard(classroom.getId());
        int studentCount = rows.size();
        List<LeaderboardEntryDto> top5 = rows.stream()
            .limit(5)
            .map(row -> new LeaderboardEntryDto(row.getDisplayName(), row.getCompletedTasks().intValue(), totalTasks))
            .toList();
        log.info("[SchoolService] Classroom {} — {} students, {} in top5", classroom.getId(), studentCount, top5.size());
        return new SchoolClassroomSummary(classroom.getId(), classroom.getName(), classroom.getDescription(), studentCount, top5);
    }

    private User findTeacher(Long teacherId) {
        return userRepository.findById(teacherId)
            .orElseThrow(() -> {
                log.warn("[SchoolService] Teacher not found: {}", teacherId);
                return new ResourceNotFoundException("User not found");
            });
    }

    private void syncTeacherClassroomsToSchool(Long teacherId, School school) {
        List<Classroom> classrooms = classroomRepository.findByTeachers_Teacher_UserId(teacherId);
        for (Classroom classroom : classrooms) {
            if (school.equals(classroom.getSchool())) {
                continue;
            }
            classroom.setSchool(school);
        }
        classroomRepository.saveAll(classrooms);
        log.info(
            "[SchoolService] Synced {} classrooms to school {} for teacher {}",
            classrooms.size(),
            school.getId(),
            teacherId
        );
    }

    private SchoolResponse toSchoolResponse(School school) {
        return new SchoolResponse(school.getId(), school.getName(), school.getJoinCode());
    }
}
