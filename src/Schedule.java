/**
 * Created by Martin on 16-03-2015.
 */
public class Schedule {
    public boolean validateRoomOccupancyConstraint() {
        return false;
    }

    /**
     * Checks that courses with the same lecturer is not assigned in same time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateSameLecturerConstraint() {
        return false;
    }

    /**
     * Checks that courses in the same curriculum are not scheduled in the same time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateSameCurriculumConstraint() {
        return false;
    }

    /**
     * Checks that courses are not scheduled in unavailable time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateAvailabilityConstraint() {
        return false;
    }

    /**
     * The array of scheduled courses
     * look up using [day][period][room]
     *
     * Value is -1 if the room is empty
     */
    public int[][][] assignments;


}
