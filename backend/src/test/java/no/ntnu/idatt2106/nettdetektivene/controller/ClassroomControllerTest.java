package no.ntnu.idatt2106.nettdetektivene.controller;

import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomStudentRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomTeacherRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import no.ntnu.idatt2106.nettdetektivene.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClassroomControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ClassroomRepository classroomRepository;
    @Autowired ClassroomTeacherRepository classroomTeacherRepository;
    @Autowired ClassroomStudentRepository classroomStudentRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void createClassroom_returns201WithJoinCode() throws Exception {
        User teacher = saveUser("teacher-create@test.no", User.Role.TEACHER);
        String token = tokenFor(teacher);

        mockMvc.perform(post("/api/classrooms")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "5A", "description": "Digital detective class" }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("5A"))
            .andExpect(jsonPath("$.joinCode").isString())
            .andExpect(jsonPath("$.description").value("Digital detective class"));
    }

    @Test
    void joinClassroom_validCode_returns200WithClassroomIdAndStatus() throws Exception {
        User teacher = saveUser("teacher-join@test.no", User.Role.TEACHER);
        User student = saveUser("student-join@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "fjord-tiger", teacher);
        String token = tokenFor(student);

        mockMvc.perform(post("/api/classrooms/join")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "code": "fjord-tiger", "displayName": "Agent Nora" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(student.getId()))
            .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$.displayName").value("Agent Nora"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getStudents_returnsStudentList() throws Exception {
        User teacher = saveUser("teacher-students@test.no", User.Role.TEACHER);
        User student = saveUser("student-students@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "skog-ulv", teacher);
        saveClassroomStudent(classroom, student, "Agent Nora", ClassroomStudent.Status.PENDING);
        String token = tokenFor(teacher);

        mockMvc.perform(get("/api/classrooms/{id}/students", classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userId").value(student.getId()))
            .andExpect(jsonPath("$[0].classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$[0].displayName").value("Agent Nora"))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    private User saveUser(String email, User.Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Classroom saveClassroom(String title, String joinCode, User teacher) {
        Classroom classroom = new Classroom();
        classroom.setTitle(title);
        classroom.setJoinCode(joinCode);
        classroom = classroomRepository.save(classroom);

        ClassroomTeacher classroomTeacher = new ClassroomTeacher();
        classroomTeacher.setClassroom(classroom);
        classroomTeacher.setTeacher(teacher);
        classroomTeacherRepository.save(classroomTeacher);

        return classroom;
    }

    private ClassroomStudent saveClassroomStudent(
        Classroom classroom,
        User student,
        String displayName,
        ClassroomStudent.Status status
    ) {
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setClassroom(classroom);
        classroomStudent.setStudent(student);
        classroomStudent.setDisplayName(displayName);
        classroomStudent.setStatus(status);
        return classroomStudentRepository.save(classroomStudent);
    }

    private String tokenFor(User user) {
        return jwtTokenProvider.generateToken(user.getId(), user.getRole().name(), user.getEmail());
    }
}
