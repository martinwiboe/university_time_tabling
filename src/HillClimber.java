/**
 * Created by Burak on 08-04-2015.
 */
public class HillClimber extends Heuristic {

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int IterationCount = 0;
	protected int bestValue;
	@Override
	public Schedule search(Schedule schedule) {
		startCountdown();
		boolean done = false;
		currentSchedule = new Schedule(this.basicInfo.days,this.basicInfo.periodsPerDay,this.basicInfo.rooms);
		int bestValue  = evaluationFunction(schedule); //the bestvalue so far
		DeepClone(schedule, currentSchedule);//A copy is made so the changes can be reversed
	    System.out.println("Start");
		while(timeoutReached() == false && done == false) {
			done = true; //Runs while there are still changes being made
			//System.out.println("Iteration Count = " + IterationCount);
			this.IterationCount++; //Adds to the iteration count
			int currentBestValue  = Integer.MAX_VALUE;
			
			for(int day=0;day<this.basicInfo.days;day++) { //run thorough all the days

				for(int period =  0;period<this.basicInfo.periodsPerDay;period++) { //all the periods

					for(int room = 0;room<this.basicInfo.rooms;room++){ //all the rooms

						for(int day2=0;day2<this.basicInfo.days;day2++) { //all the days can be swap to

							for(int period2 =  0;period2<this.basicInfo.periodsPerDay;period2++) { //all the periods can be swap to

								for(int room2 = 0;room2<this.basicInfo.rooms;room2++){ //all the rooms can be swap to
									//boolean hardConstraintViolation  = false;
									
									
			        				
									
									
									swapCourse(day, period, room, day2, period2, room2, currentSchedule);
									//TODO:check all the hard constraints 
									if(!validateSameLecturerConstraint(currentSchedule) || !validateSameCurriculumConstraint(currentSchedule) || !validateAvailabilityConstraint(currentSchedule)) {
										swapCourse(day2, period2, room2, day, period, room, currentSchedule); //hard constrain violated swap again to previous schedule
										continue;
									}
									int solVal = evaluationFunction(currentSchedule);
									if(solVal < currentBestValue) {
										currentBestValue  =solVal;
										DeepClone(currentSchedule, schedule);
									}
									DeepClone(schedule, currentSchedule);//The currentschedule is being reset for a new swap
								}
								
							}

						}
					}
				}

			}
			
			if(currentBestValue<bestValue) {
				done = false;
				bestValue = currentBestValue;
				DeepClone(currentSchedule, schedule);
			}
		}
		System.out.println("Hill Climber Found A Solution!");
		System.out.println("value  = "+evaluationFunction(schedule));
		return schedule;
	}	
}
