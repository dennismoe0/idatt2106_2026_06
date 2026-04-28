package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.ClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.SchoolLeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentProgressSummaryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentStatusResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.game.StopResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentProgressRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.SchoolLeaderboardRow;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.School;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final NotificationService notificationService;
    private final StopRepository stopRepository;
    private final StudentProgressRepository studentProgressRepository;

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

    @Transactional
    public void deleteClassroom(Long teacherId, Long classroomId) {
        Classroom classroom = getClassroomForTeacher(teacherId, classroomId);
        classroom.setActive(false);
        classroomRepository.save(classroom);
        log.info("[ClassroomService] Classroom soft-deleted: classroomId={} by teacherId={}", classroomId, teacherId);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(Long teacherId, Long classroomId) {
        log.info("[ClassroomService] getLeaderboard teacherId={} classroomId={}", teacherId, classroomId);
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        int totalTasks = Math.toIntExact(taskRepository.count());
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

        var existing = classroomStudentRepository.findByClassroom_IdAndStudent_UserId(classroom.getId(), studentId);
        if (existing.isPresent()) {
            ClassroomStudent record = existing.get();
            if (record.getStatus() == ClassroomStudentStatus.KICKED) {
                record.setStatus(ClassroomStudentStatus.PENDING);
                record.setDisplayName(req.displayName());
                record = classroomStudentRepository.save(record);
                log.info("Kicked student re-applied: classroomId={} studentId={} → PENDING", classroom.getId(), studentId);
                return toStudentResponse(record);
            }
            log.warn("Classroom join blocked: student already member, classroomId={} studentId={} status={}",
                classroom.getId(), studentId, record.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is already a member");
        }

        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setClassroom(classroom);
        classroomStudent.setStudent(student);
        classroomStudent.setDisplayName(req.displayName());
        classroomStudent.setStatus(ClassroomStudentStatus.PENDING);
        classroomStudent = classroomStudentRepository.save(classroomStudent);
        notifyTeachersAboutJoinRequest(classroom, studentId, req.displayName());

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

    @Transactional(readOnly = true)
    public List<StudentProgressSummaryDto> getStudentProgressSummaries(Long teacherId, Long classroomId) {
        log.info("[ClassroomService] getStudentProgressSummaries teacherId={} classroomId={}", teacherId, classroomId);
        verifyTeacherOwnsClassroom(teacherId, classroomId);

        List<ClassroomStudent> approved = classroomStudentRepository.findByClassroom_Id(classroomId).stream()
            .filter(cs -> cs.getStatus() == ClassroomStudentStatus.APPROVED)
            .toList();

        List<Stop> stops = stopRepository.findAllByOrderByOrderIndexAsc();

        // Required task count per stop (excluding LEARN tasks)
        Map<Long, Long> requiredPerStop = stops.stream().collect(Collectors.toMap(
            Stop::getId,
            s -> taskRepository.countByStop_IdAndTaskTypeNot(s.getId(), TaskType.LEARN)
        ));

        return approved.stream().map(member -> {
            Long studentId = member.getStudent().getId();
            int totalCompleted = (int) studentProgressRepository.countByStudent_IdAndCompletedTrue(studentId);

            // Current stop = first stop the student hasn't yet fully completed
            Stop current = stops.stream()
                .filter(stop -> {
                    long required = requiredPerStop.getOrDefault(stop.getId(), 0L);
                    if (required == 0) return false;
                    long done = studentProgressRepository
                        .countByStudent_IdAndTask_Stop_IdAndCompletedTrueAndTask_TaskTypeNot(
                            studentId, stop.getId(), TaskType.LEARN);
                    return done < required;
                })
                .findFirst()
                .orElse(null);

            String currentStopName = current != null ? current.getName() : "Fullført";
            int currentStopOrder = current != null ? current.getOrderIndex() : stops.size() + 1;
            return new StudentProgressSummaryDto(studentId, member.getDisplayName(), totalCompleted, currentStopName, currentStopOrder);
        }).toList();
    }

    public List<StopResponse> getStopsForClassroom(Long teacherId, Long classroomId) {
        log.info("Fetching stops for classroomId={} teacherId={}", classroomId, teacherId);
        verifyTeacherOwnsClassroom(teacherId, classroomId);
        return stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .map(this::toStopResponse)
            .toList();
    }

    private StopResponse toStopResponse(Stop stop) {
        int taskCount = (int) taskRepository.countByStop_Id(stop.getId());
        return new StopResponse(
            stop.getId(),           // Long id
            stop.getName(),         // String name
            stop.getOrderIndex(),   // int orderIndex
            stop.getDescription(),  // String description
            false,                  // boolean locked  (no locking logic yet — adjust if needed)
            false,                  // boolean completed (teacher view: not per-student, so false)
            taskCount,              // int taskCount
            0,                      // int correctCount (teacher view: no per-student data here)
            false,                  // boolean xpClaimable
            stop.getTheme()         // String theme
        );
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

    public List<LeaderboardEntryDto> getLeaderboard(Long classroomId) {
        log.info("[ClassroomService] getLeaderboard classroomId={}", classroomId);
        int totalTasks = (int) taskRepository.count();
        return classroomStudentRepository.getLeaderboard(classroomId).stream()
            .map(row -> new LeaderboardEntryDto(
                row.getDisplayName(),
                row.getCompletedTasks() == null ? 0 : row.getCompletedTasks().intValue(),
                totalTasks
            ))
            .toList();
    }

    @Transactional
    public List<SchoolLeaderboardEntryDto> getSchoolLeaderboard(Long userId, Long classroomId) {
        log.info("[ClassroomService] getSchoolLeaderboard userId={} classroomId={}", userId, classroomId);
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        verifySchoolLeaderboardAccess(userId, classroomId);

        School school = resolveSchoolForClassroom(classroom);
        int totalTasks = (int) taskRepository.count();
        List<Long> classroomIds;
        if (school != null) {
            classroomIds = classroomRepository.findBySchool_Id(school.getId()).stream()
                .map(Classroom::getId)
                .toList();
            log.info("[ClassroomService] School {} leaderboard includes {} classrooms", school.getId(), classroomIds.size());
        } else {
            classroomIds = List.of(classroomId);
            log.info("[ClassroomService] No school for classroomId={}, using single-classroom leaderboard", classroomId);
        }

        return toSchoolLeaderboardEntries(classroomIds, totalTasks);
    }

    @Transactional(readOnly = true)
    public List<SchoolLeaderboardEntryDto> getGlobalLeaderboard(Long userId, Long classroomId) {
        log.info("[ClassroomService] getGlobalLeaderboard userId={} classroomId={}", userId, classroomId);
        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classroom not found");
        }
        verifySchoolLeaderboardAccess(userId, classroomId);
        int totalTasks = (int) taskRepository.count();
        List<Long> classroomIds = classroomRepository.findByIsActiveTrue().stream()
            .map(Classroom::getId)
            .toList();
        log.info("[ClassroomService] Global leaderboard includes {} active classrooms", classroomIds.size());

        return toSchoolLeaderboardEntries(classroomIds, totalTasks);
    }

    private List<SchoolLeaderboardEntryDto> toSchoolLeaderboardEntries(List<Long> classroomIds, int totalTasks) {
        return classroomStudentRepository.getSchoolLeaderboard(classroomIds).stream()
            .map(row -> new SchoolLeaderboardEntryDto(
                row.getStudentId(),
                row.getDisplayName(),
                row.getClassroomId(),
                row.getClassroomName(),
                row.getSchoolName(),
                row.getCompletedTasks() == null ? 0 : row.getCompletedTasks().intValue(),
                totalTasks,
                toAvatarResponse(row)
            ))
            .toList();
    }

    public StudentInClassroomResponse updateMyDisplayName(Long studentId, Long classroomId, String displayName) {
        log.info("[ClassroomService] updateMyDisplayName studentId={} classroomId={}", studentId, classroomId);
        ClassroomStudent cs = classroomStudentRepository
            .findByClassroom_IdAndStudent_UserId(classroomId, studentId)
            .orElseThrow(() -> {
                log.warn("[ClassroomService] updateMyDisplayName: student {} not in classroom {}", studentId, classroomId);
                return new ResourceNotFoundException("Student not in classroom");
            });
        cs.setDisplayName(displayName);
        cs = classroomStudentRepository.save(cs);
        log.info("[ClassroomService] Display name updated: studentId={} classroomId={} name={}", studentId, classroomId, displayName);
        return toStudentResponse(cs);
    }

    public Optional<StudentInClassroomResponse> getMyClassroom(Long studentId) {
        log.info("[ClassroomService] getMyClassroom studentId={}", studentId);
        return classroomStudentRepository
            .findByStudentIdAndStatusNot(studentId, ClassroomStudentStatus.KICKED)
            .map(this::toStudentResponse);
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
        boolean musicMuted = entry.getClassroom().isMusicMuted();
        return new StudentStatusResponse(entry.getStatus().name(), musicMuted);
    }

    @Transactional
    public void setMusicMuted(Long teacherId, Long classroomId, boolean musicMuted) {
        log.info("[ClassroomService] setMusicMuted classroomId={} musicMuted={}", classroomId, musicMuted);
        Classroom classroom = getClassroomForTeacher(teacherId, classroomId);
        classroom.setMusicMuted(musicMuted);
        classroomRepository.save(classroom);
        log.info("[ClassroomService] Music muted updated for classroom {}", classroomId);
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
        if (!classroomRepository.existsById(classroomId)) {
            log.warn("Classroom not found: classroomId={}", classroomId);
            throw new ResourceNotFoundException("Classroom not found");
        }
        if (!classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(classroomId, teacherId)) {
            log.warn("Classroom access denied: classroomId={} teacherId={}", classroomId, teacherId);
            throw new ResourceNotFoundException("Classroom not found");
        }
    }

    private void verifySchoolLeaderboardAccess(Long userId, Long classroomId) {
        boolean teacherOwnsClassroom =
            classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(classroomId, userId);
        if (teacherOwnsClassroom) {
            return;
        }

        boolean approvedStudentInClassroom = classroomStudentRepository
            .findByClassroom_IdAndStudent_UserId(classroomId, userId)
            .map(classroomStudent -> classroomStudent.getStatus() == ClassroomStudentStatus.APPROVED)
            .orElse(false);

        if (!approvedStudentInClassroom) {
            log.warn("[ClassroomService] School leaderboard access denied: classroomId={} userId={}", classroomId, userId);
            throw new ResourceNotFoundException("Classroom not found");
        }
    }

    private void notifyTeachersAboutJoinRequest(Classroom classroom, Long studentId, String displayName) {
        String message = displayName + " vil bli med i " + classroom.getName();
        classroomTeacherRepository.findTeachersByClassroomId(classroom.getId())
            .forEach(teacher -> notificationService.createNotification(
                teacher.getId(),
                classroom.getId(),
                NotificationService.STUDENT_JOIN_REQUEST,
                message,
                studentId
            ));
    }

    private School resolveSchoolForClassroom(Classroom classroom) {
        if (classroom.getSchool() != null) {
            return classroom.getSchool();
        }

        Optional<School> teacherSchool = classroomTeacherRepository.findSchoolByClassroomId(classroom.getId());
        if (teacherSchool.isEmpty()) {
            return null;
        }

        classroom.setSchool(teacherSchool.get());
        classroomRepository.save(classroom);
        log.info(
            "[ClassroomService] Backfilled school {} for classroom {} from teacher membership",
            teacherSchool.get().getId(),
            classroom.getId()
        );
        return teacherSchool.get();
    }

    private AvatarResponse toAvatarResponse(SchoolLeaderboardRow row) {
        if (row.getAvatarGender() == null) {
            return null;
        }

        return new AvatarResponse(
            row.getAvatarGender(),
            row.getAvatarEyeColor(),
            row.getAvatarEyeStyle(),
            row.getAvatarSkinColor(),
            row.getAvatarHairColor(),
            row.getAvatarHairStyle(),
            row.getAvatarOutfit(),
            row.getAvatarOutfitColor(),
            row.getAvatarHatColor(),
            row.getAvatarAccessory()
        );
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
            classroomStudent.getStudent().getEmail(),
            classroomStudent.getStatus().name()
        );
    }
}
