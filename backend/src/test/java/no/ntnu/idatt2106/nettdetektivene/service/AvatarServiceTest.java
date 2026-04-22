package no.ntnu.idatt2106.nettdetektivene.service;

import no.ntnu.idatt2106.nettdetektivene.dto.avatar.PurchaseItemRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.entity.Avatar;
import no.ntnu.idatt2106.nettdetektivene.entity.AvatarShopItem;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.UnlockedAvatarOption;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.repository.AvatarRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.AvatarShopItemRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UnlockedAvatarOptionRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock AvatarRepository avatarRepository;
    @Mock UserRepository userRepository;
    @Mock UnlockedAvatarOptionRepository unlockedAvatarOptionRepository;
    @Mock AvatarShopItemRepository avatarShopItemRepository;
    @Mock StudentMedalRepository studentMedalRepository;
    @Mock StopRepository stopRepository;
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
        assertThat(response.outfitColor()).isEqualTo("#2563eb");
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

        when(avatarRepository.findByStudent_Id(7L)).thenReturn(Optional.of(avatar));
        when(avatarRepository.save(any(Avatar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = avatarService.updateMyAvatar(new UpdateAvatarRequest(
            "female",
            "#15803d",
            "round",
            "#D08B5B",
            "#1a1a1a",
            "curly",
            "hoodie",
            "#2563eb",
            "none",
            "none"
        ));

        assertThat(response.gender()).isEqualTo("female");
        assertThat(response.accessory()).isEqualTo("none");
        verify(avatarRepository).save(any(Avatar.class));
    }

    @Test
    void updateMyAvatar_invalidFieldValue_returns400() {
        User user = new User();
        user.setId(7L);
        user.setRole(User.Role.STUDENT);

        Avatar avatar = new Avatar();
        avatar.setStudent(user);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null, AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        when(avatarRepository.findByStudent_Id(7L)).thenReturn(Optional.of(avatar));

        assertThatThrownBy(() -> avatarService.updateMyAvatar(new UpdateAvatarRequest(
            "toaster",
            "#15803d",
            "round",
            "#D08B5B",
            "#1a1a1a",
            "curly",
            "hoodie",
            "#2563eb",
            "none",
            "none"
        )))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getMyAvatar_unauthenticated_throws401() {
        assertThatThrownBy(() -> avatarService.getMyAvatar())
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void getOptions_returnsStableConfiguredOptions() {
        var options = avatarService.getOptions();

        assertThat(options).containsKeys("gender", "eyeColor", "eyeStyle", "skinColor", "hairColor");
        assertThat(options.get("outfitColor")).containsExactly("#2563eb","#dc2626","#16a34a","#d97706","#1f2937");
        assertThat(options.get("eyeStyle")).containsExactly("round", "narrow", "wide");
        assertThat(options.get("gender")).containsExactly("neutral", "female", "male");
    }

    @Test
    void getMyOptions_noUnlocks_defaultsAvailableAllMedalLocked() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null,
                AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        when(unlockedAvatarOptionRepository.findByStudentId(7L)).thenReturn(List.of());
        when(studentMedalRepository.countByStudent_Id(7L)).thenReturn(0L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(buildStops());

        var response = avatarService.getMyOptions();

        assertThat(response.available().get("accessory")).containsExactly("none");
        assertThat(response.medalLocked()).hasSize(7);
        assertThat(response.colorPickerUnlocked()).isFalse();
        assertThat(response.available().get("hairStyle"))
            .containsExactlyInAnyOrder("short","long","curly","ponytail","buzz","braids","bun","afro","bald");
    }

    @Test
    void getMyOptions_withMedalUnlock_itemMovesFromLockedToAvailable() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null,
                AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        UnlockedAvatarOption unlock = new UnlockedAvatarOption();
        unlock.setStudentId(7L);
        unlock.setOptionType("accessory");
        unlock.setOptionValue("glasses");
        unlock.setSource(UnlockedAvatarOption.Source.MEDAL);

        when(unlockedAvatarOptionRepository.findByStudentId(7L)).thenReturn(List.of(unlock));
        when(studentMedalRepository.countByStudent_Id(7L)).thenReturn(1L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(buildStops());

        var response = avatarService.getMyOptions();

        assertThat(response.available().get("accessory")).contains("none", "glasses");
        assertThat(response.medalLocked()).hasSize(6);
        assertThat(response.colorPickerUnlocked()).isFalse();
    }

    @Test
    void getMyOptions_allMedals_colorPickerUnlocked() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null,
                AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        when(unlockedAvatarOptionRepository.findByStudentId(7L)).thenReturn(List.of());
        when(studentMedalRepository.countByStudent_Id(7L)).thenReturn(7L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(buildStops());

        var response = avatarService.getMyOptions();

        assertThat(response.colorPickerUnlocked()).isTrue();
    }

    @Test
    void handleMedalUnlock_validStop_insertsUnlockRow() {
        Stop stop = new Stop();
        stop.setId(1L);
        stop.setOrderIndex(1);
        stop.setName("Nyhetskvartalet");

        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(unlockedAvatarOptionRepository
            .existsByStudentIdAndOptionTypeAndOptionValue(7L, "accessory", "glasses"))
            .thenReturn(false);
        when(unlockedAvatarOptionRepository.save(any(UnlockedAvatarOption.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        avatarService.handleMedalUnlock(7L, 1L);

        ArgumentCaptor<UnlockedAvatarOption> captor = ArgumentCaptor.forClass(UnlockedAvatarOption.class);
        verify(unlockedAvatarOptionRepository).save(captor.capture());
        assertThat(captor.getValue().getOptionType()).isEqualTo("accessory");
        assertThat(captor.getValue().getOptionValue()).isEqualTo("glasses");
        assertThat(captor.getValue().getSource()).isEqualTo(UnlockedAvatarOption.Source.MEDAL);
    }

    @Test
    void handleMedalUnlock_alreadyUnlocked_doesNotInsertDuplicate() {
        Stop stop = new Stop();
        stop.setId(1L);
        stop.setOrderIndex(1);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(unlockedAvatarOptionRepository
            .existsByStudentIdAndOptionTypeAndOptionValue(7L, "accessory", "glasses"))
            .thenReturn(true);

        avatarService.handleMedalUnlock(7L, 1L);

        verify(unlockedAvatarOptionRepository, never()).save(any());
    }

    @Test
    void purchaseItem_sufficientStars_deductsStarsAndInsertsUnlock() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null,
                AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        AvatarShopItem item = new AvatarShopItem();
        item.setOptionType("hairColor");
        item.setOptionValue("#CCFF00");
        item.setStarPrice(1);

        User user = new User();
        user.setId(7L);
        user.setStarBalance(5);

        when(avatarShopItemRepository.findByOptionTypeAndOptionValue("hairColor", "#CCFF00"))
            .thenReturn(Optional.of(item));
        when(unlockedAvatarOptionRepository
            .existsByStudentIdAndOptionTypeAndOptionValue(7L, "hairColor", "#CCFF00"))
            .thenReturn(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(unlockedAvatarOptionRepository.save(any(UnlockedAvatarOption.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        avatarService.purchaseItem(new PurchaseItemRequest("hairColor", "#CCFF00"));

        assertThat(user.getStarBalance()).isEqualTo(4);
        verify(unlockedAvatarOptionRepository).save(any(UnlockedAvatarOption.class));
    }

    @Test
    void purchaseItem_insufficientStars_throwsPaymentRequired() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null,
                AuthorityUtils.createAuthorityList("ROLE_STUDENT"))
        );

        AvatarShopItem item = new AvatarShopItem();
        item.setOptionType("hairStyle");
        item.setOptionValue("wavy");
        item.setStarPrice(3);

        User user = new User();
        user.setId(7L);
        user.setStarBalance(1);

        when(avatarShopItemRepository.findByOptionTypeAndOptionValue("hairStyle", "wavy"))
            .thenReturn(Optional.of(item));
        when(unlockedAvatarOptionRepository
            .existsByStudentIdAndOptionTypeAndOptionValue(7L, "hairStyle", "wavy"))
            .thenReturn(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
            avatarService.purchaseItem(new PurchaseItemRequest("hairStyle", "wavy")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.PAYMENT_REQUIRED));
    }

    // Helper method
    private List<Stop> buildStops() {
        var stops = new ArrayList<Stop>();
        String[] names = {"Nyhetskvartalet","Postkontoret","Fotografen","Passordbanken",
                          "Markedsplassen","Den sosiale møteplassen","Datasenteret"};
        for (int i = 1; i <= 7; i++) {
            Stop s = new Stop();
            s.setId((long) i);
            s.setOrderIndex(i);
            s.setName(names[i - 1]);
            stops.add(s);
        }
        return stops;
    }
}
