package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.constants.AvatarMedalRewards;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarOptionsResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.MedalLockedItem;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.ShopItemDto;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.PurchaseItemRequest;
import no.ntnu.idatt2106.nettdetektivene.entity.Avatar;
import no.ntnu.idatt2106.nettdetektivene.entity.AvatarShopItem;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.UnlockedAvatarOption;
import no.ntnu.idatt2106.nettdetektivene.entity.User;
import no.ntnu.idatt2106.nettdetektivene.exception.ResourceNotFoundException;
import no.ntnu.idatt2106.nettdetektivene.repository.AvatarRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.AvatarShopItemRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UnlockedAvatarOptionRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    // Full catalogue — used only by getOptions() for reference/validation baseline
    private static final Map<String, List<String>> AVATAR_OPTIONS = createAvatarOptions();

    // Default items: free from the start. All hair styles, outfits, colors, etc. are defaults — only accessory is reduced to "none" (badge/glasses/magnifier/hat are medal rewards).
    private static final Map<String, List<String>> DEFAULT_AVATAR_OPTIONS = createDefaultAvatarOptions();

    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;
    private final UnlockedAvatarOptionRepository unlockedAvatarOptionRepository;
    private final AvatarShopItemRepository avatarShopItemRepository;
    private final StudentMedalRepository studentMedalRepository;
    private final StopRepository stopRepository;

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

        // Always-default fields: validate against global list
        validateDefaultField("gender", request.gender());
        validateDefaultField("eyeColor", request.eyeColor());
        validateDefaultField("eyeStyle", request.eyeStyle());
        validateDefaultField("skinColor", request.skinColor());
        validateDefaultField("outfitColor", request.outfitColor());
        // Fields that can be unlocked via progression
        validateAvailableField(userId, "hairColor", request.hairColor());
        validateAvailableField(userId, "hairStyle", request.hairStyle());
        validateAvailableField(userId, "outfit", request.outfit());
        validateAvailableField(userId, "accessory", request.accessory());
        // hatColor is legacy — still validate from full list
        validateDefaultField("hatColor", request.hatColor());

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

    @Transactional(readOnly = true)
    public AvatarOptionsResponse getMyOptions() {
        Long userId = currentUserId();
        log.info("[avatar] getMyOptions for user {}", userId);

        // Start with defaults
        Map<String, List<String>> available = new LinkedHashMap<>();
        DEFAULT_AVATAR_OPTIONS.forEach((k, v) -> available.put(k, new ArrayList<>(v)));

        // Add earned/purchased unlocks
        List<UnlockedAvatarOption> unlocked = unlockedAvatarOptionRepository.findByStudentId(userId);
        for (UnlockedAvatarOption u : unlocked) {
            available.computeIfAbsent(u.getOptionType(), k -> new ArrayList<>())
                     .add(u.getOptionValue());
        }

        // Build Set<"type:value"> of available items for fast lookup
        Set<String> availableSet = available.entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(v -> e.getKey() + ":" + v))
            .collect(Collectors.toSet());

        // Build medalLocked: rewards not yet in available
        Map<Integer, String> stopNames = stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .collect(Collectors.toMap(Stop::getOrderIndex, Stop::getName));

        List<MedalLockedItem> medalLocked = new ArrayList<>();
        AvatarMedalRewards.BY_ORDER.forEach((order, reward) -> {
            String key = reward.optionType() + ":" + reward.optionValue();
            if (!availableSet.contains(key)) {
                String stopName = stopNames.getOrDefault(order, "Ukjent bane");
                medalLocked.add(new MedalLockedItem(
                    reward.optionType(), reward.optionValue(), (long) order, stopName));
            }
        });

        boolean colorPickerUnlocked = studentMedalRepository.countByStudent_Id(userId) >= 7;

        return new AvatarOptionsResponse(
            Collections.unmodifiableMap(available),
            medalLocked,
            colorPickerUnlocked
        );
    }

    @Transactional
    public void handleMedalUnlock(Long studentId, Long stopId) {
        Stop stop = stopRepository.findById(stopId).orElse(null);
        if (stop == null) {
            log.warn("[avatar] handleMedalUnlock: stop {} not found", stopId);
            return;
        }
        AvatarMedalRewards.AvatarReward reward = AvatarMedalRewards.BY_ORDER.get(stop.getOrderIndex());
        if (reward == null) {
            log.info("[avatar] handleMedalUnlock: no reward for stop orderIndex={}", stop.getOrderIndex());
            return;
        }
        boolean alreadyHas = unlockedAvatarOptionRepository
            .existsByStudentIdAndOptionTypeAndOptionValue(studentId, reward.optionType(), reward.optionValue());
        if (alreadyHas) {
            log.info("[avatar] handleMedalUnlock: student {} already has {}/{}", studentId, reward.optionType(), reward.optionValue());
            return;
        }
        UnlockedAvatarOption row = new UnlockedAvatarOption();
        row.setStudentId(studentId);
        row.setOptionType(reward.optionType());
        row.setOptionValue(reward.optionValue());
        row.setSource(UnlockedAvatarOption.Source.MEDAL);
        row.setStopId(stopId);
        unlockedAvatarOptionRepository.save(row);
        log.info("[avatar] handleMedalUnlock: unlocked {}/{} for student {} via stop {}",
            reward.optionType(), reward.optionValue(), studentId, stopId);
    }

    @Transactional(readOnly = true)
    public List<ShopItemDto> getShopItems() {
        Long userId = currentUserId();
        log.info("[avatar] getShopItems for user {}", userId);

        Set<String> ownedKeys = unlockedAvatarOptionRepository.findByStudentId(userId).stream()
            .filter(u -> u.getSource() == UnlockedAvatarOption.Source.PURCHASE)
            .map(u -> u.getOptionType() + ":" + u.getOptionValue())
            .collect(Collectors.toSet());

        return avatarShopItemRepository.findAllByOrderByDisplayOrderAsc().stream()
            .map(item -> new ShopItemDto(
                item.getId(),
                item.getOptionType(),
                item.getOptionValue(),
                item.getStarPrice(),
                ownedKeys.contains(item.getOptionType() + ":" + item.getOptionValue())
            ))
            .toList();
    }

    @Transactional
    public void purchaseItem(PurchaseItemRequest request) {
        Long userId = currentUserId();
        log.info("[avatar] purchaseItem user={} type={} value={}", userId, request.optionType(), request.optionValue());

        AvatarShopItem item = avatarShopItemRepository
            .findByOptionTypeAndOptionValue(request.optionType(), request.optionValue())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop item not found"));

        boolean alreadyOwned = unlockedAvatarOptionRepository
            .existsByStudentIdAndOptionTypeAndOptionValue(userId, request.optionType(), request.optionValue());
        if (alreadyOwned) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already owned");
        }

        // TODO: add pessimistic locking or a conditional-update query to prevent concurrent double-spend
        User user = getAuthenticatedUser(userId);
        if (user.getStarBalance() < item.getStarPrice()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                "Not enough stars: need %d, have %d".formatted(item.getStarPrice(), user.getStarBalance()));
        }

        user.setStarBalance(user.getStarBalance() - item.getStarPrice());
        userRepository.save(user);

        UnlockedAvatarOption unlock = new UnlockedAvatarOption();
        unlock.setStudentId(userId);
        unlock.setOptionType(request.optionType());
        unlock.setOptionValue(request.optionValue());
        unlock.setSource(UnlockedAvatarOption.Source.PURCHASE);
        unlockedAvatarOptionRepository.save(unlock);

        log.info("[avatar] purchaseItem success user={} item={}/{} cost={}",
            userId, request.optionType(), request.optionValue(), item.getStarPrice());
    }

    public Map<String, List<String>> getOptions() {
        log.info("[avatar] getOptions");
        return AVATAR_OPTIONS;
    }

    // --- private helpers ---

    private void validateDefaultField(String key, String value) {
        if (value == null) return;
        List<String> allowed = AVATAR_OPTIONS.get(key);
        if (allowed != null && !allowed.contains(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid value '%s' for field '%s'".formatted(value, key));
        }
    }

    private void validateAvailableField(Long studentId, String optionType, String optionValue) {
        if (optionValue == null) return;
        // In defaults? OK.
        List<String> defaults = DEFAULT_AVATAR_OPTIONS.get(optionType);
        if (defaults != null && defaults.contains(optionValue)) return;
        // Color picker: any valid hex allowed for hairColor when all medals done
        if ("hairColor".equals(optionType)
                && optionValue.matches("^#[0-9a-fA-F]{6}$")
                && studentMedalRepository.countByStudent_Id(studentId) >= 7) {
            return;
        }
        // Earned or purchased unlock?
        if (!unlockedAvatarOptionRepository.existsByStudentIdAndOptionTypeAndOptionValue(
                studentId, optionType, optionValue)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Value '%s' is locked for field '%s'".formatted(optionValue, optionType));
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
        } catch (NumberFormatException e) {
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
        avatar.setAccessory("none");
        return avatarRepository.save(avatar);
    }

    private static Map<String, List<String>> createAvatarOptions() {
        Map<String, List<String>> options = new LinkedHashMap<>();
        options.put("gender", List.of("neutral", "female", "male"));
        options.put("eyeColor", List.of("#4a3000","#1e40af","#15803d","#6b7280","#92400e"));
        options.put("eyeStyle", List.of("round", "narrow", "wide"));
        options.put("skinColor", List.of("#FDDBB4","#EDB98A","#D08B5B","#AE5D29","#694D3D","#3B1F0E"));
        options.put("hairColor", List.of("#1a1a1a","#8B4513","#D2691E","#F4D150","#E8E1E1","#CC2200","#FF69B4","#9B59B6"));
        options.put("hairStyle", List.of("short","long","curly","ponytail","buzz","braids","bun","afro","bald"));
        options.put("outfit", List.of("detective-coat","hoodie","sweater","uniform","raincoat","cyber-suit"));
        options.put("outfitColor", List.of("#2563eb","#dc2626","#16a34a","#d97706","#1f2937"));
        options.put("hatColor", List.of("none","black","brown","red"));
        options.put("accessory", List.of("none","badge","glasses","magnifier","hat"));
        return Collections.unmodifiableMap(options);
    }

    private static Map<String, List<String>> createDefaultAvatarOptions() {
        Map<String, List<String>> options = new LinkedHashMap<>();
        options.put("gender", List.of("neutral", "female", "male"));
        options.put("eyeColor", List.of("#4a3000","#1e40af","#15803d","#6b7280","#92400e"));
        options.put("eyeStyle", List.of("round", "narrow", "wide"));
        options.put("skinColor", List.of("#FDDBB4","#EDB98A","#D08B5B","#AE5D29","#694D3D","#3B1F0E"));
        options.put("hairColor", List.of("#1a1a1a","#8B4513","#D2691E","#F4D150","#E8E1E1","#CC2200","#FF69B4","#9B59B6"));
        options.put("hairStyle", List.of("short","long","curly","ponytail","buzz","braids","bun","afro","bald"));
        options.put("outfit", List.of("detective-coat","hoodie","sweater","uniform","raincoat"));
        options.put("outfitColor", List.of("#2563eb","#dc2626","#16a34a","#d97706","#1f2937"));
        options.put("hatColor", List.of("none","black","brown","red"));
        options.put("accessory", List.of("none")); // only "none" is default; badge/glasses/magnifier/hat are medal rewards
        return Collections.unmodifiableMap(options);
    }

    private AvatarResponse toResponse(Avatar avatar) {
        return new AvatarResponse(
            avatar.getGender(), avatar.getEyeColor(), avatar.getEyeStyle(),
            avatar.getSkinColor(), avatar.getHairColor(), avatar.getHairStyle(),
            avatar.getOutfit(), avatar.getOutfitColor(), avatar.getHatColor(), avatar.getAccessory()
        );
    }
}
