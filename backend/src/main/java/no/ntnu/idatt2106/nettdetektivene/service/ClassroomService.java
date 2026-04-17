package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.ClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentStatusResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.util.ClassroomCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    private static final Logger log = LoggerFactory.getLogger(ClassroomService.class);

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final UserRepository userRepository;
    private final ClassroomCodeGenerator classroomCodeGenerator;
    private final TaskRepository taskRepository;

    @Transactional
    public ClassroomResponse createClassroom(Long teacherId, CreateClassroomRequest req) {
        log.info("[ClassroomService] createClassroom teacherId={} name={}", teacherId, req.name());
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> {
                log.warn("[ClassroomService] Teacher not found: {}", teacherId);
                return new ResourceNotFoundException("User not found");
            });

        Classroom classroom = new Classroom();
        classroom.setName(req.name());
        classroom.setDescription(req.description());
        classroom.setJoinCode(classroomCodeGenerator.generate(classroomRepository));
        classroom.setSchool(teacher.getSchool()); // attach school if teacher has one (null is fine)
        classroom = classroomRepository.save(classroom);

        ClassroomTeacher classroomTeacher = new ClassroomTeacher();
        classroomTeacher.setTeacher(teacher);
        classroomTeacher.setClassroom(classroom);
        classroomTeacherRepository.save(classroomTeacher);

        log.info("[ClassroomService] Classroom created: classroomId={} teacherId={} joinCode={} schoolId={}",
            classroom.getId(), teacherId, classroom.getJoinCode(),
            teacher.getSchool() != null ? teacher.getSchool().getId() : null);
        return toClassroomResponse(classroom);
    }

    public List<ClassroomResponse> getMyClassrooms(Long teacherId) {
        log.info("Fetching classrooms for teacherId={}", teacherId);
        return classroomRepository.findByTeachers_Teacher_UserId(teacherId).stream()
            .map(this::toClassroomResponse)
            .toList();
    }

    public ClassroomResponse getClassroom(Long teacherId, Long classroomId) {
        log.info("Fetching classroom: classroomId={} teacherId={}", classroomId, teacherId);
        Classroom classroom = getClassroomForTeacher(teacherId, classroomId);
        return toClassroomResponse(classroom);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(Long teacherId, Long classroomId) {
        log.info("[ClassroomService] getLeaderboard teacherId={} classroomId={}", teacherId, classroomId);
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        int totalTasks = (int) taskRepository.count();
        List<LeaderboardEntryDto> entries = classroomStudentRepository.getLeaderboard(classroomId).stream()
            .map(row -> new LeaderboardEntryDto(row.getDisplayName(), row.getCompletedTasks().intValue(), totalTasks))
            .toList();
        log.info("[ClassroomService] Leaderboard fetched: classroomId={} entries={}", classroomId, entries.size());
        return entries;
    }

    public StudentInClassroomResponse joinClassroom(Long studentId, JoinClassroomRequest req) {
        Classroom classroom = classroomRepository.findByJoinCode(req.code())
            .orElseThrow(() -> {
                log.warn("Classroom join failed: invalid join code, studentId={} code={}", studentId, req.code());
                return new ResourceNotFoundException("Classroom not found");
            });
        User student = userRepository.getReferenceById(studentId);

        classroomStudentRepository.findByClassroom_IdAndStudent_UserId(classroom.getId(), studentId)
            .ifPresent(existing -> {
                if (existing.getStatus() == ClassroomStudentStatus.KICKED) {
                    log.warn("Classroom join blocked: kicked student tried to rejoin, classroomId={} studentId={}",
                        classroom.getId(), studentId);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student cannot rejoin this classroom");
                }
                log.warn("Classroom join blocked: student already member, classroomId={} studentId={} status={}",
                    classroom.getId(), studentId, existing.getStatus());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is already a member");
            });

        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setClassroom(classroom);
        classroomStudent.setStudent(student);
        classroomStudent.setDisplayName(req.displayName());
        classroomStudent.setStatus(ClassroomStudentStatus.PENDING);
        classroomStudent = classroomStudentRepository.save(classroomStudent);

        log.info("Student joined classroom: classroomId={} studentId={} status={}",
            classroom.getId(), studentId, classroomStudent.getStatus());
        return toStudentResponse(classroomStudent);
    }

    public List<StudentInClassroomResponse> getStudents(Long teacherId, Long classroomId) {
        log.info("Fetching students for classroomId={} teacherId={}", classroomId, teacherId);
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        return classroomStudentRepository.findByClassroom_Id(classroomId).stream()
            .map(this::toStudentResponse)
            .toList();
    }

    public StudentInClassroomResponse updateStudentStatus(
        Long teacherId,
        Long classroomId,
        Long studentId,
        ClassroomStudentStatus status
    ) {
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        if (status == ClassroomStudentStatus.PENDING) {
            log.warn("Student status update failed: invalid status, classroomId={} studentId={} teacherId={} status={}",
                classroomId, studentId, teacherId, status);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be APPROVED or KICKED");
        }
        ClassroomStudent classroomStudent = classroomStudentRepository
            .findByClassroom_IdAndStudent_UserId(classroomId, studentId)
            .orElseThrow(() -> {
                log.warn("Student status update failed: membership not found, classroomId={} studentId={} teacherId={}",
                    classroomId, studentId, teacherId);
                return new ResourceNotFoundException("Student not found in classroom");
            });

        classroomStudent.setStatus(status);
        classroomStudent = classroomStudentRepository.save(classroomStudent);

        log.info("Student status updated: classroomId={} studentId={} teacherId={} status={}",
            classroomId, studentId, teacherId, status);
        return toStudentResponse(classroomStudent);
    }

    public StudentStatusResponse getMyStatus(Long studentId, Long classroomId) {
        log.info("[ClassroomService] getMyStatus studentId={} classroomId={}", studentId, classroomId);
        ClassroomStudent entry = classroomStudentRepository
            .findByClassroom_IdAndStudent_UserId(classroomId, studentId)
            .orElseThrow(() -> {
                log.warn("[ClassroomService] Student {} not in classroom {}", studentId, classroomId);
                return new ResourceNotFoundException("Student not in classroom");
            });
        log.info("[ClassroomService] Student {} status: {}", studentId, entry.getStatus());
        return new StudentStatusResponse(entry.getStatus().name());
    }

    private Classroom getClassroomForTeacher(Long teacherId, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> {
                log.warn("Classroom fetch failed: classroom not found, classroomId={} teacherId={}",
                    classroomId, teacherId);
                return new ResourceNotFoundException("Classroom not found");
            });
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        return classroom;
    }

    private void verifyTeacherOwnsClassroom(Long teacherId, Long classroomId) {
        if (!classroomRepository.existsById(classroomId)
            || !classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(classroomId, teacherId)) {
            log.warn("Classroom access denied: classroomId={} teacherId={}", classroomId, teacherId);
            throw new ResourceNotFoundException("Classroom not found");
        }
    }

    private ClassroomResponse toClassroomResponse(Classroom classroom) {
        return new ClassroomResponse(
            classroom.getId(),
            classroom.getName(),
            classroom.getJoinCode(),
            classroom.getDescription(),
            classroom.getCreatedAt()
        );
    }

    private StudentInClassroomResponse toStudentResponse(ClassroomStudent classroomStudent) {
        return new StudentInClassroomResponse(
            classroomStudent.getStudent().getId(),
            classroomStudent.getClassroom().getId(),
            classroomStudent.getDisplayName(),
            classroomStudent.getStatus().name()
        );
    }
}
