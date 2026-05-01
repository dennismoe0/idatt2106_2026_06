package no.ntnu.idatt2106.nettdetektivene.model;

/**
 * Represents the approval state of a student's membership in a classroom.
 * <ul>
 *   <li>{@code PENDING} — the student has requested to join but the teacher has not yet acted</li>
 *   <li>{@code APPROVED} — the teacher has accepted the student into the classroom</li>
 *   <li>{@code KICKED} — the teacher has removed the student from the classroom</li>
 * </ul>
 */
public enum ClassroomStudentStatus {
    PENDING,
    APPROVED,
    KICKED
}
