package no.ntnu.idatt2106.nettdetektivene.repository;

/**
 * JPA projection used by the classroom leaderboard query in
 * {@link ClassroomStudentRepository#getLeaderboard}. Returns the minimum data
 * needed to render the in-classroom ranking.
 */
public interface LeaderboardRow {
    String getDisplayName();
    Long getCompletedTasks();
}
