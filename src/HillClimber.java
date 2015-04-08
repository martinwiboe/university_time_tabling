/**
 * Created by Burak on 08-04-2015.
 */
public class HillClimber extends Heuristic {
	
	public int IterationCount = 0;
	@Override
	public Schedule search(Schedule schedule) {
		startCountdown();
		boolean done = true;
		Schedule currentSchedule = getRandomInitialSolution();
		int currentBestValue  =evaluationFunction(currentSchedule);
		System.out.println("current best val = " + currentBestValue);
		 while(timeoutReached() == false && done == false) {
			 done = true; //Runs while there are still changes being made
	         this.IterationCount++; //Adds to the iteration count
	         
	        }

	        return schedule;
	}
}
