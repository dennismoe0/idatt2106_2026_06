package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

import java.util.List;
import java.util.Map;

/**
 * Response carrying available avatar customisation options, medal-locked items, and whether the color picker is unlocked for a student.
 */
public record AvatarOptionsResponse(
    Map<String, List<String>> available,
    List<MedalLockedItem> medalLocked,
    boolean colorPickerUnlocked
) {}
