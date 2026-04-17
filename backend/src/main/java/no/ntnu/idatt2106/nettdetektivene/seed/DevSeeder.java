package no.ntnu.idatt2106.nettdetektivene.seed;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.entity.*;
import no.ntnu.idatt2106.nettdetektivene.model.ClassroomStudentStatus;
import no.ntnu.idatt2106.nettdetektivene.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DevSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeeder.class);

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        wipe();
        seed();
    }

    private void wipe() {
        for (String code : List.of("dataing-2", "norsk-3")) {
            classroomRepository.findByJoinCode(code).ifPresent(c -> {
                classroomRepository.delete(c);
                log.info("[DevSeeder] Deleted classroom: {}", code);
            });
        }
        for (String email : List.of(
            "grethe@teacher.no", "ali@teacher.no",
            "dennis@student.local", "shakti@student.local", "kristian@student.local",
            "oliver@student.local", "kasper@student.local", "ola@student.local",
            "christian@student.local", "aleksander@student.local", "mia@student.local",
            "nora@student.local", "lars@student.local", "emma@student.local"
        )) {
            userRepository.findByEmail(email).ifPresent(u -> {
                userRepository.delete(u);
                log.info("[DevSeeder] Deleted user: {}", email);
            });
        }
        for (String code : List.of("skole-a", "skole-b")) {
            schoolRepository.findByJoinCode(code).ifPresent(s -> {
                schoolRepository.delete(s);
                log.info("[DevSeeder] Deleted school: {}", code);
            });
        }
    }

    private void seed() {
        School skoleA = createSchool("Nettdetektiv videregående skole", "skole-a");
        School skoleB = createSchool("Detektiv ungdomsskole",           "skole-b");

        User grethe = createTeacher("grethe@teacher.no", skoleA);
        User ali    = createTeacher("ali@teacher.no",    skoleB);

        Classroom c1 = createClassroom("DATAING 2. klasse", "dataing-2", grethe, skoleA);
        Classroom c2 = createClassroom("Norsk 3. klasse",   "norsk-3",   ali,    skoleB);

        for (String[] s : List.of(
            new String[]{"dennis",    "Dennis"},
            new String[]{"shakti",    "Shakti"},
            new String[]{"kristian",  "Kristian"},
            new String[]{"oliver",    "Oliver"},
            new String[]{"kasper",    "Kasper"},
            new String[]{"ola",       "Ola"},
            new String[]{"christian", "Christian"}
        )) {
            enroll(createStudent(s[0], skoleA), c1, s[1]);
        }

        for (String[] s : List.of(
            new String[]{"aleksander", "Aleksander"},
            new String[]{"mia",        "Mia"},
            new String[]{"nora",       "Nora"},
            new String[]{"lars",       "Lars"},
            new String[]{"emma",       "Emma"}
        )) {
            enroll(createStudent(s[0], skoleB), c2, s[1]);
        }

        log.info("[DevSeeder] Seeded: 2 schools, 2 teachers, 2 classrooms, 12 students");
    }

    private School createSchool(String name, String joinCode) {
        School s = new School();
        s.setName(name);
        s.setJoinCode(joinCode);
        return schoolRepository.save(s);
    }

    private User createTeacher(String email, School school) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("password123"));
        u.setRole(User.Role.TEACHER);
        u.setSchool(school);
        return userRepository.save(u);
    }

    private User createStudent(String username, School school) {
        User u = new User();
        u.setEmail(username + "@student.local");
        u.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        u.setRole(User.Role.STUDENT);
        u.setSchool(school);
        return userRepository.save(u);
    }

    private Classroom createClassroom(String name, String joinCode, User teacher, School school) {
        Classroom c = new Classroom();
        c.setName(name);
        c.setJoinCode(joinCode);
        c.setActive(true);
        c.setSchool(school);
        classroomRepository.save(c);

        ClassroomTeacher ct = new ClassroomTeacher();
        ct.setClassroom(c);
        ct.setTeacher(teacher);
        classroomTeacherRepository.save(ct);

        return c;
    }

    private void enroll(User student, Classroom classroom, String displayName) {
        ClassroomStudent cs = new ClassroomStudent();
        cs.setClassroom(classroom);
        cs.setStudent(student);
        cs.setDisplayName(displayName);
        cs.setStatus(ClassroomStudentStatus.APPROVED);
        classroomStudentRepository.save(cs);
    }
}
