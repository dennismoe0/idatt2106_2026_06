package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.LeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.SchoolLeaderboardEntryDto;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.School;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.SchoolLeaderboardRow;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.LeaderboardRow;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.util.ClassroomCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {
    @Mock ClassroomRepository classroomRepository;
    @Mock ClassroomStudentRepository classroomStudentRepository;
    @Mock ClassroomTeacherRepository classroomTeacherRepository;
    @Mock UserRepository userRepository;
    @Mock ClassroomCodeGenerator classroomCodeGenerator;
    @Mock TaskRepository taskRepository;
    @Mock NotificationService notificationService;
    @InjectMocks ClassroomService classroomService;

    @Test
    void createClassroom_success() {
        User teacher = user(1L, User.Role.TEACHER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(classroomCodeGenerator.generate(classroomRepository)).thenReturn("fjord-tiger");
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(invocation -> {
            Classroom classroom = invocation.getArgument(0);
            classroom.setId(10L);
            classroom.setCreatedAt(LocalDateTime.now());
            return classroom;
        });

        var response = classroomService.createClassroom(
            1L,
            new CreateClassroomRequest("5A", "Digital detective class")
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("5A");
        assertThat(response.joinCode()).isEqualTo("fjord-tiger");
        assertThat(response.description()).isEqualTo("Digital detective class");
    }

    @Test
    void createClassroom_attachesSchoolFromTeacher() {
        School school = school(42L);
        User teacher = user(1L, User.Role.TEACHER);
        teacher.setSchool(school);
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(classroomCodeGenerator.generate(classroomRepository)).thenReturn("fjord-tiger");

        ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
        when(classroomRepository.save(captor.capture())).thenAnswer(invocation -> {
            Classroom classroom = invocation.getArgument(0);
            classroom.setId(10L);
            classroom.setCreatedAt(LocalDateTime.now());
            return classroom;
        });

        classroomService.createClassroom(1L, new CreateClassroomRequest("5A", "desc"));

        assertThat(captor.getValue().getSchool()).isEqualTo(school);
    }

    @Test
    void getLeaderboard_returnsRankedEntries() {
        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(true);
        when(taskRepository.count()).thenReturn(7L);
        when(classroomStudentRepository.getLeaderboard(10L)).thenReturn(List.of(
            leaderboardRow("Agent Alfa", 7L),
            leaderboardRow("Agent Beta", 4L),
            leaderboardRow("Agent Gamma", 1L)
        ));

        List<LeaderboardEntryDto> result = classroomService.getLeaderboard(1L, 10L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).displayName()).isEqualTo("Agent Alfa");
        assertThat(result.get(0).completedTasks()).isEqualTo(7);
        assertThat(result.get(0).totalTasks()).isEqualTo(7);
        assertThat(result.get(1).displayName()).isEqualTo("Agent Beta");
        assertThat(result.get(1).completedTasks()).isEqualTo(4);
        assertThat(result.get(2).displayName()).isEqualTo("Agent Gamma");
        assertThat(result.get(2).completedTasks()).isEqualTo(1);
    }

    @Test
    void getLeaderboard_throwsIfTeacherDoesNotOwnClassroom() {
        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> classroomService.getLeaderboard(99L, 10L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Classroom not found");
    }

    @Test
    void deleteClassroom_setsIsActiveFalse() {
        Classroom classroom = classroom(10L, "7A");
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(true);
        when(classroomRepository.existsById(10L)).thenReturn(true);

        classroomService.deleteClassroom(1L, 10L);

        ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
        verify(classroomRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void deleteClassroom_throws_whenTeacherDoesNotOwnClassroom() {
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom(10L, "7A")));
        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> classroomService.deleteClassroom(1L, 10L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSchoolLeaderboard_returnsAllStudentsAcrossSchoolClassrooms_forApprovedMember() {
        School school = school(5L);

        Classroom c10 = classroom(10L, "Klasse A");
        c10.setSchool(school);
        Classroom c20 = classroom(20L, "Klasse B");
        c20.setSchool(school);
        ClassroomStudent membership = classroomStudent(c10, user(2L, User.Role.STUDENT), ClassroomStudentStatus.APPROVED);

        when(classroomRepository.findById(10L)).thenReturn(Optional.of(c10));
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(membership));
        when(classroomRepository.findBySchool_Id(5L)).thenReturn(List.of(c10, c20));
        when(taskRepository.count()).thenReturn(7L);

        SchoolLeaderboardRow row1 = mockSchoolRow("Alice", 10L, "Klasse A", 5L);
        SchoolLeaderboardRow row2 = mockSchoolRow("Bob",   20L, "Klasse B", 3L);
        when(classroomStudentRepository.getSchoolLeaderboard(List.of(10L, 20L)))
            .thenReturn(List.of(row1, row2));

        List<SchoolLeaderboardEntryDto> result = classroomService.getSchoolLeaderboard(2L, 10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).studentId()).isEqualTo(10L);
        assertThat(result.get(0).displayName()).isEqualTo("Alice");
        assertThat(result.get(0).classroomName()).isEqualTo("Klasse A");
        assertThat(result.get(0).schoolName()).isEqualTo("Testskole");
        assertThat(result.get(0).avatar()).isNotNull();
        assertThat(result.get(0).avatar().hairStyle()).isEqualTo("short");
        assertThat(result.get(1).classroomName()).isEqualTo("Klasse B");
    }

    @Test
    void getSchoolLeaderboard_fallsBackToSingleClassroom_whenTeacherOwnsClassroom() {
        Classroom c10 = classroom(10L, "Klasse A");
        c10.setSchool(null);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(c10));
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(true);
        when(taskRepository.count()).thenReturn(7L);

        SchoolLeaderboardRow row = mockSchoolRow("Alice", 10L, "Klasse A", 5L);
        when(classroomStudentRepository.getSchoolLeaderboard(List.of(10L)))
            .thenReturn(List.of(row));

        List<SchoolLeaderboardEntryDto> result = classroomService.getSchoolLeaderboard(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).displayName()).isEqualTo("Alice");
    }

    @Test
    void getSchoolLeaderboard_backfillsClassroomSchoolFromTeacherMembership() {
        School school = school(5L);
        Classroom c10 = classroom(10L, "Klasse A");
        Classroom c20 = classroom(20L, "Klasse B");
        c10.setSchool(null);
        c20.setSchool(school);
        ClassroomStudent membership = classroomStudent(c10, user(2L, User.Role.STUDENT), ClassroomStudentStatus.APPROVED);

        when(classroomRepository.findById(10L)).thenReturn(Optional.of(c10));
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(membership));
        when(classroomTeacherRepository.findSchoolByClassroomId(10L)).thenReturn(Optional.of(school));
        when(classroomRepository.findBySchool_Id(5L)).thenReturn(List.of(c10, c20));
        when(taskRepository.count()).thenReturn(7L);

        SchoolLeaderboardRow row1 = mockSchoolRow("Alice", 10L, "Klasse A", 5L);
        SchoolLeaderboardRow row2 = mockSchoolRow("Bob", 20L, "Klasse B", 3L);
        when(classroomStudentRepository.getSchoolLeaderboard(List.of(10L, 20L)))
            .thenReturn(List.of(row1, row2));

        List<SchoolLeaderboardEntryDto> result = classroomService.getSchoolLeaderboard(2L, 10L);

        assertThat(result).hasSize(2);
        assertThat(c10.getSchool()).isEqualTo(school);
        verify(classroomRepository).save(c10);
    }

    @Test
    void getSchoolLeaderboard_throwsIfUserIsNotTeacherOrApprovedMember() {
        Classroom c10 = classroom(10L, "Klasse A");
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(c10));
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 99L)).thenReturn(false);
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> classroomService.getSchoolLeaderboard(99L, 10L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Classroom not found");
    }

    @Test
    void getGlobalLeaderboard_returnsAllStudentsAcrossActiveClassrooms() {
        Classroom c10 = classroom(10L, "Klasse A");
        Classroom c20 = classroom(20L, "Klasse B");
        Classroom c30 = classroom(30L, "Klasse C");

        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(classroomStudent(c10, user(2L, User.Role.STUDENT), ClassroomStudentStatus.APPROVED)));
        when(classroomRepository.findByIsActiveTrue()).thenReturn(List.of(c10, c20, c30));
        when(taskRepository.count()).thenReturn(7L);
        when(classroomStudentRepository.getSchoolLeaderboard(List.of(10L, 20L, 30L)))
            .thenReturn(List.of(
                mockSchoolRow("Alice", 10L, "Klasse A", "Testskole", 5L),
                mockSchoolRow("Bob", 20L, "Klasse B", "Annen skole", 3L)
            ));

        List<SchoolLeaderboardEntryDto> result = classroomService.getGlobalLeaderboard(2L, 10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).schoolName()).isEqualTo("Testskole");
        assertThat(result.get(1).schoolName()).isEqualTo("Annen skole");
    }

    @Test
    void joinClassroom_invalidCode_throws() {
        when(classroomRepository.findByJoinCode("bad-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classroomService.joinClassroom(
            2L,
            new JoinClassroomRequest("bad-code", "Agent Nora")
        ))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Classroom not found");
    }

    @Test
    void joinClassroom_alreadyMember_throws() {
        Classroom classroom = classroom(10L);
        ClassroomStudent existing = classroomStudent(classroom, user(2L, User.Role.STUDENT), ClassroomStudentStatus.PENDING);

        when(classroomRepository.findByJoinCode("fjord-tiger")).thenReturn(Optional.of(classroom));
        when(userRepository.getReferenceById(2L)).thenReturn(existing.getStudent());
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> classroomService.joinClassroom(
            2L,
            new JoinClassroomRequest("fjord-tiger", "Agent Nora")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Student is already a member");
    }

    @Test
    void joinClassroom_kickedStudentRejoins_returnsPending() {
        Classroom classroom = classroom(10L);
        ClassroomStudent existing = classroomStudent(classroom, user(2L, User.Role.STUDENT), ClassroomStudentStatus.KICKED);

        when(classroomRepository.findByJoinCode("fjord-tiger")).thenReturn(Optional.of(classroom));
        when(userRepository.getReferenceById(2L)).thenReturn(existing.getStudent());
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(existing));
        when(classroomStudentRepository.save(existing)).thenReturn(existing);

        var response = classroomService.joinClassroom(
            2L,
            new JoinClassroomRequest("fjord-tiger", "Agent Nora")
        );

        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void updateStudentStatus_notOwner_throws() {
        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> classroomService.updateStudentStatus(
            1L,
            10L,
            2L,
            ClassroomStudentStatus.APPROVED
        ))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Classroom not found");
    }

    @Test
    void updateStudentStatus_invalidStatus_throws() {
        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> classroomService.updateStudentStatus(
            1L,
            10L,
            2L,
            ClassroomStudentStatus.PENDING
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Status must be APPROVED or KICKED");
    }

    @Test
    void getMyStatus_member_returnsStatus() {
        Classroom classroom = classroom(10L);
        ClassroomStudent existing = classroomStudent(classroom, user(2L, User.Role.STUDENT), ClassroomStudentStatus.APPROVED);
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(existing));

        var response = classroomService.getMyStatus(2L, 10L);

        assertThat(response.status()).isEqualTo("APPROVED");
    }

    @Test
    void getMyStatus_notMember_throws() {
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> classroomService.getMyStatus(2L, 10L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Student not in classroom");
    }

    @Test
    void joinClassroom_success_returnsClassroomId() {
        Classroom classroom = classroom(10L);
        User teacher = user(1L, User.Role.TEACHER);
        User student = user(2L, User.Role.STUDENT);

        when(classroomRepository.findByJoinCode("fjord-tiger")).thenReturn(Optional.of(classroom));
        when(userRepository.getReferenceById(2L)).thenReturn(student);
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.empty());
        when(classroomStudentRepository.save(any(ClassroomStudent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(classroomTeacherRepository.findTeachersByClassroomId(10L)).thenReturn(List.of(teacher));

        StudentInClassroomResponse response = classroomService.joinClassroom(
            2L,
            new JoinClassroomRequest("fjord-tiger", "Agent Nora")
        );

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.classroomId()).isEqualTo(10L);
        assertThat(response.displayName()).isEqualTo("Agent Nora");
        assertThat(response.status()).isEqualTo("PENDING");
        verify(notificationService).createNotification(
            1L,
            10L,
            NotificationService.STUDENT_JOIN_REQUEST,
            "Agent Nora vil bli med i 5A",
            2L
        );
    }

    private School school(Long id) {
        School school = new School();
        school.setId(id);
        school.setName("Testskole");
        school.setJoinCode("TEST01");
        return school;
    }

    private LeaderboardRow leaderboardRow(String displayName, Long completedTasks) {
        return new LeaderboardRow() {
            @Override public String getDisplayName() { return displayName; }
            @Override public Long getCompletedTasks() { return completedTasks; }
        };
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@test.no");
        user.setPasswordHash("hashed");
        user.setRole(role);
        return user;
    }

    private Classroom classroom(Long id) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setName("5A");
        classroom.setJoinCode("fjord-tiger");
        classroom.setActive(true);
        return classroom;
    }

    private Classroom classroom(Long id, String name) {
        Classroom c = new Classroom();
        c.setId(id);
        c.setName(name);
        c.setJoinCode("test-code");
        c.setActive(true);
        return c;
    }

    private ClassroomStudent classroomStudent(Classroom classroom, User student, ClassroomStudentStatus status) {
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setClassroom(classroom);
        classroomStudent.setStudent(student);
        classroomStudent.setDisplayName("Agent Nora");
        classroomStudent.setStatus(status);
        return classroomStudent;
    }

    private SchoolLeaderboardRow mockSchoolRow(
        String name,
        Long classroomId,
        String classroomName,
        Long completed
    ) {
        return mockSchoolRow(name, classroomId, classroomName, "Testskole", completed);
    }

    private SchoolLeaderboardRow mockSchoolRow(
        String name,
        Long classroomId,
        String classroomName,
        String schoolName,
        Long completed
    ) {
        return new SchoolLeaderboardRow() {
            public Long getStudentId() { return classroomId; }
            public String getDisplayName()   { return name; }
            public Long getClassroomId()     { return classroomId; }
            public String getClassroomName() { return classroomName; }
            public String getSchoolName() { return schoolName; }
            public Long getCompletedTasks()  { return completed; }
            public String getAvatarGender() { return "neutral"; }
            public String getAvatarEyeColor() { return "#4a3000"; }
            public String getAvatarEyeStyle() { return "round"; }
            public String getAvatarSkinColor() { return "#D08B5B"; }
            public String getAvatarHairColor() { return "#8B4513"; }
            public String getAvatarHairStyle() { return "short"; }
            public String getAvatarOutfit() { return "detective-coat"; }
            public String getAvatarOutfitColor() { return "#2563eb"; }
            public String getAvatarHatColor() { return "none"; }
            public String getAvatarAccessory() { return "badge"; }
        };
    }
}
