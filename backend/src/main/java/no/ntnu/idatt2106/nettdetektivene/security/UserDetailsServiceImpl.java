package no.ntnu.idatt2106.nettdetektivene.security;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return build(user);
    }

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
