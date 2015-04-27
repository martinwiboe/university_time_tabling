import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import com.opencsv.CSVWriter;

/**
 * Created by Burak on 08-04-2015.
 */
public class StochasticHillClimber extends Heuristic {

	protected int currentValue;
	protected int previousValue  = Integer.MAX_VALUE;

	private Random random = new XORShiftRandom();

	public  StochasticHillClimber() throws IOException {
		super();
		String uuid = UUID.randomUUID().toString();
		f = new FileWriter(this.getClass()+uuid+"iterationValue.csv");
		writer = new CSVWriter(f, ',', CSVWriter.NO_QUOTE_CHARACTER);
	}

	@Override
	public Schedule search(Schedule schedule) throws IOException {
		startCountdown();
		currentValue = evaluationFunction(schedule); // value of the current solution
        deltaState.courseAssignmentCount = getCourseAssignmentCount(schedule);
		String[] result = new String[] { "" + iterationCount, currentValue + "" };
		writer.writeNext(result);
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

                int valueIfThisCourseIsAssigned = valueIfAssigningCourse(schedule, currentValue, day, room, period, courseToAssign);

				if (valueIfThisCourseIsAssigned < currentValue) {
					assignCourse(schedule, day, period, room, courseToAssign);
					currentValue = valueIfThisCourseIsAssigned;
				}
			} else {
				// Maybe we should remove the assigned course?
                int valueIfThisCourseIsRemoved = valueIfRemovingCourse(schedule, currentValue, day, room, period);

				if (valueIfThisCourseIsRemoved < currentValue) {
					removeCourse(schedule, day, period, room);
					currentValue = valueIfThisCourseIsRemoved;
				}
			}
			if((float)Math.abs(previousValue-currentValue)/currentValue >= 0.05 ) {
				result = new String[] { "" + iterationCount, currentValue + "" };
				writer.writeNext(result);
				previousValue = currentValue;
			}


		}

		result = new String[] { "" + iterationCount, currentValue + "" };
		writer.writeNext(result);
		writer.flush();
		f.close();
		return schedule;
	}
}

