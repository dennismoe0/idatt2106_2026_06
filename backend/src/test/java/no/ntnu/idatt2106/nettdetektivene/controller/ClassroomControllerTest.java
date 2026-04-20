package no.ntnu.idatt2106.nettdetektivene.controller;

import no.ntnu.idatt2106.nettdetektivene.entity.Classroom;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomStudent;
import no.ntnu.idatt2106.nettdetektivene.entity.ClassroomTeacher;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void joinClassroom_validCode_returns201WithClassroomIdAndStatus() throws Exception {
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
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(student.getId()))
            .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$.displayName").value("Agent Nora"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void studentCallingTeacherEndpoint_returns403() throws Exception {
        User student = saveUser("student-rbac@test.no", User.Role.STUDENT);
        String token = tokenFor(student);

        mockMvc.perform(get("/api/classrooms")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void getClassroom_otherTeacher_returns404() throws Exception {
        User owner = saveUser("teacher-owner@test.no", User.Role.TEACHER);
        User otherTeacher = saveUser("teacher-other@test.no", User.Role.TEACHER);
        Classroom classroom = saveClassroom("5A", "dal-rev", owner);
        String token = tokenFor(otherTeacher);

        mockMvc.perform(get("/api/classrooms/{id}", classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    void joinClassroom_invalidCode_returns404() throws Exception {
        User student = saveUser("student-invalid-code@test.no", User.Role.STUDENT);
        String token = tokenFor(student);

        mockMvc.perform(post("/api/classrooms/join")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "code": "missing-code", "displayName": "Agent Nora" }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void joinClassroom_duplicateMember_returns409() throws Exception {
        User teacher = saveUser("teacher-duplicate@test.no", User.Role.TEACHER);
        User student = saveUser("student-duplicate@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "li-hare", teacher);
        saveClassroomStudent(classroom, student, "Agent Nora", ClassroomStudentStatus.PENDING);
        String token = tokenFor(student);

        mockMvc.perform(post("/api/classrooms/join")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "code": "li-hare", "displayName": "Agent Nora" }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void getStudents_returnsStudentList() throws Exception {
        User teacher = saveUser("teacher-students@test.no", User.Role.TEACHER);
        User student = saveUser("student-students@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "skog-ulv", teacher);
        saveClassroomStudent(classroom, student, "Agent Nora", ClassroomStudentStatus.PENDING);
        String token = tokenFor(teacher);

        mockMvc.perform(get("/api/classrooms/{id}/students", classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userId").value(student.getId()))
            .andExpect(jsonPath("$[0].classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$[0].displayName").value("Agent Nora"))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void updateStudentStatus_approved_returnsUpdatedStudent() throws Exception {
        User teacher = saveUser("teacher-update@test.no", User.Role.TEACHER);
        User student = saveUser("student-update@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "sol-orn", teacher);
        saveClassroomStudent(classroom, student, "Agent Nora", ClassroomStudentStatus.PENDING);
        String token = tokenFor(teacher);

        mockMvc.perform(put("/api/classrooms/{id}/students/{sid}", classroom.getId(), student.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "APPROVED" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(student.getId()))
            .andExpect(jsonPath("$.classroomId").value(classroom.getId()))
            .andExpect(jsonPath("$.displayName").value("Agent Nora"))
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getMyStatus_studentMember_returnsStatus() throws Exception {
        User teacher = saveUser("teacher-my-status@test.no", User.Role.TEACHER);
        User student = saveUser("student-my-status@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "status-rev", teacher);
        saveClassroomStudent(classroom, student, "Agent Nora", ClassroomStudentStatus.APPROVED);
        String token = tokenFor(student);

        mockMvc.perform(get("/api/classrooms/{id}/my-status", classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getMyStatus_teacherReturns403() throws Exception {
        User teacher = saveUser("teacher-status-forbidden@test.no", User.Role.TEACHER);
        Classroom classroom = saveClassroom("5A", "status-ulv", teacher);
        String token = tokenFor(teacher);

        mockMvc.perform(get("/api/classrooms/{id}/my-status", classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void getMyStatus_studentNotMember_returns404() throws Exception {
        User teacher = saveUser("teacher-status-missing@test.no", User.Role.TEACHER);
        User student = saveUser("student-status-missing@test.no", User.Role.STUDENT);
        Classroom classroom = saveClassroom("5A", "status-hare", teacher);
        String token = tokenFor(student);

        mockMvc.perform(get("/api/classrooms/{id}/my-status", classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Student not in classroom"));
    }

    @Test
    void deleteClassroom_returns204_forOwningTeacher() throws Exception {
        User teacher = saveUser("teacher-del@test.no", User.Role.TEACHER);
        Classroom classroom = saveClassroomForTeacher(teacher, "KlasseToDelete");
        String token = tokenFor(teacher);

        mockMvc.perform(delete("/api/classrooms/" + classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        Classroom updated = classroomRepository.findById(classroom.getId()).orElseThrow();
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    void deleteClassroom_returns404_forNonOwner() throws Exception {
        User owner = saveUser("owner-del@test.no", User.Role.TEACHER);
        User other = saveUser("other-del@test.no", User.Role.TEACHER);
        Classroom classroom = saveClassroomForTeacher(owner, "OwnerClass");
        String token = tokenFor(other);

        mockMvc.perform(delete("/api/classrooms/" + classroom.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    private User saveUser(String email, User.Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Classroom saveClassroom(String name, String joinCode, User teacher) {
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setJoinCode(joinCode);
        classroom = classroomRepository.save(classroom);

        ClassroomTeacher classroomTeacher = new ClassroomTeacher();
        classroomTeacher.setClassroom(classroom);
        classroomTeacher.setTeacher(teacher);
        classroomTeacherRepository.save(classroomTeacher);

        return classroom;
    }

    private Classroom saveClassroomForTeacher(User teacher, String name) {
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setJoinCode(name.toLowerCase().replace(" ", "-") + "-code");
        classroom.setActive(true);
        classroom = classroomRepository.save(classroom);
        ClassroomTeacher ct = new ClassroomTeacher();
        ct.setTeacher(teacher);
        ct.setClassroom(classroom);
        classroomTeacherRepository.save(ct);
        return classroom;
    }

    private ClassroomStudent saveClassroomStudent(
        Classroom classroom,
        User student,
        String displayName,
        ClassroomStudentStatus status
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
