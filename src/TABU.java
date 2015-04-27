import java.io.IOException;
import java.util.Vector;


/**
 * Created by Burak on 08-04-2015.
 */
 public class TABU extends Heuristic {
 
	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int IterationCount = 0;
	protected int currentValue;
	private int tabooLength;
	private int bestCourse1;
	private int bestCourse2;
    protected Vector<Integer> tabooList1; //The first taboolist - ONLY TABOOSEARCH
    protected Vector<Integer> tabooList2; //The second taboolist - ONLY TABOOSEARCH
	public TABU(int TabooLength) throws IOException
    {
        this.tabooLength = TabooLength;
        tabooList1  = new Vector<Integer>();
        tabooList2  = new Vector<Integer>();
        
    }
	
	@Override
	public Schedule search(Schedule schedule) {
		startCountdown();
        currentValue = evaluationFunction(schedule); // value of the current solution
		deltaState.courseAssignmentCount = getCourseAssignmentCount(schedule);
 	    System.out.println("Start");
		while(timeoutReached() == false) {
			
			this.IterationCount++; //Adds to the iteration count
			System.out.println("Iteration Count = " + IterationCount);
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
 									valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, currentValue, day, period,room, day2, period2, room2);
 									if(currentValue>valueIfThisCoursesAreSwapped && IsTaboo(schedule.assignments[day][period][room], schedule.assignments[day2][period2][room2]) == false) {
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
