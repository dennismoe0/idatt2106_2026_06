package no.ntnu.idatt2106.nettdetektivene.security;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Spring Security {@link UserDetailsService} implementation that loads users by email or by numeric ID.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by email address.
     *
     * @param email the user's email
     * @return the {@link UserDetails} representation
     * @throws UsernameNotFoundException if no user with the given email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return build(user);
    }

    /**
     * Loads a user by their numeric database ID (used by {@link no.ntnu.idatt2106.nettdetektivene.security.JwtAuthFilter}).
     *
     * @param id the user's database ID
     * @return the {@link UserDetails} representation
     * @throws UsernameNotFoundException if no user with the given ID exists
     */
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        return build(user);
    }

    private UserDetails build(User user) {
        return org.springframework.security.core.userdetails.User.builder()
            .username(String.valueOf(user.getId()))
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .build();
    }
}
