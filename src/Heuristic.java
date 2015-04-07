import java.util.Date;
import java.util.Iterator;

/**
 * Created by Martin on 05-04-2015.
 */
public abstract class Heuristic {
    /**
     * Perform a search with the input schedule as a starting point. The method will return the best schedule
     * found before timeout.
     * This method is abstract and must be implemented according to the chosen heuristic.
     * @param schedule The Schedule to start from
     * @return The most optimal schedule found during search.
     */
    public abstract Schedule search(Schedule schedule);

    private int timeout = 300;

    /**
     * @return the duration, in seconds, that the heuristic is allowed to search for solutions.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the duration, in seconds, that the heuristic is allowed to search for solutions.
     * @param seconds the duration in seconds. Must be a positive value.
     */
    public void setTimeout(int seconds) {
        if (seconds <= 0)
            return;
        timeout = seconds;
    }

    /**
     * The millisecond time when the countdown was started.
     */
    private long countdownStartTime;

    /**
     * Starts the countdown timer. Call timeoutReached() to determine when the timeout has been reached.
     */
    protected void startCountdown() {
        countdownStartTime = new Date().getTime();
    }

    /**
     * Returns a value indicating whether the timeout has currently been reached.
     * startCountdown() must be called prior to calling this method.
     * @return true if the timeout has been reached, or if the countdown has not yet started. If not, false.
     */
    protected boolean timeoutReached() {
        Date currentTime = new Date();
        long delta = (currentTime.getTime() - countdownStartTime) / 1000;
        return delta > timeout;
    }

    /**
     * Checks that courses with the same lecturer is not assigned in same time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateSameLecturerConstraint(Schedule schedule) {
        // For a given day, keep track of which lecturers are busy
        boolean[] lecturerBusy;
        for (int day = 0; day < basicInfo.days; day++) {
            for (int period = 0; period < basicInfo.periodsPerDay; period++) {
                // Assume all lecturers are idle
                lecturerBusy = new boolean[basicInfo.lecturers];

                for (int room = 0; room < basicInfo.rooms; room++) {
                    // Check which course, if any, is assigned to this room at this time
                    int assignedCourse = schedule.assignments[day][period][room];
                    if (assignedCourse == -1)
                        continue;

                    // Remember that the lecturer is busy. If we already know this, the constraint is violated.
                    int lecturer = courses.lecturerForCourse[assignedCourse];
                    if (lecturerBusy[lecturer])
                        return false;
                    lecturerBusy[lecturer] = true;
                }
            }
        }

        return true;
    }

    public BasicInfo basicInfo;
    public Curriculum curriculum;
    public Lecturers lecturers;
    public Courses courses;
    public Unavailability unavailability;
    public Rooms rooms;

    /**
     * Checks that courses in the same curriculum are not scheduled in the same time slots
     * @return true if the constraint is satisfied
     */
    public boolean validateSameCurriculumConstraint(Schedule schedule) {
        // For a given day, keep track of which lecturers are busy
        boolean[] curriculumBusy;
        for (int day = 0; day < basicInfo.days; day++) {
            for (int period = 0; period < basicInfo.periodsPerDay; period++) {
                // Assume all curricula are idle
                curriculumBusy = new boolean[basicInfo.curricula];

                for (int room = 0; room < basicInfo.rooms; room++) {
                    // Check which course, if any, is assigned to this room at this time
                    int assignedCourse = schedule.assignments[day][period][room];
                    if (assignedCourse == -1)
                        continue;

                    for (int curriculum = 0; curriculum < basicInfo.curricula; curriculum++) {
                        if (this.curriculum.isCourseInCurriculum[assignedCourse][curriculum]) {
                            if (curriculumBusy[curriculum])
                                return false;
                            curriculumBusy[curriculum] = true;
                        }
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
    public boolean validateAvailabilityConstraint(Schedule schedule) {
        // Iterate through all constraints and check whether any have been violated
        Iterator<UnavailabilityConstraint> iter = unavailability.constraints.iterator();
        while (iter.hasNext()) {
            UnavailabilityConstraint c = iter.next();

            for (int room = 0; room < basicInfo.rooms; room++) {
                if (schedule.assignments[c.day][c.period][room] == c.course)
                    return false;
            }
        }
        return true;
    }

    public Schedule getRandomInitialSolution(){
        Schedule result = new Schedule(basicInfo.days, basicInfo.periodsPerDay, basicInfo.rooms);
        int[] courseAssignmentCount = new int[basicInfo.courses];
        
        for (int day = 0; day < basicInfo.days; day++) {
    		boolean[] courseAlreadyAssigned = new boolean[basicInfo.courses];
    		
        	for (int period = 0; period < basicInfo.periods; period++) {
        		boolean[] lecturerBusy = new boolean[basicInfo.lecturers];
        		boolean[] curriculumBusy = new boolean[basicInfo.curricula];
        		for (int room = 0; room < basicInfo.rooms; room++) {
        			int assignedCourse = 0; // fix
        			int candidatecourse = 0;
        			// find the course to assign, subject to
        			while (assignedCourse == 0) {
        				candidateCourse++; // maybe use a priority queue instead?
        				
	        			// course not already assigned in time slot
        				if (courseAlreadyAssigned[candidatecourse])
        					continue;
        				
	        			// course not unavailable in time slot
        				if (unavailability.courseUnavailable[day][period][candidatecourse])
        					continue;
        				
	        			// course lecturer cannot be busy
	        			if (lecturerBusy[lecturers.lecturerForCourse[candidatecourse])
	        				continue;
        				
        				// course curriculum cannot be busy
	        			boolean curriculumConflict = false;
	                    for (int curriculum = 0; curriculum < basicInfo.curricula; curriculum++) {
	                        if (this.curriculum.isCourseInCurriculum[candidatecourse][curriculum]) {
	                            if (curriculumBusy[curriculum])
	                                curriculumConflict = true;
	                        }
	                    }
	                    if (curriculumConflict)
	                    	continue;
	        			
	        			// course max lectures cannot be reached
	                    if (courseAssignmentCount[candidatecourse] == courses.minimumLecturesForCourse[candidatecourse])
	                    	continue;
	                    
	        			// course minimum working days .... ?
	                    
	                    // no constraints violated! assign this course
	                    assignedCourse = candidatecourse;
        			}
        			
        			// increment constraints
        			courseAlreadyAssigned[assignedCourse] = true;
        			lecturerBusy[lecturers.lecturerForCourse[candidatecourse] = true;
        			for (int curriculum = 0; curriculum < basicInfo.curricula; curriculum++) {
                        if (this.curriculum.isCourseInCurriculum[assignedCourse][curriculum]) {
                            curriculumBusy[curriculum] = true;
                        }
                    }
        			courseAssignmentCount[candidatecourse]++;
        			
        			result.assignments[day][period[room] = assignedCourse;
        		}
        	}
        }
        
        return result;
        
        /*
        int slot;
        //Haven't created the variable, assume they will be declared later
        int totalSlot = basicInfo.days * basicInfo.periodsPerDay * basicInfo.rooms;
        int course = basicInfo.courses;

        int value = -1; //to assign specific value for each slot
        int i,j,k;

    	for(i=0;i< basicInfo.days;i++){
    		for(j=0;j< basicInfo.periodsPerDay;j++){
    			for(k=0;k<basicInfo.rooms;k++){

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
    	}*/
    }
}
