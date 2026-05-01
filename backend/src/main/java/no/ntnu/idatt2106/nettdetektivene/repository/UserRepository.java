package no.ntnu.idatt2106.nettdetektivene.repository;

import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.entity.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link User} aggregate root.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address. Used during authentication.
     *
     * @param email the email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Returns all users with the given role.
     *
     * @param role the role to filter by (STUDENT or TEACHER)
     */
    List<User> findByRole(Role role);

    /**
     * Returns all users affiliated with the given school.
     *
     * @param schoolId the school id
     */
    List<User> findBySchool_Id(Long schoolId);
}
