package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.entity.Avatar;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.AvatarRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    private static final Map<String, List<String>> AVATAR_OPTIONS = Map.of(
        "gender", List.of("neutral", "female", "male"),
        "eyeColor", List.of("blue", "brown", "green", "gray"),
        "skinColor", List.of("light", "medium", "dark"),
        "hairColor", List.of("black", "brown", "blonde", "red"),
        "hairStyle", List.of("short", "curly", "ponytail", "buzz"),
        "outfit", List.of("detective-coat", "hoodie", "uniform", "raincoat"),
        "outfitColor", List.of("blue", "red", "green", "yellow"),
        "hatColor", List.of("none", "black", "brown", "red"),
        "accessory", List.of("none", "badge", "glasses", "magnifier")
    );

    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;

    public AvatarResponse getMyAvatar() {
        Long userId = currentUserId();
        log.info("[avatar] getMyAvatar for user {}", userId);

        return avatarRepository.findByStudent_Id(userId)
            .map(this::toResponse)
            .orElseGet(() -> {
                User user = getAuthenticatedUser(userId);
                Avatar defaultAvatar = createDefault(user);
                return toResponse(defaultAvatar);
            });
    }

    public AvatarResponse updateMyAvatar(UpdateAvatarRequest request) {
        Long userId = currentUserId();
        log.info("[avatar] updateMyAvatar for user {}", userId);

        Avatar avatar = avatarRepository.findByStudent_Id(userId)
            .orElseGet(() -> createDefault(getAuthenticatedUser(userId)));

        avatar.setGender(request.gender());
        avatar.setEyeColor(request.eyeColor());
        avatar.setSkinColor(request.skinColor());
        avatar.setHairColor(request.hairColor());
        avatar.setHairStyle(request.hairStyle());
        avatar.setOutfit(request.outfit());
        avatar.setOutfitColor(request.outfitColor());
        avatar.setHatColor(request.hatColor());
        avatar.setAccessory(request.accessory());

        return toResponse(avatarRepository.save(avatar));
    }

    public Map<String, List<String>> getOptions() {
        log.info("[avatar] getOptions");
        return AVATAR_OPTIONS;
    }

    private User getAuthenticatedUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }

    private Avatar createDefault(User user) {
        Avatar avatar = new Avatar();
        avatar.setStudent(user);
        avatar.setGender("neutral");
        avatar.setEyeColor("brown");
        avatar.setSkinColor("medium");
        avatar.setHairColor("brown");
        avatar.setHairStyle("short");
        avatar.setOutfit("detective-coat");
        avatar.setOutfitColor("beige");
        avatar.setHatColor("none");
        avatar.setAccessory("badge");
        return avatarRepository.save(avatar);
    }

    private AvatarResponse toResponse(Avatar avatar) {
        return new AvatarResponse(
            avatar.getGender(),
            avatar.getEyeColor(),
            avatar.getSkinColor(),
            avatar.getHairColor(),
            avatar.getHairStyle(),
            avatar.getOutfit(),
            avatar.getOutfitColor(),
            avatar.getHatColor(),
            avatar.getAccessory()
        );
    }
}
