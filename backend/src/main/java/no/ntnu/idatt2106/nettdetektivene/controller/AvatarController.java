package no.ntnu.idatt2106.nettdetektivene.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarOptionsResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.PurchaseItemRequest;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.ShopItemDto;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.service.AvatarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles avatar management for students, including retrieving, updating, and purchasing avatar items.
 */
@RestController
@RequestMapping("/api/avatars")
@Tag(name = "Avatar")
public class AvatarController {

    private static final Logger log = LoggerFactory.getLogger(AvatarController.class);
    private final AvatarService avatarService;

    /**
     * Constructs the controller with the given avatar service.
     *
     * @param avatarService the service handling avatar business logic
     */
    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    /**
     * Returns the authenticated student's avatar, creating a default one if it does not yet exist.
     *
     * @return 200 OK with the student's {@link AvatarResponse}
     */
    @GetMapping("/me")
    @Operation(summary = "Get my avatar, creating a default avatar if missing")
    public ResponseEntity<AvatarResponse> getMyAvatar() {
        log.info("[avatar] GET /api/avatars/me");
        return ResponseEntity.ok(avatarService.getMyAvatar());
    }

    /**
     * Updates the authenticated student's avatar with the supplied configuration.
     *
     * @param request the new avatar configuration to apply
     * @return 200 OK with the updated {@link AvatarResponse}
     */
    @PutMapping("/me")
    @Operation(summary = "Update my avatar")
    public ResponseEntity<AvatarResponse> updateMyAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        log.info("[avatar] PUT /api/avatars/me");
        return ResponseEntity.ok(avatarService.updateMyAvatar(request));
    }

    /**
     * Returns the avatar options available to the authenticated student, including unlocked items,
     * medal-locked items, and whether a color picker is enabled.
     *
     * @return 200 OK with the student's {@link AvatarOptionsResponse}
     */
    @GetMapping("/options")
    @Operation(summary = "Get per-student avatar options: available, medal-locked, color picker flag")
    public ResponseEntity<AvatarOptionsResponse> getMyOptions() {
        log.info("[avatar] GET /api/avatars/options");
        return ResponseEntity.ok(avatarService.getMyOptions());
    }

    /**
     * Returns the full shop catalogue with purchased status for each item for the authenticated student.
     *
     * @return 200 OK with a list of {@link ShopItemDto}
     */
    @GetMapping("/shop")
    @Operation(summary = "Get shop catalogue with purchased status for the current student")
    public ResponseEntity<List<ShopItemDto>> getShop() {
        log.info("[avatar] GET /api/avatars/shop");
        return ResponseEntity.ok(avatarService.getShopItems());
    }

    /**
     * Purchases a shop item for the authenticated student.
     *
     * @param request the item to purchase, identified by option type and value
     * @return 200 OK with no body on success
     */
    @PostMapping("/shop/purchase")
    @Operation(summary = "Purchase a shop item")
    public ResponseEntity<Void> purchaseItem(@Valid @RequestBody PurchaseItemRequest request) {
        log.info("[avatar] POST /api/avatars/shop/purchase type={} value={}", request.optionType(), request.optionValue());
        avatarService.purchaseItem(request);
        return ResponseEntity.ok().build();
    }
}
