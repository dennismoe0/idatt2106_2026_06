package no.ntnu.idatt2106.nettdetektivene.repository;

public interface SchoolLeaderboardRow {
    Long getStudentId();
    String getDisplayName();
    Long getClassroomId();
    String getClassroomName();
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
