package no.ntnu.idatt2106.nettdetektivene.constants;

import java.util.Map;

/**
 * Constant mapping that defines which avatar cosmetic item is unlocked when a
 * student earns the medal for a given stop. The map is keyed by the stop's
 * {@code orderIndex} (1–7). This class is non-instantiable; use {@link #BY_ORDER}
 * directly.
 */
public final class AvatarMedalRewards {

    /**
     * Carries the cosmetic category and value for a single avatar reward.
     *
     * @param optionType  the cosmetic category (e.g. "hairColor", "accessory")
     * @param optionValue the specific value within that category (e.g. "glasses", "#00E5FF")
     */
    public record AvatarReward(String optionType, String optionValue) {}

    /** Maps stop order index (1–7) to the avatar cosmetic unlocked upon earning that stop's medal. */
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
