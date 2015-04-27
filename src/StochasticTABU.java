import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.opencsv.CSVWriter;


/**
 * Created by Burak on 08-04-2015.
 */
public class StochasticTABU extends Heuristic {

	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int currentValue;
	protected int previousValue = Integer.MAX_VALUE;
	private int tabooLength;
	private int bestCourse1;
	private int bestCourse2;
	private Random random = new XORShiftRandom();
	protected Vector<Integer> tabooList1; //The first taboolist - ONLY TABOOSEARCH
	protected Vector<Integer> tabooList2; //The second taboolist - ONLY TABOOSEARCH
	private Integer[] bestdayPeriodRoom1;
	private Integer [] bestdayPeriodRoom2;
	protected Vector<Integer[]> tabooListSlots1; //The first taboolist - ONLY TABOOSEARCH
	protected Vector<Integer[]> tabooListSlots2; //The second taboolist - ONLY TABOOSEARCH
	private static final int ASSIGNNO = -2;
	private static final int REMOVENO = -3;
	public StochasticTABU(int TabooLength) throws IOException
	{
		this.tabooLength = TabooLength;
		//tabooList1  = new Vector<Integer>();
		//tabooList2  = new Vector<Integer>();
		tabooListSlots1 = new Vector<Integer[]>();
		tabooListSlots2 = new  Vector<Integer[]>();
		String uuid = UUID.randomUUID().toString();
		f = new FileWriter(this.getClass()+Integer.toString(tabooLength)+uuid+"iterationValue.csv");
		writer = new CSVWriter(f, ',', CSVWriter.NO_QUOTE_CHARACTER);

	}

	@Override
	public Schedule search(Schedule schedule) throws IOException {
		startCountdown();
		currentValue = evaluationFunction(schedule); // value of the current solution
		String[] result = new String[] { "" + iterationCount, currentValue + "" };
		writer.writeNext(result);
		deltaState.courseAssignmentCount = getCourseAssignmentCount(schedule);
		int rooms = this.basicInfo.rooms;
		int days = this.basicInfo.days;
		int periods = this.basicInfo.periodsPerDay;
		System.out.println("Start");
		while(timeoutReached() == false) {

			this.iterationCount++; //Adds to the iteration count
			int room = random.nextInt(rooms);
			int day = random.nextInt(days);
			int period = random.nextInt(periods);
			int room2 = random.nextInt(rooms);
			int day2 = random.nextInt(days);
			int period2 = random.nextInt(periods);
			int valueIfThisCourseIsAssigned  = Integer.MAX_VALUE;
			int valueIfThisCourseIsRemoved  = Integer.MAX_VALUE;
			int valueIfThisCoursesAreSwapped  = Integer.MAX_VALUE;
			valueIfThisCourseIsRemoved  = valueIfRemovingCourse(schedule, currentValue, day, room, period); //calculate the value if we remove the course given timeslot
			int courseId = random.nextInt(this.basicInfo.courses);
			valueIfThisCourseIsAssigned  = valueIfAssigningCourse(schedule, currentValue, day, room, period, courseId); //calculate the value if we swap the courses given timeslots
			valueIfThisCoursesAreSwapped  = valueIfSwappingCourses(schedule, currentValue, day, period, room, day2, period2, room2);//calculate the value if we add the course given timeslot
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

			int deltaval;
			switch (change) {
			case REMOVE:{ 
				bestdayPeriodRoom1 = new Integer[3];
				bestdayPeriodRoom1[0] = day;
				bestdayPeriodRoom1[1] = period;
				bestdayPeriodRoom1[2] = room;
				bestdayPeriodRoom2 = new Integer[3];
				bestdayPeriodRoom2[0] = REMOVENO;
				bestdayPeriodRoom2[1] = REMOVENO;
				bestdayPeriodRoom2[2] = REMOVENO;
				deltaval =  valueIfThisCourseIsRemoved - currentValue;
				bestCourse1 = schedule.assignments[day][period][room];//Remembers the course for taboo list
				if(deltaval < 0 && !IsTaboo(bestdayPeriodRoom1,bestdayPeriodRoom2)) { 
					currentValue  +=deltaval;
					removeCourse(schedule, day, period, room);
					AddTaboo(bestdayPeriodRoom1, bestdayPeriodRoom2);//we add the course in the tabo list with the REMOVENO so when we check the taboo list we will know its assigned
				}
				break;
			}
			case ASSIGN: {
				bestdayPeriodRoom1 = new Integer[3];
				bestdayPeriodRoom1[0] = day;
				bestdayPeriodRoom1[1] = period;
				bestdayPeriodRoom1[2] = room;
				bestdayPeriodRoom2 = new Integer[3];
				bestdayPeriodRoom2[0] = ASSIGNNO;
				bestdayPeriodRoom2[1] = ASSIGNNO;
				bestdayPeriodRoom2[2] = ASSIGNNO;
				deltaval =  valueIfThisCourseIsAssigned - currentValue;
				if(deltaval < 0 && !IsTaboo(bestdayPeriodRoom1,bestdayPeriodRoom2)) { 
					currentValue  +=deltaval;
					assignCourse(schedule, day, period, room, courseId);
					AddTaboo(bestdayPeriodRoom1, bestdayPeriodRoom2); //we add the course in the tabo list with the ASSIGNNO so when we check the taboo list we will know its assigned
				}
				break;
			}
			case SWAP: {
				bestdayPeriodRoom1 = new Integer[3];
				bestdayPeriodRoom1[0] = day;
				bestdayPeriodRoom1[1] = period;
				bestdayPeriodRoom1[2] = room;
				bestdayPeriodRoom2 = new Integer[3];
				bestdayPeriodRoom2[0] = day2;
				bestdayPeriodRoom2[1] = period2;
				bestdayPeriodRoom2[2] = room2;
				deltaval = valueIfThisCoursesAreSwapped - currentValue;
				//bestCourse1 = schedule.assignments[day][period][room];//Remembers the course for to assign
				//bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the course for to assign
				if(deltaval < 0 && !IsTaboo(bestdayPeriodRoom1,bestdayPeriodRoom2)) { 
					currentValue  = valueIfThisCoursesAreSwapped;
					removeCourse(schedule, day2, period2, room2);
					removeCourse(schedule, day, period, room);
					assignCourse(schedule, day2, period2, room2, bestCourse1);
					assignCourse(schedule, day, period, room, bestCourse2);
					AddTaboo(bestdayPeriodRoom1, bestdayPeriodRoom2); //Makes the swap back taboo.
				}
				break;
			}

			default:
				break;
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
			if(course2==ASSIGNNO) { //that means we need to check TABU list for assign

				if(tabooList1.elementAt(i) == course1)  
				{
					if(tabooList2.elementAt(i) == REMOVENO) //that means if we remove the course before dont assign that course again it is TABU
					{
						return true;
					}
				}
			}
			else if (course2 == REMOVENO) {//check TABU list for remove
				if(tabooList1.elementAt(i) == course1)  
				{
					if(tabooList2.elementAt(i) == ASSIGNNO) //that means if we assign the course before dont remove that course again it is TABU
					{
						return true;
					}
				}
			}
			else { //finaly if course2 is not ASSIGNNO or REMOVENO then check TABU list for Swap
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
		}

		return false;

	}
	/**
	 * Adds this swap to the taboolist - ONLY TABOOSEARCH
	 * @param dayPeriodRoom1
	 * @param dayPeriodRoom2
	 */
	public void AddTaboo(Integer[] dayPeriodRoom1,Integer[] dayPeriodRoom2)
	{
		//If the list is full, the first added is now removed
		if(this.tabooList1.size() == this.tabooLength)
		{
			this.tabooList1.remove(0);
			this.tabooList2.remove(0);
		}
		this.tabooListSlots1.add(dayPeriodRoom1);
		this.tabooListSlots1.add(dayPeriodRoom2);
	}

	/**
	 * Finds out if the swap is tabooed - ONLY TABOO SEARCH
	 */
	public boolean IsTaboo(Integer[] dayPeriodRoom1,Integer[] dayPeriodRoom2)
	{
		
		if(dayPeriodRoom2[0] == REMOVENO) {
			
		}

		for (int i = 0 ; i < this.tabooList1.size(); i++)
		{
			
			if(tabooListSlots1.elementAt(i)[0] == dayPeriodRoom1[0] && tabooListSlots1.elementAt(i)[1] == dayPeriodRoom1[1] && tabooListSlots1.elementAt(i)[2] == dayPeriodRoom1[2] )  
			{
				if(tabooListSlots2.elementAt(i)[0] == dayPeriodRoom2[0] && tabooListSlots2.elementAt(i)[1] == dayPeriodRoom2[1] && tabooListSlots2.elementAt(i)[2] == dayPeriodRoom2[2])
				{
					return true;
				}
			}
			if(tabooListSlots1.elementAt(i)[0] == dayPeriodRoom2[0] && tabooListSlots1.elementAt(i)[1] == dayPeriodRoom2[1] && tabooListSlots1.elementAt(i)[2] == dayPeriodRoom2[2] )  
			{
				if(tabooListSlots2.elementAt(i)[0] == dayPeriodRoom1[0] && tabooListSlots2.elementAt(i)[1] == dayPeriodRoom1[1] && tabooListSlots2.elementAt(i)[2] == dayPeriodRoom1[2])
				{
					return true;
				}
			}
			
			
		}
		return false;

	}
}
