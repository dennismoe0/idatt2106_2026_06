package no.ntnu.idatt2106.nettdetektivene.constants;

import java.util.Map;

public final class AvatarMedalRewards {

    public record AvatarReward(String optionType, String optionValue) {}

    // key = stop orderIndex (1-7), value = the avatar item awarded when that stop is completed
    public static final Map<Integer, AvatarReward> BY_ORDER = Map.of(
        1, new AvatarReward("accessory", "glasses"),
        2, new AvatarReward("accessory", "badge"),
        3, new AvatarReward("accessory", "magnifier"),
        4, new AvatarReward("hairColor", "#00E5FF"),
        5, new AvatarReward("accessory", "hat"),
        6, new AvatarReward("hairColor", "#FF0099"),
        7, new AvatarReward("outfit",    "cyber-suit")
    );

    private AvatarMedalRewards() {}
}
