import java.io.IOException;
import java.util.Random;
import java.util.Vector;


/**
 * Created by Burak on 08-04-2015.
 */
public class StochasticTABU extends Heuristic {

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int IterationCount = 0;
	protected int currentValue;
	private int tabooLength;
	private int bestCourse1;
	private int bestCourse2;
	private Random random = new XORShiftRandom();
	protected Vector<Integer> tabooList1; //The first taboolist - ONLY TABOOSEARCH
	protected Vector<Integer> tabooList2; //The second taboolist - ONLY TABOOSEARCH
	public StochasticTABU(int TabooLength) throws IOException
	{
		this.tabooLength = TabooLength;
		tabooList1  = new Vector<Integer>();
		tabooList2  = new Vector<Integer>();

	}

	@Override
	public Schedule search(Schedule schedule) {
		startCountdown();
		currentValue = evaluationFunction(schedule); // value of the current solution
		courseAssignmentCount = getCourseAssignmentCount(schedule);
		int rooms = this.basicInfo.rooms;
		int days = this.basicInfo.days;
		int periods = this.basicInfo.periodsPerDay;
		System.out.println("Start");
		while(timeoutReached() == false) {

			this.IterationCount++; //Adds to the iteration count
			if(IterationCount%100000 ==0)
			System.out.println("Iteration Count = " + IterationCount);
			int room = random.nextInt(rooms);
			int day = random.nextInt(days);
			int period = random.nextInt(periods);
			int room2 = random.nextInt(rooms);
			int day2 = random.nextInt(days);
			int period2 = random.nextInt(periods);
			//int valueIfThisCourseIsAssigned  = Integer.MAX_VALUE;
			//int valueIfThisCourseIsRemoved  = Integer.MAX_VALUE;
			int valueIfThisCoursesAreSwapped  = Integer.MAX_VALUE;
			//TODO:also check the values if with emoving and adding methods
			valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, day, period,room, day2, period2, room2);
			if(currentValue>valueIfThisCoursesAreSwapped && IsTaboo(schedule.assignments[day][period][room], schedule.assignments[day2][period2][room2]) == false) {
				currentValue = valueIfThisCoursesAreSwapped;
				bestCourse1 = schedule.assignments[day][period][room];//Remembers the person for the taboolist
				bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the person for the taboolist
				removeCourse(schedule, day2, period2, room2);
				removeCourse(schedule, day, period, room);
				assignCourse(schedule, day2, period2, room2, bestCourse1);
				assignCourse(schedule, day, period, room, bestCourse2);
			}


			AddTaboo(bestCourse1, bestCourse2); //Makes the swap back taboo.

		}
		System.out.println("Tabu  Found A Solution!");
		System.out.println("Value  = "+evaluationFunction(schedule));
		return schedule;
	}	

	/**
	 * Adds this swap to the taboolist - ONLY TABOOSEARCH
	 * @param course1
	 * @param course2
	 */
	public void AddTaboo(int course1, int course2)
	{
		//If the list is full, the first added is now removed
		if(this.tabooList1.size() == this.tabooLength)
		{
			this.tabooList1.remove(0);
			this.tabooList2.remove(0);
		}
		this.tabooList1.add(course1);
		this.tabooList2.add(course2);
	}

	/**
	 * Finds out if the swap is tabooed - ONLY TABOO SEARCH
	 * @param course1
	 * @param course2
	 * @return
	 */
	public boolean IsTaboo(int course1, int course2)
	{
		for (int i = 0 ; i < this.tabooList1.size(); i++)
		{
			if(tabooList1.elementAt(i) == course1)  
			{
				if(tabooList2.elementAt(i) == course2)
				{
					return true;
				}
			}
			if(tabooList1.elementAt(i) == course2)
			{
				if(tabooList2.elementAt(i) == course1)
				{
					return true;
				}
			}
		}
		return false;

	}
}
