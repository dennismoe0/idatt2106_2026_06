package no.ntnu.idatt2106.nettdetektivene.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock SchoolRepository schoolRepository;
    @Mock UserRepository userRepository;
    @Mock ClassroomRepository classroomRepository;
    @Mock ClassroomStudentRepository classroomStudentRepository;
    @Mock TaskRepository taskRepository;
    @Mock SchoolCodeGenerator schoolCodeGenerator;

    @InjectMocks SchoolService schoolService;

    private static final Long TEACHER_ID = 1L;
    private static final Long SCHOOL_ID = 10L;

    private User teacher(School school) {
        User u = new User();
        u.setId(TEACHER_ID);
        u.setRole(User.Role.TEACHER);
        u.setSchool(school);
        return u;
    }

    private School school() {
        School s = new School();
        s.setId(SCHOOL_ID);
        s.setName("Vågsbygd skole");
        s.setJoinCode("nord-01");
        return s;
    }

    @Test
    void createSchool_createsSchoolAndLinksTeacher() {
        User teacher = teacher(null);
        Classroom classroom = new Classroom();
        classroom.setId(5L);
        classroom.setName("7A");
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));
        when(schoolCodeGenerator.generate(schoolRepository)).thenReturn("nord-01");
        School saved = school();
        when(schoolRepository.save(any(School.class))).thenReturn(saved);
        when(classroomRepository.findByTeachers_Teacher_UserId(TEACHER_ID)).thenReturn(List.of(classroom));
        when(classroomRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        SchoolResponse response = schoolService.createSchool(TEACHER_ID, new CreateSchoolRequest("Vågsbygd skole"));

        assertThat(response.name()).isEqualTo("Vågsbygd skole");
        assertThat(response.joinCode()).isEqualTo("nord-01");
        verify(userRepository).save(teacher);
        verify(classroomRepository).saveAll(List.of(classroom));
        assertThat(teacher.getSchool()).isEqualTo(saved);
        assertThat(classroom.getSchool()).isEqualTo(saved);
    }

    @Test
    void createSchool_throwsIfTeacherAlreadyHasSchool() {
        User teacher = teacher(school());
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> schoolService.createSchool(TEACHER_ID, new CreateSchoolRequest("Annen skole")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("already belongs");
    }

    @Test
    void joinSchool_linksTeacherToSchool() {
        User teacher = teacher(null);
        School s = school();
        Classroom classroom = new Classroom();
        classroom.setId(5L);
        classroom.setName("7A");
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));
        when(schoolRepository.findByJoinCode("nord-01")).thenReturn(Optional.of(s));
        when(classroomRepository.findByTeachers_Teacher_UserId(TEACHER_ID)).thenReturn(List.of(classroom));
        when(classroomRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        SchoolResponse response = schoolService.joinSchool(TEACHER_ID, new JoinSchoolRequest("nord-01"));

        assertThat(response.id()).isEqualTo(SCHOOL_ID);
        verify(userRepository).save(teacher);
        verify(classroomRepository).saveAll(List.of(classroom));
        assertThat(teacher.getSchool()).isEqualTo(s);
        assertThat(classroom.getSchool()).isEqualTo(s);
    }

    @Test
    void joinSchool_throwsIfTeacherAlreadyHasSchool() {
        User teacher = teacher(school());
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> schoolService.joinSchool(TEACHER_ID, new JoinSchoolRequest("nord-01")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("already belongs");
    }

    @Test
    void joinSchool_throwsOnInvalidCode() {
        User teacher = teacher(null);
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));
        when(schoolRepository.findByJoinCode("bad-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolService.joinSchool(TEACHER_ID, new JoinSchoolRequest("bad-code")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMySchool_returnsSchool() {
        User teacher = teacher(school());
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));

        SchoolResponse response = schoolService.getMySchool(TEACHER_ID);

        assertThat(response.name()).isEqualTo("Vågsbygd skole");
    }

    @Test
    void getMySchool_throwsIfNoSchool() {
        User teacher = teacher(null);
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> schoolService.getMySchool(TEACHER_ID))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSchoolClassrooms_returnsSummariesWithTop5() {
        School s = school();
        User teacher = teacher(s);
        Classroom cls = new Classroom();
        cls.setId(5L);
        cls.setName("7A");
        cls.setSchool(s);

        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));
        when(classroomRepository.findBySchool_Id(SCHOOL_ID)).thenReturn(List.of(cls));
        when(taskRepository.count()).thenReturn(10L);

        var rows = List.of(
            row("Alpha", 9L), row("Beta", 8L), row("Gamma", 7L),
            row("Delta", 6L), row("Epsilon", 5L), row("Zeta", 4L)
        );
        when(classroomStudentRepository.getLeaderboard(5L)).thenReturn(rows);

        List<SchoolClassroomSummary> summaries = schoolService.getSchoolClassrooms(TEACHER_ID);

        assertThat(summaries).hasSize(1);
        SchoolClassroomSummary summary = summaries.get(0);
        assertThat(summary.classroomId()).isEqualTo(5L);
        assertThat(summary.studentCount()).isEqualTo(6);
        assertThat(summary.top5()).hasSize(5);
        assertThat(summary.top5().get(0).displayName()).isEqualTo("Alpha");
    }

    private LeaderboardRow row(String name, Long tasks) {
        return new LeaderboardRow() {
            public String getDisplayName() { return name; }
            public Long getCompletedTasks() { return tasks; }
        };
    }
}
