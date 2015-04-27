import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

import com.opencsv.CSVWriter;


/**
 * Created by Burak on 08-04-2015.
 */
public class HillClimberOld extends Heuristic {

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int currentValue;
	protected int previousValue;
	protected int bestValue  = Integer.MAX_VALUE;
	private int bestCourse1;
	private int bestCourse2;


	public HillClimberOld() throws IOException {
		super();
		String uuid = UUID.randomUUID().toString();
		f = new FileWriter(this.getClass()+uuid+"iterationValue.csv");
		writer = new CSVWriter(f, ',', CSVWriter.NO_QUOTE_CHARACTER);
	}


	@Override
	public Schedule search(Schedule schedule) throws IOException {
		startCountdown();
		currentValue = evaluationFunction(schedule); // value of the current solution
		String[] result = new String[] { "" + iterationCount, currentValue + "" };
		writer.writeNext(result);
		courseAssignmentCount = getCourseAssignmentCount(schedule);
		boolean done = false;
		System.out.println("Start");
		while(timeoutReached() == false) {
			//done = true;
			bestValue = currentValue;
			this.iterationCount++; //Adds to the iteration count
			for(int day=0;day<this.basicInfo.days;day++) { //run thorough all the days

				for(int period =  0;period<this.basicInfo.periodsPerDay;period++) { //all the periods

					for(int room = 0;room<this.basicInfo.rooms;room++){ //all the rooms

						for(int day2=0;day2<this.basicInfo.days;day2++) { //all the days can be swap to

							for(int period2 =  0;period2<this.basicInfo.periodsPerDay;period2++) { //all the periods can be swap to

								for(int room2 = 0;room2<this.basicInfo.rooms;room2++){ //all the rooms can be swap to
									//int valueIfThisCourseIsAssigned  = Integer.MAX_VALUE;
									//int valueIfThisCourseIsRemoved  = Integer.MAX_VALUE;
									int valueIfThisCoursesAreSwapped  = Integer.MAX_VALUE;
									//TODO:also check the values if with emoving and adding methods
									valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, day, period,room, day2, period2, room2);
									if(currentValue>valueIfThisCoursesAreSwapped) {
										currentValue = valueIfThisCoursesAreSwapped;
										bestCourse1 = schedule.assignments[day][period][room];//Remembers the person for the taboolist
										bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the person for the taboolist
										removeCourse(schedule, day2, period2, room2);
										removeCourse(schedule, day, period, room);
										assignCourse(schedule, day2, period2, room2, bestCourse1);
										assignCourse(schedule, day, period, room, bestCourse2);
									}

								}

							}

						}
					}
				}

			}
			if(bestValue>currentValue ) //If there has been found a solution that improves the best, it is saved
			{
				done = false; //If improvements were made, there migth be more, and it runs again

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
		System.out.println("HillClimber Old  Found A Solution!");
		System.out.println("Value  = "+evaluationFunction(schedule));
		return schedule;
	}	

}
