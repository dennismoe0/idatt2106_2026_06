package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.entity.Avatar;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.AvatarRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock AvatarRepository avatarRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AvatarService avatarService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyAvatar_missingAvatar_createsDefaultAvatar() {
        User user = new User();
        user.setId(7L);
        user.setRole(User.Role.STUDENT);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null, AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(avatarRepository.findByStudent_Id(7L)).thenReturn(Optional.empty());
        when(avatarRepository.save(any(Avatar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = avatarService.getMyAvatar();

        assertThat(response.gender()).isEqualTo("neutral");
        assertThat(response.outfit()).isEqualTo("detective-coat");
        verify(avatarRepository).save(any(Avatar.class));
    }

    @Test
    void updateMyAvatar_updatesExistingAvatar() {
        User user = new User();
        user.setId(7L);
        user.setRole(User.Role.STUDENT);

        Avatar avatar = new Avatar();
        avatar.setStudent(user);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null, AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(avatarRepository.findByStudent_Id(7L)).thenReturn(Optional.of(avatar));
        when(avatarRepository.save(any(Avatar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = avatarService.updateMyAvatar(new UpdateAvatarRequest(
            "female",
            "green",
            "medium",
            "black",
            "curly",
            "hoodie",
            "red",
            "none",
            "badge"
        ));

        assertThat(response.gender()).isEqualTo("female");
        assertThat(response.accessory()).isEqualTo("badge");
        verify(avatarRepository).save(any(Avatar.class));
    }
}