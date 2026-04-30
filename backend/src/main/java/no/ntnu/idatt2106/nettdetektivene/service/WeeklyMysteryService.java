package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.MysteryCompleteDto;
import no.ntnu.idatt2106.nettdetektivene.dto.MysteryCompleteResultDto;
import no.ntnu.idatt2106.nettdetektivene.dto.WeeklyMysteryEditDto;
import no.ntnu.idatt2106.nettdetektivene.dto.WeeklyMysterySubmissionDto;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMysteryCompletion;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.entity.WeeklyMystery;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMysteryCompletionRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.WeeklyMysteryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WeeklyMysteryService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyMysteryService.class);

    private static final String MEDAL_FIRST_CORRECT = "Ukens detektiv";
    private static final String MEDAL_FIVE_CORRECT  = "Mysterium-mester";

    private final WeeklyMysteryRepository mysteryRepo;
    private final StudentMysteryCompletionRepository completionRepo;
    private final MedalRepository medalRepo;
    private final StudentMedalRepository studentMedalRepo;
    private final UserRepository userRepo;
    private final ClassroomRepository classroomRepo;
    private final ClassroomStudentRepository classroomStudentRepo;
    private final ClassroomTeacherRepository classroomTeacherRepo;
    private final NotificationService notificationService;

    public WeeklyMysteryService(
            WeeklyMysteryRepository mysteryRepo,
            StudentMysteryCompletionRepository completionRepo,
            MedalRepository medalRepo,
            StudentMedalRepository studentMedalRepo,
            UserRepository userRepo,
            ClassroomRepository classroomRepo,
            ClassroomStudentRepository classroomStudentRepo,
            ClassroomTeacherRepository classroomTeacherRepo,
            NotificationService notificationService) {
        this.mysteryRepo = mysteryRepo;
        this.completionRepo = completionRepo;
        this.medalRepo = medalRepo;
        this.studentMedalRepo = studentMedalRepo;
        this.userRepo = userRepo;
        this.classroomRepo = classroomRepo;
        this.classroomStudentRepo = classroomStudentRepo;
        this.classroomTeacherRepo = classroomTeacherRepo;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Student: submit a mystery
    // -------------------------------------------------------------------------

    @Transactional
    public WeeklyMystery submitMystery(Long studentId, WeeklyMysterySubmissionDto dto) {
        log.info("[WeeklyMysteryService] submitMystery studentId={} classroomId={}", studentId, dto.classroomId());

        User student = userRepo.getReferenceById(studentId);

        var classroom = classroomRepo.findById(dto.classroomId())
                .orElseThrow(() -> {
                    log.warn("[WeeklyMysteryService] classroom not found id={}", dto.classroomId());
                    return new IllegalArgumentException("Classroom not found: " + dto.classroomId());
                });

        WeeklyMystery mystery = new WeeklyMystery();
        mystery.setClassroom(classroom);
        mystery.setStudent(student);
        mystery.setTitle(dto.title());
        mystery.setDescription(dto.description());
        mystery.setImageUrl(dto.imageUrl());
        mystery.setStatus(WeeklyMystery.Status.PENDING);

        WeeklyMystery saved = mysteryRepo.save(mystery);
        notifyTeachersAboutMysterySubmission(saved);
        log.info("[WeeklyMysteryService] mystery submitted mysteryId={}", saved.getId());
        return saved;
    }

    // -------------------------------------------------------------------------
    // Student: get active (featured + approved) mystery for classroom
    // -------------------------------------------------------------------------

    public WeeklyMystery getActiveMystery(Long classroomId) {
        log.info("[WeeklyMysteryService] getActiveMystery classroomId={}", classroomId);
        return mysteryRepo.findByClassroomIdAndFeaturedTrue(classroomId).orElse(null);
    }

    // -------------------------------------------------------------------------
    // Student: has already completed this mystery?
    // -------------------------------------------------------------------------

    public boolean hasStudentCompleted(Long studentId, Long mysteryId) {
        boolean completed = completionRepo.existsByStudentIdAndMysteryId(studentId, mysteryId);
        log.info("[WeeklyMysteryService] hasStudentCompleted studentId={} mysteryId={} result={}", studentId, mysteryId, completed);
        return completed;
    }

    // -------------------------------------------------------------------------
    // Student: submit answer for active mystery
    // -------------------------------------------------------------------------

    @Transactional
    public MysteryCompleteResultDto completeMystery(Long studentId, MysteryCompleteDto dto) {
        log.info("[WeeklyMysteryService] completeMystery studentId={} classroomId={}", studentId, dto.classroomId());

        User student = userRepo.getReferenceById(studentId);

        if (!classroomStudentRepo.existsByClassroom_IdAndStudent_IdAndStatus(
                dto.classroomId(), student.getId(), ClassroomStudentStatus.APPROVED)) {
            log.warn("[WeeklyMysteryService] student not enrolled studentId={} classroomId={}", student.getId(), dto.classroomId());
            throw new SecurityException("Student is not enrolled in this classroom");
        }

        WeeklyMystery mystery = mysteryRepo.findByClassroomIdAndFeaturedTrue(dto.classroomId())
                .orElseThrow(() -> {
                    log.warn("[WeeklyMysteryService] no featured mystery for classroomId={}", dto.classroomId());
                    return new IllegalStateException("No active mystery for classroom: " + dto.classroomId());
                });

        if (completionRepo.existsByStudentIdAndMysteryId(student.getId(), mystery.getId())) {
            log.warn("[WeeklyMysteryService] student already completed mysteryId={} studentId={}", mystery.getId(), student.getId());
            throw new IllegalStateException("Student has already completed this mystery");
        }

        boolean correct = mystery.getCorrectAnswer() != null
                && mystery.getCorrectAnswer().equalsIgnoreCase(dto.answer());

        StudentMysteryCompletion completion = new StudentMysteryCompletion();
        completion.setStudent(student);
        completion.setMystery(mystery);
        completion.setClassroom(mystery.getClassroom());
        completion.setCorrect(correct);
        completionRepo.save(completion);

        log.info("[WeeklyMysteryService] mystery completion saved mysteryId={} studentId={} correct={}", mystery.getId(), student.getId(), correct);

        int starsEarned = correct ? mystery.getRewardStars() : 0;
        int xpEarned    = correct ? mystery.getRewardXp()    : 0;
        String medalName = null;
        boolean medalEarned = false;

        if (correct) {
            User studentEntity = userRepo.findById(student.getId())
                    .orElseThrow(() -> new IllegalStateException("Student not found: " + student.getId()));
            studentEntity.setXp(studentEntity.getXp() + xpEarned);
            studentEntity.setStarBalance(studentEntity.getStarBalance() + starsEarned);
            userRepo.save(studentEntity);
            log.info("[WeeklyMysteryService] awarded xp={} stars={} to studentId={}", xpEarned, starsEarned, student.getId());
            long correctCount = completionRepo.countByStudentIdAndCorrectTrue(student.getId());
            log.info("[WeeklyMysteryService] correctCount for studentId={} is {}", student.getId(), correctCount);

            if (correctCount == 1) {
                String awarded = tryAwardMedal(student, MEDAL_FIRST_CORRECT);
                if (awarded != null) {
                    medalEarned = true;
                    medalName = awarded;
                }
            } else if (correctCount == 5) {
                String awarded = tryAwardMedal(student, MEDAL_FIVE_CORRECT);
                if (awarded != null) {
                    medalEarned = true;
                    medalName = awarded;
                }
            }
        }

        return new MysteryCompleteResultDto(
                correct,
                mystery.getTeacherComment(),
                starsEarned,
                xpEarned,
                medalEarned,
                medalName
        );
    }

    // -------------------------------------------------------------------------
    // Teacher: list all submissions for a classroom
    // -------------------------------------------------------------------------

    public List<WeeklyMystery> getSubmissions(Long classroomId) {
        log.info("[WeeklyMysteryService] getSubmissions classroomId={}", classroomId);
        return mysteryRepo.findByClassroomIdOrderByCreatedAtDesc(classroomId);
    }

    // -------------------------------------------------------------------------
    // Teacher: edit/approve a mystery (sets status to APPROVED)
    // -------------------------------------------------------------------------

    @Transactional
    public WeeklyMystery editMystery(Long mysteryId, WeeklyMysteryEditDto dto, Long teacherId) {
        log.info("[WeeklyMysteryService] editMystery mysteryId={} teacherId={}", mysteryId, teacherId);

        WeeklyMystery mystery = mysteryRepo.findById(mysteryId)
                .orElseThrow(() -> {
                    log.warn("[WeeklyMysteryService] mystery not found id={}", mysteryId);
                    return new IllegalArgumentException("Mystery not found: " + mysteryId);
                });

        if (!classroomRepo.isTeacherOfClassroom(mystery.getClassroom().getId(), teacherId)) {
            log.warn("[WeeklyMysteryService] unauthorized edit attempt teacherId={} mysteryId={}", teacherId, mysteryId);
            throw new SecurityException("Not authorized to edit this mystery");
        }

        if (dto.title() != null)         mystery.setTitle(dto.title());
        if (dto.description() != null)   mystery.setDescription(dto.description());
        if (dto.imageUrl() != null)      mystery.setImageUrl(dto.imageUrl());
        if (dto.mysteryType() != null)   mystery.setMysteryType(dto.mysteryType());
        if (dto.questionText() != null)  mystery.setQuestionText(dto.questionText());
        if (dto.correctAnswer() != null) mystery.setCorrectAnswer(dto.correctAnswer());
        if (dto.teacherComment() != null) mystery.setTeacherComment(dto.teacherComment());
        if (dto.rewardStars() > 0)       mystery.setRewardStars(dto.rewardStars());
        if (dto.rewardXp() > 0)          mystery.setRewardXp(dto.rewardXp());

        mystery.setStatus(WeeklyMystery.Status.APPROVED);

        WeeklyMystery saved = mysteryRepo.save(mystery);
        log.info("[WeeklyMysteryService] mystery edited and approved mysteryId={}", mysteryId);
        return saved;
    }

    // -------------------------------------------------------------------------
    // Teacher: activate (feature) a mystery for the classroom
    // -------------------------------------------------------------------------

    @Transactional
    public WeeklyMystery activateMystery(Long mysteryId, Long classroomId, Long teacherId) {
        log.info("[WeeklyMysteryService] activateMystery mysteryId={} classroomId={} teacherId={}", mysteryId, classroomId, teacherId);

        if (!classroomRepo.isTeacherOfClassroom(classroomId, teacherId)) {
            log.warn("[WeeklyMysteryService] unauthorized activate attempt teacherId={} classroomId={}", teacherId, classroomId);
            throw new SecurityException("Not authorized to activate mystery in this classroom");
        }

        // Deactivate any currently featured mystery in this classroom
        mysteryRepo.findByClassroomIdAndFeaturedTrue(classroomId).ifPresent(current -> {
            log.info("[WeeklyMysteryService] deactivating previously featured mysteryId={}", current.getId());
            current.setFeatured(false);
            mysteryRepo.save(current);
        });

        WeeklyMystery mystery = mysteryRepo.findById(mysteryId)
                .orElseThrow(() -> {
                    log.warn("[WeeklyMysteryService] mystery not found id={}", mysteryId);
                    return new IllegalArgumentException("Mystery not found: " + mysteryId);
                });

        mystery.setFeatured(true);
        WeeklyMystery saved = mysteryRepo.save(mystery);
        log.info("[WeeklyMysteryService] mystery activated mysteryId={}", mysteryId);
        return saved;
    }

    // -------------------------------------------------------------------------
    // Teacher: reject a mystery
    // -------------------------------------------------------------------------

    @Transactional
    public WeeklyMystery rejectMystery(Long mysteryId, Long teacherId) {
        log.info("[WeeklyMysteryService] rejectMystery mysteryId={} teacherId={}", mysteryId, teacherId);

        WeeklyMystery mystery = mysteryRepo.findById(mysteryId)
                .orElseThrow(() -> {
                    log.warn("[WeeklyMysteryService] mystery not found id={}", mysteryId);
                    return new IllegalArgumentException("Mystery not found: " + mysteryId);
                });

        if (!classroomRepo.isTeacherOfClassroom(mystery.getClassroom().getId(), teacherId)) {
            log.warn("[WeeklyMysteryService] unauthorized reject attempt teacherId={} mysteryId={}", teacherId, mysteryId);
            throw new SecurityException("Not authorized to reject this mystery");
        }

        mystery.setStatus(WeeklyMystery.Status.REJECTED);
        WeeklyMystery saved = mysteryRepo.save(mystery);
        log.info("[WeeklyMysteryService] mystery rejected mysteryId={}", mysteryId);
        return saved;
    }

    // -------------------------------------------------------------------------
    // Internal: try to award a named medal; returns medal name if awarded, null otherwise
    // -------------------------------------------------------------------------

    private String tryAwardMedal(User student, String medalName) {
        Optional<Medal> medal = medalRepo.findByName(medalName);
        if (medal.isEmpty()) {
            log.warn("[WeeklyMysteryService] medal not found name={}", medalName);
            return null;
        }

        Long medalId = medal.get().getId();
        if (studentMedalRepo.existsByStudent_IdAndMedal_Id(student.getId(), medalId)) {
            log.info("[WeeklyMysteryService] student already has medal studentId={} medalId={}", student.getId(), medalId);
            return null;
        }

        StudentMedal studentMedal = new StudentMedal();
        studentMedal.setStudent(userRepo.getReferenceById(student.getId()));
        studentMedal.setMedal(medal.get());
        studentMedalRepo.save(studentMedal);

        log.info("[WeeklyMysteryService] awarded medal '{}' to studentId={}", medalName, student.getId());
        return medalName;
    }

    private void notifyTeachersAboutMysterySubmission(WeeklyMystery mystery) {
        String message = "Nytt ukens mysterium sendt inn: " + mystery.getTitle();
        classroomTeacherRepo.findTeachersByClassroomId(mystery.getClassroom().getId())
            .forEach(teacher -> notificationService.createNotification(
                teacher.getId(),
                mystery.getClassroom().getId(),
                NotificationService.MYSTERY_SUBMITTED,
                message,
                mystery.getId()
            ));
    }
}
