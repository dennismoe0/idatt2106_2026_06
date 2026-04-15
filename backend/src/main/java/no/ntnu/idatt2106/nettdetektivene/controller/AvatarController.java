package no.ntnu.idatt2106.nettdetektivene.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.AvatarResponse;
import no.ntnu.idatt2106.nettdetektivene.dto.avatar.UpdateAvatarRequest;
import no.ntnu.idatt2106.nettdetektivene.service.AvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AvatarController {

    private final AvatarService avatarService;

    @GetMapping("/me")
    @Operation(summary = "Get my avatar, creating a default avatar if missing")
    public ResponseEntity<AvatarResponse> getMyAvatar() {
        return ResponseEntity.ok(avatarService.getMyAvatar());
    }

    @PutMapping("/me")
    @Operation(summary = "Update my avatar")
    public ResponseEntity<AvatarResponse> updateMyAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        return ResponseEntity.ok(avatarService.updateMyAvatar(request));
    }

    @GetMapping("/options")
    @Operation(summary = "Get available avatar customization options")
    public ResponseEntity<Map<String, List<String>>> getAvatarOptions() {
        return ResponseEntity.ok(avatarService.getOptions());
    }
}
