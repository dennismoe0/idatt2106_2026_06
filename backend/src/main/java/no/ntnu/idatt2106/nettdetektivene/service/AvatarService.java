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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    private static final Map<String, List<String>> AVATAR_OPTIONS = createAvatarOptions();

    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;

    @Transactional
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

    @Transactional
    public AvatarResponse updateMyAvatar(UpdateAvatarRequest request) {
        Long userId = currentUserId();
        log.info("[avatar] updateMyAvatar for user {}", userId);

        Avatar avatar = avatarRepository.findByStudent_Id(userId)
            .orElseGet(() -> createDefault(getAuthenticatedUser(userId)));

        validateField("gender", request.gender());
        validateField("eyeColor", request.eyeColor());
        validateField("eyeStyle", request.eyeStyle());
        validateField("skinColor", request.skinColor());
        validateField("hairColor", request.hairColor());
        validateField("hairStyle", request.hairStyle());
        validateField("outfit", request.outfit());
        validateField("outfitColor", request.outfitColor());
        validateField("hatColor", request.hatColor());
        validateField("accessory", request.accessory());

        avatar.setGender(request.gender());
        avatar.setEyeColor(request.eyeColor());
        avatar.setEyeStyle(request.eyeStyle());
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

    private void validateField(String key, String value) {
        List<String> allowedValues = AVATAR_OPTIONS.get(key);
        if (value != null && allowedValues != null && !allowedValues.contains(value)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid value '%s' for field '%s'".formatted(value, key)
            );
        }
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
        avatar.setEyeColor("#4a3000");
        avatar.setEyeStyle("round");
        avatar.setSkinColor("#D08B5B");
        avatar.setHairColor("#8B4513");
        avatar.setHairStyle("short");
        avatar.setOutfit("detective-coat");
        avatar.setOutfitColor("#2563eb");
        avatar.setHatColor("none");
        avatar.setAccessory("badge");
        return avatarRepository.save(avatar);
    }

    private static Map<String, List<String>> createAvatarOptions() {
        Map<String, List<String>> options = new LinkedHashMap<>();
        options.put("gender", List.of("neutral", "female", "male"));
        options.put("eyeColor", List.of("#4a3000","#1e40af","#15803d","#6b7280","#92400e"));
        options.put("eyeStyle", List.of("round", "narrow", "wide"));
        options.put("skinColor", List.of("#FDDBB4","#EDB98A","#D08B5B","#AE5D29","#694D3D","#3B1F0E"));
        options.put("hairColor", List.of("#1a1a1a","#8B4513","#D2691E","#F4D150","#E8E1E1","#CC2200","#FF69B4","#9B59B6"));
        options.put("hairStyle", List.of("short", "long", "curly", "ponytail", "buzz", "braids", "bun", "afro", "bald"));
        options.put("outfit", List.of("detective-coat", "hoodie", "sweater", "uniform", "raincoat"));
        options.put("outfitColor", List.of("#2563eb","#dc2626","#16a34a","#d97706","#1f2937"));
        options.put("hatColor", List.of("none", "black", "brown", "red"));
        options.put("accessory", List.of("none", "badge", "glasses", "magnifier", "hat"));
        return Collections.unmodifiableMap(options);
    }

    private AvatarResponse toResponse(Avatar avatar) {
        return new AvatarResponse(
            avatar.getGender(),
            avatar.getEyeColor(),
            avatar.getEyeStyle(),
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
