package no.ntnu.idatt2106.nettdetektivene.dto.avatar;

import java.util.List;
import java.util.Map;

public record AvatarOptionsResponse(
    Map<String, List<String>> available,
    List<MedalLockedItem> medalLocked,
    boolean colorPickerUnlocked
) {}
