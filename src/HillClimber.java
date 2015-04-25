import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.omg.PortableInterceptor.CurrentOperations;


/**
 * Created by Burak on 08-04-2015.
 */
public class HillClimber extends Heuristic {

	protected Schedule schedule; //current schedule 
	//protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int IterationCount = 0;
	protected int currentValue;
	private Random rand = new Random();
	private Map<Integer,Integer> unScheduledCourses;
	@Override
	public Schedule search(Schedule schedule) {
		startCountdown();
		unScheduledCourses = new HashMap<Integer,Integer>();
		for(int courseNo=0;courseNo<this.basicInfo.courses;courseNo++){ //checked the unscheduled course and the number of unscheduled lectures
			int numberOfLecturesUnAssigned = this.courseAssignmentCount[courseNo] - this.courses.numberOfLecturesForCourse[courseNo];
			if(numberOfLecturesUnAssigned != 0)
				unScheduledCourses.put(courseNo, Math.abs(numberOfLecturesUnAssigned));
		}
		boolean done = false;
		currentValue  = evaluationFunction(schedule); //the bestvalue so far
	    System.out.println("Start");
		while(timeoutReached() == false) { //TODO: we need to deal with the d
			done = true; //Runs while there are still changes being made
			//System.out.println("Iteration Count = " + IterationCount);
			this.IterationCount++; //Adds to the iteration count
			if(this.IterationCount%100 == 0)
			System.err.println("Iteration Count  = " + this.IterationCount );
			int currentBestValue  = Integer.MAX_VALUE;
			
			for(int day=0;day<this.basicInfo.days;day++) { //run thorough all the days

				for(int period =  0;period<this.basicInfo.periodsPerDay;period++) { //all the periods
						
					for(int room = 0;room<this.basicInfo.rooms;room++){ //all the rooms

							//	System.out.println("day = "+ day +" period = " + period + " room = " + room + " course  = " + schedule.assignments[day][period][room]);
									for (Map.Entry<Integer, Integer> entry : unScheduledCourses.entrySet()){
										int unScheduledCourseNo = entry.getKey();
										int unScheduledCourseValue = entry.getValue();
										if(schedule.assignments[day][period][room] == HillClimberOld.EMPTY_ROOM) {// room is empty
											int valueIfThisCourseIsAssigned = valueIfAssigningCourse(schedule, day, room, period, unScheduledCourseNo);
											if(valueIfThisCourseIsAssigned<=currentValue) {
												assignCourse(schedule, day, period, room, unScheduledCourseNo);
												unScheduledCourseValue--;
												if(unScheduledCourseValue == 0) {
													unScheduledCourses.remove(unScheduledCourseNo);
												}
											}
												
										}
										
										else { //room ýs not epmty first we need to check the 
											
										}
											
										int removedCourseNo = RemoveCourse( day, period, room,schedule);
										//fisrt remove the couse in current time-room slot and then we will add our unscheduled course
										
										if(!AddCourse(unScheduledCourseNo, day, period, room, schedule))
											System.err.println("Add Couse Failed");
										else {
											if(!validateSameLecturerConstraint(schedule) || !validateSameCurriculumConstraint(schedule) || !validateAvailabilityConstraint(schedule)) {
												RemoveCourse(day, period, room, schedule);
												AddCourse(removedCourseNo, day, period, room, schedule);
												continue;//violates the hard constraint so chose another course
											}
											else {
												
											}
										}
										
									}
						/*			int day2 = rand.nextInt(this.basicInfo.days);
									int period2 = rand.nextInt(this.basicInfo.periodsPerDay);
									int room2 = rand.nextInt(this.basicInfo.rooms);
									SwapCourse(day, period, room, day2, period2, room2, schedule);
									//TODO:check all the hard constraints 
									if(!validateSameLecturerConstraint(schedule) || !validateSameCurriculumConstraint(schedule) || !validateAvailabilityConstraint(schedule)) {
										SwapCourse(day2, period2, room2, day, period, room, schedule); //hard constrain violated swap again to previous schedule
										continue;
									}
									int solVal = evaluationFunction(schedule);
									if(solVal < currentBestValue) {
										currentBestValue  =solVal;
									}
									else 
										SwapCourse(day2, period2, room2, day, period, room, schedule); //hard constrain violated swap again to previous schedule
								*/
								}
								
							}
				
			}
			
			if(currentBestValue<currentValue) {
				done = false;
				currentValue = currentBestValue;
			}
		}
		System.out.println("Hill Climber Found A Solution!");
		System.out.println("value  = "+evaluationFunction(schedule));
		return schedule;
	}	
}
