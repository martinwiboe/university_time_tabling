import java.util.Date;
import java.util.Random;



public class SimulatedAnnealing extends Heuristic{

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int IterationCount = 0;
	protected int currentValue;
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
   
    
    
	public SimulatedAnnealing(double temperature,double tempchange) {
		super();
		this.temperature = temperature;
		this.tempchange = tempchange;
	}


	@Override
	public Schedule search(Schedule schedule) {
		
		System.out.println("Start");
		startCountdown();
        currentValue = evaluationFunction(schedule); // value of the current solution
        deltaState.courseAssignmentCount = getCourseAssignmentCount(schedule);
	    while(!timeoutReached()) {
	    	this.IterationCount++; //Adds to the iteration count
	    	if(IterationCount % 100000 == 0)
	    	System.out.println("Iteation count = " + IterationCount);
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
	    		valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, day1, period1,room1,day2,period2,room2);	
	    		valueIfThisCourseIsRemoved  = valueIfRemovingCourse(schedule, currentValue, day1, room1, period1);
	    		courseId = Rand.nextInt(this.basicInfo.courses);
	    		valueIfThisCourseIsAssigned  = valueIfAssigningCourse(schedule, currentValue, day1, room1, period1, courseId);
	    		if(!(valueIfThisCoursesAreSwapped == Integer.MAX_VALUE && valueIfThisCourseIsRemoved == Integer.MAX_VALUE && valueIfThisCourseIsAssigned == Integer.MAX_VALUE))
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
					removeCourse(schedule, day2, period2, room2);
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
	    	
	    		temperature= temperature*tempchange; //Reduces the temperature
	    
	    	
	}
		return schedule;
	}
	
    private double GetProbability()
    {
        return Math.exp(deltaval/temperature);
    }

}
