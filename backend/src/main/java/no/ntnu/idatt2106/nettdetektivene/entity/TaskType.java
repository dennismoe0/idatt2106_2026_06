package no.ntnu.idatt2106.nettdetektivene.entity;

/**
 * Enumerates the distinct task formats used across the seven game stops.
 * The frontend uses this value to select the correct interactive component.
 * {@code LEARN} is a non-scored instructional task; {@code CLUE_RIDDLE} presents
 * the mystery clue; {@code FINAL_BOSS} combines multiple types at the final stop.
 */
public enum TaskType {
    LEARN,
    FAKE_NEWS,
    PHISHING_EMAIL,
    AI_PHOTO,
    PASSWORD,
    MARKETPLACE,
    SOCIAL_MEDIA,
    CLUE_RIDDLE,
    FINAL_BOSS
}
