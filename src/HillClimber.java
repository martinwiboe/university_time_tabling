import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.omg.PortableInterceptor.CurrentOperations;

import com.opencsv.CSVWriter;



/**
 * Created by Burak on 08-04-2015.
 */
public class HillClimber extends Heuristic {

	protected Schedule schedule; //current schedule 
	//protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int currentValue;
	private Random Rand = new XORShiftRandom();
	private Map<Integer,Integer> unScheduledCourses;
	
	
	public HillClimber() throws IOException {
		super();
		 f = new FileWriter(this.getClass()+"iterationValue.csv");
	     writer = new CSVWriter(f, ',', CSVWriter.NO_QUOTE_CHARACTER);
	}


	@Override
	public Schedule search(Schedule schedule) throws IOException {
		startCountdown();
		currentValue = evaluationFunction(schedule); // value of the current solution
        courseAssignmentCount = getCourseAssignmentCount(schedule);
		unScheduledCourses = new HashMap<Integer,Integer>();
		for(int courseNo=0;courseNo<this.basicInfo.courses;courseNo++){ //checked the unscheduled course and the number of unscheduled lectures
			int numberOfLecturesUnAssigned = this.courseAssignmentCount[courseNo] - this.courses.numberOfLecturesForCourse[courseNo];
			if(numberOfLecturesUnAssigned != 0)
				unScheduledCourses.put(courseNo, Math.abs(numberOfLecturesUnAssigned));
		}
		
		boolean done = false;
		
	    System.out.println("Start");
		while(!timeoutReached()) { 
			//System.out.println("Iteration Count = " + IterationCount);
			this.iterationCount++; //Adds to the iteration count
			
			for(int day=0;day<this.basicInfo.days;day++) { //run thorough all the days

				for(int period =  0;period<this.basicInfo.periodsPerDay;period++) { //all the periods
						
					for(int room = 0;room<this.basicInfo.rooms;room++){ //all the rooms

							//	System.out.println("day = "+ day +" period = " + period + " room = " + room + " course  = " + schedule.assignments[day][period][room]);
							/*		for (Map.Entry<Integer, Integer> entry : unScheduledCourses.entrySet()){
										int unScheduledCourseNo = entry.getKey();
										int unScheduledCourseValue = entry.getValue();
										if(schedule.assignments[day][period][room] == StochasticHillClimber.EMPTY_ROOM) {// room is empty
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
										//fisrt remove the course in current time-room slot and then we will add our unscheduled course
										
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
											*/
				    	int valueIfThisCourseIsAssigned  = Integer.MAX_VALUE;
						int valueIfThisCourseIsRemoved  = Integer.MAX_VALUE;
						int valueIfThisCoursesAreSwapped  = Integer.MAX_VALUE;
						int day2 =0, period2 =0, room2=0;
						day2  = Rand.nextInt(this.basicInfo.days);
						room2   = Rand.nextInt(this.basicInfo.rooms);
			    		period2  = Rand.nextInt(this.basicInfo.periodsPerDay);
			    		valueIfThisCourseIsRemoved  = valueIfRemovingCourse(schedule, day, room, period); //calculate the value if we remove the course given timeslot
			    		int courseId = Rand.nextInt(this.basicInfo.courses);
			    		valueIfThisCourseIsAssigned  = valueIfAssigningCourse(schedule, day, room, period, courseId); //calculate the value if we swap the courses given timeslots
			    		valueIfThisCoursesAreSwapped  = valueIfSwappingCourses(schedule, day, period, room, day2, period2, room2);//calculate the value if we add the course given timeslot
			    		Type change;
			    		//we have the new values now we need to choose which action would be the best according to new values
				    	if(valueIfThisCourseIsRemoved<=valueIfThisCourseIsAssigned){
				    		if(valueIfThisCourseIsRemoved<=valueIfThisCoursesAreSwapped) {
				    			if(valueIfThisCourseIsRemoved != Integer.MAX_VALUE) 
				    			 change = Type.REMOVE;//if removing the course gives the best value then choose remove
				    			else 
				    				change = Type.NOTHING;//that means we have Max_int value so we do nothing in this iteration
				    		}
				    		else {
				    			 change = Type.SWAP;//choose swap if the swapping gives the best value
				    		}
				    	}
				    	else {
				    		if(valueIfThisCourseIsAssigned<valueIfThisCoursesAreSwapped)
				    			change = Type.ASSIGN; //choose assign if the assigning gives the best value
				    		else 
				    			change = Type.SWAP;//choose swap if the swapping gives the best value
				    	}
				    	
				    	int bestCourse1;
				    	int bestCourse2;
				    	int deltaval;
				    	switch (change) {
						case REMOVE:{ 
							deltaval =  valueIfThisCourseIsRemoved - currentValue;
				    		if(deltaval < 0) { 
				    			currentValue  +=deltaval;
								removeCourse(schedule, day, period, room);
				    		}
							break;
						}
						case ASSIGN: {
							deltaval =  valueIfThisCourseIsAssigned - currentValue;
				    		if(deltaval < 0) { 
				    			currentValue  +=deltaval;
								assignCourse(schedule, day, period, room, courseId);
				    		}
							break;
						}
						case SWAP: {
							deltaval = valueIfThisCoursesAreSwapped - currentValue;
				    		if(deltaval < 0) { 
				    			currentValue  = valueIfThisCoursesAreSwapped;
				    			bestCourse1 = schedule.assignments[day][period][room];//Remembers the course for to assign
								bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the course for to assign
								removeCourse(schedule, day2, period2, room2);
								removeCourse(schedule, day, period, room);
								assignCourse(schedule, day2, period2, room2, bestCourse1);
								assignCourse(schedule, day, period, room, bestCourse2);
				    		}
							break;
						}

						default:
							break;
						}
								}
								
							}
		
				
			}
			
			if(iterationCount%1000 == 0){
	           	 String[] result = new String[] { "" + iterationCount, currentValue + "" };
	       	     writer.writeNext(result);
	            }
	        }
	        writer.flush();
	        f.close();
	        return schedule;
	}	
}
