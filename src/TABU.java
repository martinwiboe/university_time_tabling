import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Vector;

import com.opencsv.CSVWriter;


/**
 * Created by Burak on 08-04-2015.
 */
 public class TABU extends Heuristic {
 
	protected Schedule schedule; //current schedule 
	protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
	protected int currentValue;
	private int tabooLength;
	private Integer[] bestdayPeriodRoom1;
	private Integer [] bestdayPeriodRoom2;
    protected Vector<Integer[]> tabooList1; //The first taboolist - ONLY TABOOSEARCH
    protected Vector<Integer[]> tabooList2; //The second taboolist - ONLY TABOOSEARCH
	public TABU(int TabooLength) throws IOException
    {
		super();
        this.tabooLength = TabooLength;
        tabooList1  = new Vector<Integer[]>();
        tabooList2  = new Vector<Integer[]>();
        f = new FileWriter(this.getClass()+Integer.toString(tabooLength)+"iterationValue.csv");
	    writer = new CSVWriter(f, ',', CSVWriter.NO_QUOTE_CHARACTER);
        
    }
	
	@Override
	public Schedule search(Schedule schedule) throws IOException {
		startCountdown();
        currentValue = evaluationFunction(schedule); // value of the current solution
        courseAssignmentCount = getCourseAssignmentCount(schedule);
 	    System.out.println("Start");
		while(timeoutReached() == false) {
			
			this.iterationCount++; //Adds to the iteration count
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
 									valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, day, period,room, day2, period2, room2);
 									if(currentValue>valueIfThisCoursesAreSwapped && IsTaboo(day,period,room, day2,period2,room2) == false) {
 										currentValue = valueIfThisCoursesAreSwapped;
 										
 										
 										bestdayPeriodRoom1 = new Integer[3];
 										bestdayPeriodRoom1[0] = day;
 										bestdayPeriodRoom1[1] = period;
 										bestdayPeriodRoom1[2] = room;
 										bestdayPeriodRoom2 = new Integer[3];
 										bestdayPeriodRoom2[0] = day2;
 										bestdayPeriodRoom2[1] = period2;
 										bestdayPeriodRoom2[2] = room2;
 										
 										
 										int bestCourse1 = schedule.assignments[day][period][room];//Remembers the course for the assign
 										int bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the course for the assign
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
			AddTaboo(bestdayPeriodRoom1, bestdayPeriodRoom2); //Makes the swap back taboo.
	        String[] result = new String[] { "" + iterationCount, currentValue + "" };
	       	writer.writeNext(result);
	        }
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
    public void AddTaboo(Integer[] dayPeriodRoom1,Integer[] dayPeriodRoom2)
    {
        //If the list is full, the first added is now removed
        if(this.tabooList1.size() == this.tabooLength)
        {
            this.tabooList1.remove(0);
            this.tabooList2.remove(0);
        }
        this.tabooList1.add(dayPeriodRoom1);
        this.tabooList2.add(dayPeriodRoom2);
    }
    
    /**
     * Finds out if the swap is tabooed - ONLY TABOO SEARCH
     * @param course1
     * @param course2
     * @return
     */
    public boolean IsTaboo(int day1,int period1, int room1,int day2,int period2,int room2)
    {
        for (int i = 0 ; i < this.tabooList1.size(); i++)
        {
            if(tabooList1.elementAt(i)[0] == day1 && tabooList1.elementAt(i)[1] == period1 && tabooList1.elementAt(i)[2] == room1 )  
            {
                if(tabooList2.elementAt(i)[0] == day2 && tabooList2.elementAt(i)[1] == period2 && tabooList2.elementAt(i)[2] == room2)
                {
                    return true;
                }
            }
            if(tabooList1.elementAt(i)[0] == day2 && tabooList1.elementAt(i)[1] == period2 && tabooList1.elementAt(i)[2] == room2 )  
            {
                if(tabooList2.elementAt(i)[0] == day1 && tabooList2.elementAt(i)[1] == period1 && tabooList2.elementAt(i)[2] == room1)
                {
                    return true;
                }
            }
        }
        return false;

    }
 }
