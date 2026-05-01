package no.ntnu.idatt2106.nettdetektivene.repository;

/**
 * JPA projection used by the school-wide leaderboard query in
 * {@link ClassroomStudentRepository#getSchoolLeaderboard}. Returns per-student
 * data including classroom context and all avatar fields needed for visual rendering.
 */
public interface SchoolLeaderboardRow {
    Long getStudentId();
    String getDisplayName();
    Long getClassroomId();
    String getClassroomName();
    String getSchoolName();
    Long getCompletedTasks();
    String getAvatarGender();
    String getAvatarEyeColor();
    String getAvatarEyeStyle();
    String getAvatarSkinColor();
    String getAvatarHairColor();
    String getAvatarHairStyle();
    String getAvatarOutfit();
    String getAvatarOutfitColor();
    String getAvatarHatColor();
    String getAvatarAccessory();
}
