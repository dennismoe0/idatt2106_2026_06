package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.classroom.CreateClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.JoinClassroomRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.classroom.StudentInClassroomResponse;
import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.util.ClassroomCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {
    @Mock ClassroomRepository classroomRepository;
    @Mock ClassroomStudentRepository classroomStudentRepository;
    @Mock ClassroomTeacherRepository classroomTeacherRepository;
    @Mock UserRepository userRepository;
    @Mock ClassroomCodeGenerator classroomCodeGenerator;
    @InjectMocks ClassroomService classroomService;

    @Test
    void createClassroom_success() {
        User teacher = user(1L, User.Role.TEACHER);
        when(userRepository.getReferenceById(1L)).thenReturn(teacher);
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
        ClassroomStudent existing = classroomStudent(classroom, user(2L, User.Role.STUDENT), ClassroomStudent.Status.PENDING);

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
    void joinClassroom_kickedStudentCannotRejoin_throws() {
        Classroom classroom = classroom(10L);
        ClassroomStudent existing = classroomStudent(classroom, user(2L, User.Role.STUDENT), ClassroomStudent.Status.KICKED);

        when(classroomRepository.findByJoinCode("fjord-tiger")).thenReturn(Optional.of(classroom));
        when(userRepository.getReferenceById(2L)).thenReturn(existing.getStudent());
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> classroomService.joinClassroom(
            2L,
            new JoinClassroomRequest("fjord-tiger", "Agent Nora")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Student cannot rejoin this classroom");
    }

    @Test
    void updateStudentStatus_notOwner_throws() {
        when(classroomRepository.existsById(10L)).thenReturn(true);
        when(classroomTeacherRepository.existsByClassroom_IdAndTeacher_UserId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> classroomService.updateStudentStatus(
            1L,
            10L,
            2L,
            ClassroomStudent.Status.APPROVED
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
            ClassroomStudent.Status.PENDING
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Status must be APPROVED or KICKED");
    }

    @Test
    void joinClassroom_success_returnsClassroomId() {
        Classroom classroom = classroom(10L);
        User student = user(2L, User.Role.STUDENT);

        when(classroomRepository.findByJoinCode("fjord-tiger")).thenReturn(Optional.of(classroom));
        when(userRepository.getReferenceById(2L)).thenReturn(student);
        when(classroomStudentRepository.findByClassroom_IdAndStudent_UserId(10L, 2L))
            .thenReturn(Optional.empty());
        when(classroomStudentRepository.save(any(ClassroomStudent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentInClassroomResponse response = classroomService.joinClassroom(
            2L,
            new JoinClassroomRequest("fjord-tiger", "Agent Nora")
        );

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.classroomId()).isEqualTo(10L);
        assertThat(response.displayName()).isEqualTo("Agent Nora");
        assertThat(response.status()).isEqualTo("PENDING");
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
        return classroom;
    }

    private ClassroomStudent classroomStudent(Classroom classroom, User student, ClassroomStudent.Status status) {
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setClassroom(classroom);
        classroomStudent.setStudent(student);
        classroomStudent.setDisplayName("Agent Nora");
        classroomStudent.setStatus(status);
        return classroomStudent;
    }
}
