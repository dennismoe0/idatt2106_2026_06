package no.ntnu.idatt2106.nettdetektivene.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.service.AvatarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    @Operation(summary = "Get available avatar customization options")
    public ResponseEntity<Map<String, List<String>>> getAvatarOptions() {
        log.info("[avatar] GET /api/avatars/options");
        return ResponseEntity.ok(avatarService.getOptions());
    }
}
