import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import com.opencsv.CSVWriter;


public class SimulatedAnnealing extends Heuristic{

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int currentValue;
	protected int previousValue = Integer.MAX_VALUE;
    private double temperature;
    private double tempchange;
    private int deltaval;
    private int day1;
    private int period1;
    private int room1;
    private int day2;
    private int period2;
    private int room2;
    private int bestCourse1;
    private int bestCourse2;
    private Random Rand = new XORShiftRandom();
    
    
	public SimulatedAnnealing(double temperature,double tempchange) throws IOException {
		super();
		this.temperature = temperature;
		this.tempchange = tempchange;
		// write iteration value to a CSV file
		String uuid = UUID.randomUUID().toString();
	     f = new FileWriter(this.getClass()+Float.toString((float) temperature)+Float.toString((float) tempchange)+uuid+"iterationValue.csv");
	     writer = new CSVWriter(f, ',', CSVWriter.NO_QUOTE_CHARACTER);
	}


	@Override
	public Schedule search(Schedule schedule) throws IOException {
		
	    
		System.out.println("Start");
		startCountdown();
        currentValue = evaluationFunction(schedule); // value of the current solution
        deltaState.courseAssignmentCount = getCourseAssignmentCount(schedule);
        String[] result = new String[] { "" + iterationCount, currentValue + "" };
	    writer.writeNext(result);
	    while(!timeoutReached()) {
	    	this.iterationCount++; //Adds to the iteration count
	    	if(iterationCount % 100000 == 0)
	    	System.out.println("Iteation count = " + iterationCount);
	    	day1  = day2 = period1 = period2 = room1 = room2 = 0;
	    	boolean hardConstraintViolation = true;
	    	int valueIfThisCourseIsAssigned  = Integer.MAX_VALUE;
			int valueIfThisCourseIsRemoved  = Integer.MAX_VALUE;
			int valueIfThisCoursesAreSwapped  = Integer.MAX_VALUE;
			int courseId = -1;
	    	while(((day1==day2)&&(period1==period2)&&(room1==room2) || hardConstraintViolation) ) {    		
	    		day1 = Rand.nextInt(this.basicInfo.days);
	    		day2 = Rand.nextInt(this.basicInfo.days);
	    		period1 = Rand.nextInt(this.basicInfo.periodsPerDay);
	    		period2 = Rand.nextInt(this.basicInfo.periodsPerDay);
	    		room1  = Rand.nextInt(this.basicInfo.rooms);
	    		room2  = Rand.nextInt(this.basicInfo.rooms);
	    		valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, currentValue, day1, period1,room1,day2,period2,room2);
	    		valueIfThisCourseIsRemoved  = valueIfRemovingCourse(schedule, currentValue, day1, period1, room1);
	    		courseId = Rand.nextInt(this.basicInfo.courses);
	    		valueIfThisCourseIsAssigned  = valueIfAssigningCourse(schedule, currentValue, day1, period1, room1, courseId);
	    		if(!( valueIfThisCourseIsRemoved == Integer.MAX_VALUE && valueIfThisCourseIsAssigned == Integer.MAX_VALUE))
	    			hardConstraintViolation =  false;
	
	    	}
	    	
	    	Type change;
	    	if(valueIfThisCourseIsRemoved<=valueIfThisCourseIsAssigned){
	    		if(valueIfThisCourseIsRemoved<=valueIfThisCoursesAreSwapped) {
	    			if(valueIfThisCourseIsRemoved != Integer.MAX_VALUE)
	    			 change = Type.REMOVE;
	    			else 
	    				change = Type.NOTHING;
	    		}
	    		else {
	    			 change = Type.SWAP;
	    		}
	    	}
	    	else {
	    		if(valueIfThisCourseIsAssigned<valueIfThisCoursesAreSwapped)
	    			change = Type.ASSIGN;
	    		else 
	    			change = Type.SWAP;
	    	}
	    	switch (change) {
			case REMOVE:{
				deltaval =  valueIfThisCourseIsRemoved - currentValue;
	    		if(deltaval < 0) { 
	    			currentValue  +=deltaval;
					removeCourse(schedule, day1, period1, room1);
	    		}
	    		else if(GetProbability() > Rand.nextDouble()  && deltaval!=0) {
	    			currentValue +=deltaval;
					removeCourse(schedule, day1, period1, room1);
	    		}
				break;
			}
			case ASSIGN: {
				deltaval =  valueIfThisCourseIsAssigned - currentValue;
	    		if(deltaval < 0) { 
	    			currentValue  +=deltaval;
					assignCourse(schedule, day1, period1, room1, courseId);
	    		}
	    		else if(GetProbability() > Rand.nextDouble()  && deltaval!=0) {
	    			currentValue +=deltaval;
	    			assignCourse(schedule, day1, period1, room1, courseId);
	    		}
				break;
			}
			case SWAP: {
				deltaval = valueIfThisCoursesAreSwapped - currentValue;
	    		if(deltaval < 0) { 
	    			currentValue  = valueIfThisCoursesAreSwapped;
	    			bestCourse1 = schedule.assignments[day1][period1][room1];//Remembers the person for the taboolist
					bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the person for the taboolist
					removeCourse(schedule, day2, period2, room2);
					removeCourse(schedule, day1, period1, room1);
					assignCourse(schedule, day2, period2, room2, bestCourse1);
					assignCourse(schedule, day1, period1, room1, bestCourse2);
	    		}
	    		else if(GetProbability() > Rand.nextDouble()  && deltaval!=0 ) {
	    			currentValue  = valueIfThisCoursesAreSwapped;
	    			bestCourse1 = schedule.assignments[day1][period1][room1];//Remembers the person for the taboolist
					bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the person for the taboolist
					removeCourse(schedule, day2, period2, room2);
					removeCourse(schedule, day1, period1, room1);
					assignCourse(schedule, day2, period2, room2, bestCourse1);
					assignCourse(schedule, day1, period1, room1, bestCourse2);
	    		}
				break;
			}

			default:
				break;
			}
	    	
	    	// TODO write results to a CSV file
       
	    		 temperature= temperature*tempchange; //Reduces the temperature
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
	
    private double GetProbability()
    {
        return Math.exp(-deltaval/temperature);
    }

}
