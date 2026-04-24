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

@RestController
@RequestMapping("/api/avatars")
@Tag(name = "Avatar")
public class AvatarController {

    private static final Logger log = LoggerFactory.getLogger(AvatarController.class);
    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get my avatar, creating a default avatar if missing")
    public ResponseEntity<AvatarResponse> getMyAvatar() {
        log.info("[avatar] GET /api/avatars/me");
        return ResponseEntity.ok(avatarService.getMyAvatar());
    }

    @PutMapping("/me")
    @Operation(summary = "Update my avatar")
    public ResponseEntity<AvatarResponse> updateMyAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        log.info("[avatar] PUT /api/avatars/me");
        return ResponseEntity.ok(avatarService.updateMyAvatar(request));
    }

    @GetMapping("/options")
    @Operation(summary = "Get per-student avatar options: available, medal-locked, color picker flag")
    public ResponseEntity<AvatarOptionsResponse> getMyOptions() {
        log.info("[avatar] GET /api/avatars/options");
        return ResponseEntity.ok(avatarService.getMyOptions());
    }

    @GetMapping("/shop")
    @Operation(summary = "Get shop catalogue with purchased status for the current student")
    public ResponseEntity<List<ShopItemDto>> getShop() {
        log.info("[avatar] GET /api/avatars/shop");
        return ResponseEntity.ok(avatarService.getShopItems());
    }

    @PostMapping("/shop/purchase")
    @Operation(summary = "Purchase a shop item")
    public ResponseEntity<Void> purchaseItem(@Valid @RequestBody PurchaseItemRequest request) {
        log.info("[avatar] POST /api/avatars/shop/purchase type={} value={}", request.optionType(), request.optionValue());
        avatarService.purchaseItem(request);
        return ResponseEntity.ok().build();
    }
}
