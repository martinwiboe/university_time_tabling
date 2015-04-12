import java.util.Date;
import java.util.Random;



public class SimulatedAnnealing extends Heuristic{

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int IterationCount = 0;
	protected int bestValue;
    private double temperature;
    private double tempchange;
	int deltaval;
	int day1;
	int period1;
	int room1;
	int day2;
	int period2;
	int room2;
    Random Rand = new Random();
    
    
	public SimulatedAnnealing(double temperature,double tempchange) {
		super();
		this.temperature = temperature;
		this.tempchange = tempchange;
	}


	@Override
	public Schedule search(Schedule schedule) {
		
		System.out.println("Start");

		startCountdown();
		Date da=new Date(); //TODO:delete this
		long start_time = da.getTime();
	    long max_sec = start_time+1000*30;
	    da=new Date();
		schedule = new Schedule(this.basicInfo.days,this.basicInfo.periodsPerDay,this.basicInfo.rooms);
		schedule = getRandomInitialSolution();
		currentSchedule = new Schedule(this.basicInfo.days,this.basicInfo.periodsPerDay,this.basicInfo.rooms);
		int currentVal  = evaluationFunction(schedule); //Calculates the start value
		DeepClone(schedule, currentSchedule);//A copy is made so the changes can be reversed
	    while(da.getTime() < max_sec) {
	    	da=new Date(); //Helps keep track of time
	    	this.IterationCount++; //Adds to the iteration count
	    	//System.out.println("Iteation count = " + IterationCount);
	    	day1  = day2 = period1 = period2 = room1 = room2 = 0;
	    	boolean hardConstraintViolation = true;
	    	while(((day1==day2)&&(period1==period2)&&(room1==room2))||hardConstraintViolation) {
	    		
	    		day1 = Rand.nextInt(this.basicInfo.days);
	    		day2 = Rand.nextInt(this.basicInfo.days);
	    		period1 = Rand.nextInt(this.basicInfo.periodsPerDay);
	    		period2 = Rand.nextInt(this.basicInfo.periodsPerDay);
	    		room1  = Rand.nextInt(this.basicInfo.rooms);
	    		room1  = Rand.nextInt(this.basicInfo.rooms);
	    		swapCourse(day1, period1, room1, day2, period2, room2, currentSchedule);
	    		if(!validateSameLecturerConstraint(currentSchedule) || !validateSameCurriculumConstraint(currentSchedule) || !validateAvailabilityConstraint(currentSchedule)) {
					swapCourse(day2, period2, room2, day1, period1, room1, currentSchedule); //hard constrain violated swap again to previous schedule
				}
	    		else {
	    			hardConstraintViolation  = false;	    		
	    			}
	    	}
	    		deltaval = evaluationFunction(currentSchedule) - currentVal;
	    		if(deltaval > 0) { 
	    			currentVal +=deltaval;
	    			DeepClone(currentSchedule,schedule);//The best solution so far is saved
	    		}
	    		else if(GetProbability() > Rand.nextDouble()  && deltaval!=0) {
	    			currentVal +=deltaval;
	    			DeepClone(currentSchedule,schedule);//The best solution so far is saved
	    		}
	    		else {
	    			swapCourse(day2, period2, room2, day1, period1, room1, currentSchedule); //hard constrain violated swap again to previous schedule
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
