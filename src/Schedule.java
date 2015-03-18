import java.util.Vector;

/**
 * Created by Martin on 16-03-2015.
 */

public class Schedule {
	
    public int[] remainLecture; //The array of lecture of each course
    public boolean[][] curriculum; //Check if courses are allocated in the same curriculum
    public int[][][] assignments; //The array of scheduled courses, look up using [day][period][room],Value is -1 if the room is empty
    public boolean[][][] unavailableCourse; // look up using [day][period][course]
    public int[] lecturer; // lecturer of the course		
    /**
     * Checks that courses are not assigned into an occupied time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateRoomOccupancyConstraint() {
    	return false;
    }

    /**
     * Checks that courses with the same lecturer is not assigned in same time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateSameLecturerConstraint(int Day, int Period, int Room, int Course) {
    	int lecturer1;
    	int lecturer2;
    	
    	lecturer1 = lecturer[Course]; 
    	
    	if (Room>0){
    		int startPoint = Room - 1;
    	}
    	else if (Room == 0){
    		int startPoint = Room + 1;
    	}
    	
    	int k;
    	for(k=startPoint;k<NumberOfRoom;k++){
    		if(k != Room){
    			int course1 = assignments[Day][Period][k];
    			if(course1 <0){
    				lecturer2 = this.NumberOfLecturer;
    			}
    			else if(course1 >0){
    				lecturer2 = lecturer[course1];
    			}
    				 
    			if(lecturer1 == lecturer2){
    				return false;
    			}
    		}
    	}
        return true;
    }

    /**
     * Checks that courses in the same curriculum are not scheduled in the same time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateSameCurriculumConstraint(int Day, int Period, int Room, int Course) {
    	if (Room>0){
    		int startPoint = Room - 1;
    	}
    	else if (Room == 0){
    		int startPoint = Room + 1;
    	}
    	
    	int k;
    	for(k=startPoint;k<NumberOfRoom;k++){
    		if(k != Room){
    			int course1 = assignments[Day][Period][k];
    			if(course1 >0){
    				if(course1 == Course || curriculum[course1][Course] == true){ 
    					return false;
    				}
    			}
    		}
    	}
    	
    	return true;
    }

    /**
     * Checks that courses are not scheduled in unavailable time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateAvailabilityConstraint(int Day, int Period, int Course) {
        return unavailableCourse[Day][Period][Course];
    }
    
    public void GetRandomInitialSolution(){
    	int slot;
    	//Haven't created the variable, assume they will be declared later
    	int totalSlot = this.NumberOfDay*this.NumberOfPeriod*this.NumberOfRoom;
    	int course = this.NumberOfCourse;
    	
    	int value = -1; //to assign specific value for each slot
    	int i,j,k;
    	
    	for(i=0;i<this.NumberOfDay;i++){
    		for(j=0;j<this.NumberOfPeriod;j++){
    			for(k=0;k<this.NumberOfRoom;k++){
    				assigments[i][j][k] = value;
    				value--;
    			}
    		}
    	}
    	
    	Random Rand = new Random();
    	Vector<Integer> RemainRooms = new Vector<Integer>();
    	
    	for(slot =-1;i>-this.totalSlot;i--){
    		RemainRoom.add(slot);
    	}
    	
    	int assignedCourse= 0;
    	
    	do{
    		do{
    			do{
    				do{
    					int getDay;
            			int getPeriod;
            			int getRoom;
            		
            			Random Rand = new Random();
            			int SlotSpot = Rand.nextInt(RemainRooms.size());
            			slot = RemainRooms.elementAt(SlotSpot);
            		
            			for(i=0;i<this.Day;i++){
            				for(j=0;j<this.Period;j++){
            					for(k=0;k<this.room;k++){
            						if (assigments[i][j][k] == slot){
            							getDay = i;
            							getPeriod = j;
            							getRoom = k;
            						}	
            					}
            				}
            			}
    				}while(validateAvailabilityConstraint(getDay,getPeriod,assignedCourse) == false);
    				
    			}while(validateSameLecturerConstraint(getDay,getPeriod,getRoom,assignedCourse) == false);
    			
    		} while(validateSameCurriculumConstraint(getDay,getPeriod,getRoom,assignedCourse) == false);
    		
    		assignments[getDay][getPeriod][getRoom] = assignedCourse;
    		RemainRooms.removeElementAt(SlotSpot);
    		
    		remainLecture[assignedCourse]--;
    		if(rremainLecture[assignedCourse] == 0){
    			assignedCourse++;
    		}
    }while(assignedCourse < course);
    	
    	//assign -1 for all empty room
    	
    	for(i=0;i<this.NumberOfDay;i++){
    		for(j=0;j<this.NumberOfPeriod;j++){
    			for(k=0;k<this.NumberOfRoom;k++){
    				if(assigments[i][j][k] < 0){
    					assignments[i][j][k] = -1;
    				}
    			}
    		}
    	}
    }
}
