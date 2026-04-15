package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.ClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.util.ClassroomCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public ClassroomResponse createClassroom(Long teacherId, CreateClassroomRequest req) {
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> {
                log.warn("Classroom creation failed: teacher not found, teacherId={}", teacherId);
                return new ResourceNotFoundException("Teacher not found");
            });

        Classroom classroom = new Classroom();
        classroom.setTitle(req.name());
        classroom.setDescription(req.description());
        classroom.setJoinCode(classroomCodeGenerator.generate(classroomRepository));
        classroom = classroomRepository.save(classroom);

        ClassroomTeacher classroomTeacher = new ClassroomTeacher();
        classroomTeacher.setTeacher(teacher);
        classroomTeacher.setClassroom(classroom);
        classroomTeacherRepository.save(classroomTeacher);

        log.info("Classroom created: classroomId={} teacherId={} joinCode={}",
            classroom.getId(), teacherId, classroom.getJoinCode());
        return toClassroomResponse(classroom);
    }

    public List<ClassroomResponse> getMyClassrooms(Long teacherId) {
        return classroomRepository.findByTeachers_Teacher_UserId(teacherId).stream()
            .map(this::toClassroomResponse)
            .toList();
    }

    public ClassroomResponse getClassroom(Long teacherId, Long classroomId) {
        Classroom classroom = getClassroomForTeacher(teacherId, classroomId);
        return toClassroomResponse(classroom);
    }

    public StudentInClassroomResponse joinClassroom(Long studentId, JoinClassroomRequest req) {
        Classroom classroom = classroomRepository.findByJoinCode(req.code())
            .orElseThrow(() -> {
                log.warn("Classroom join failed: invalid join code, studentId={} code={}", studentId, req.code());
                return new ResourceNotFoundException("Classroom not found");
            });
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> {
                log.warn("Classroom join failed: student not found, studentId={}", studentId);
                return new ResourceNotFoundException("Student not found");
            });

        classroomStudentRepository.findByClassroom_IdAndStudent_UserId(classroom.getId(), studentId)
            .ifPresent(existing -> {
                if (existing.getStatus() == ClassroomStudent.Status.KICKED) {
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
        classroomStudent.setStatus(ClassroomStudent.Status.PENDING);
        classroomStudent = classroomStudentRepository.save(classroomStudent);

        log.info("Student joined classroom: classroomId={} studentId={} status={}",
            classroom.getId(), studentId, classroomStudent.getStatus());
        return toStudentResponse(classroomStudent);
    }

    public List<StudentInClassroomResponse> getStudents(Long teacherId, Long classroomId) {
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        return classroomStudentRepository.findByClassroom_Id(classroomId).stream()
            .map(this::toStudentResponse)
            .toList();
    }

    public StudentInClassroomResponse updateStudentStatus(
        Long teacherId,
        Long classroomId,
        Long studentId,
        String status
    ) {
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        ClassroomStudent.Status newStatus = parseStatus(status, teacherId, classroomId, studentId);
        ClassroomStudent classroomStudent = classroomStudentRepository
            .findByClassroom_IdAndStudent_UserId(classroomId, studentId)
            .orElseThrow(() -> {
                log.warn("Student status update failed: membership not found, classroomId={} studentId={} teacherId={}",
                    classroomId, studentId, teacherId);
                return new ResourceNotFoundException("Student not found in classroom");
            });

        classroomStudent.setStatus(newStatus);
        classroomStudent = classroomStudentRepository.save(classroomStudent);

        log.info("Student status updated: classroomId={} studentId={} teacherId={} status={}",
            classroomId, studentId, teacherId, newStatus);
        return toStudentResponse(classroomStudent);
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

    private ClassroomStudent.Status parseStatus(String status, Long teacherId, Long classroomId, Long studentId) {
        try {
            ClassroomStudent.Status parsed = ClassroomStudent.Status.valueOf(status.toUpperCase());
            if (parsed == ClassroomStudent.Status.APPROVED || parsed == ClassroomStudent.Status.KICKED) {
                return parsed;
            }
        } catch (IllegalArgumentException ignored) {
            // Handled below with a consistent API response.
        }
        log.warn("Student status update failed: invalid status, classroomId={} studentId={} teacherId={} status={}",
            classroomId, studentId, teacherId, status);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be APPROVED or KICKED");
    }

    private ClassroomResponse toClassroomResponse(Classroom classroom) {
        return new ClassroomResponse(
            classroom.getId(),
            classroom.getTitle(),
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
