import java.util.Random;

/**
 * Created by Burak on 08-04-2015.
 */
public class StochasticHillClimber extends Heuristic {

    public static final int EMPTY_ROOM = -1;
    protected int currentValue;

    private Random random = new XORShiftRandom();

	@Override
	public Schedule search(Schedule schedule) {
        startCountdown();
        currentValue = evaluationFunction(schedule); // value of the current solution
        courseAssignmentCount = getCourseAssignmentCount(schedule);

        int rooms = this.basicInfo.rooms;
        int days = this.basicInfo.days;
        int periods = this.basicInfo.periodsPerDay;

        while (!timeoutReached()) {
            this.iterationCount++; //Adds to the iteration count

            // Find a candidate for changing
            int room = random.nextInt(rooms);
            int day = random.nextInt(days);
            int period = random.nextInt(periods);

            // If the room is empty, check the impact of adding a course
            int currentlyAssignedCourse = schedule.assignments[day][period][room];
            if (currentlyAssignedCourse == EMPTY_ROOM) {
                // Find a course to assign
                int courseToAssign = random.nextInt(basicInfo.courses);

                int valueIfThisCourseIsAssigned = valueIfAssigningCourse(schedule, day, room, period, courseToAssign);

                if (valueIfThisCourseIsAssigned < currentValue) {
                    assignCourse(schedule, day, period, room, courseToAssign);
                    currentValue = valueIfThisCourseIsAssigned;
                }
            } else {
                // Maybe we should remove the assigned course?
                int valueIfThisCourseIsRemoved = valueIfRemovingCourse(schedule, day, room, period);

                if (valueIfThisCourseIsRemoved < currentValue) {
                    removeCourse(schedule, day, period, room);
                    currentValue = valueIfThisCourseIsRemoved;
                }
            }
        }

        return schedule;
    }
}

