import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

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

	public int iterationCount = 0;

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
    public int[] courseAssignmentCount;

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

    /**
     * Returns an array containing the number of times each course has been scheduled
     */
	protected int[] getCourseAssignmentCount(Schedule schedule) {
        int[] result = new int[basicInfo.courses];
        for (int day = 0; day < basicInfo.days; day++) {
            for (int period = 0; period < basicInfo.periodsPerDay; period++) {
                for (int room = 0; room < basicInfo.rooms; room++){
                    int assignedCourse = schedule.assignments[day][period][room];
                    if(assignedCourse == -1)
                        continue;
                    result[assignedCourse]++;
                }
            }
        }

        return result;
    }

    /**
     * Validates that no course has been scheduled more than the minimum number of times.
     * @return true if the constraint is not violated by the current schedule
     */
	public boolean validateMaximumScheduleCountConstraint(Schedule schedule) {
        for (int course = 0; course < basicInfo.courses; course++) {
            if (courseAssignmentCount[course] > courses.numberOfLecturesForCourse[course])
                return false;
        }
        return true;
	}

	private Random rand = new XORShiftRandom();

    public Schedule getRandomInitialSolution(){
        Schedule result = new Schedule(basicInfo.days, basicInfo.periodsPerDay, basicInfo.rooms);
        int[] courseAssignmentCount = new int[basicInfo.courses];
        
        for (int day = 0; day < basicInfo.days; day++) {
    		boolean[] courseAlreadyAssigned = new boolean[basicInfo.courses];
    		
        	for (int period = 0; period < basicInfo.periodsPerDay; period++) {
        		boolean[] lecturerBusy = new boolean[basicInfo.lecturers];
        		boolean[] curriculumBusy = new boolean[basicInfo.curricula];
        		for (int room = 0; room < basicInfo.rooms; room++) {
        			int assignedCourse = -1; // fix
        			int candidatecourse = -1;

					int attempts = 0;

        			while (assignedCourse == -1) {
                        candidatecourse = rand.nextInt(basicInfo.courses); // maybe use a priority queue instead?
                            attempts++;
                        if (attempts == basicInfo.courses * 2)
                            break;

	        			// course not already assigned in time slot
        				if (courseAlreadyAssigned[candidatecourse])
        					continue;
        				
	        			// course not unavailable in time slot
        				if (unavailability.courseUnavailable[day][period][candidatecourse])
        					continue;
        				
	        			// course lecturer cannot be busy
	        			if (lecturerBusy[courses.lecturerForCourse[candidatecourse]])
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
	                    if (courseAssignmentCount[candidatecourse] == courses.numberOfLecturesForCourse[candidatecourse])
	                    	continue;

	                    // no constraints violated! assign this course
	                    assignedCourse = candidatecourse;
        			}
        			
        			if (assignedCourse == -1)
        				continue;
        			
        			// increment constraints
        			courseAlreadyAssigned[assignedCourse] = true;
        			lecturerBusy[courses.lecturerForCourse[candidatecourse]] = true;
        			for (int curriculum = 0; curriculum < basicInfo.curricula; curriculum++) {
                        if (this.curriculum.isCourseInCurriculum[assignedCourse][curriculum]) {
                            curriculumBusy[curriculum] = true;
                        }
                    }
        			courseAssignmentCount[candidatecourse]++;
        			
        			result.assignments[day][period][room] = assignedCourse;
        		}
        	}
        }
        
        return result;
    }

    public int evaluationFunction(Schedule schedule)
    {
    	int[] numberOfLecturesOfCourse = new int[basicInfo.courses];
    	
    	for(int i=0; i < basicInfo.courses; i++){
    		numberOfLecturesOfCourse[i] = courses.numberOfLecturesForCourse[i];
    	}
    	
    	//to calculate the number of unallocated lectures of each course
    	for(int day= 0; day < basicInfo.days; day++){
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int room = 0; room < basicInfo.rooms; room++){
    				int assignedCourse = schedule.assignments[day][period][room];
    				if(assignedCourse == -1)
    					continue;
    				numberOfLecturesOfCourse[assignedCourse]--;
    			}
    		}
    	}
    	
    	//to calculate the number of days of each course that is scheduled below the minimum number of working days
    	int[] minimumWorkingDaysOfCourse = new int[basicInfo.courses];
    	
    	for(int i=0; i < basicInfo.courses; i++){
    		minimumWorkingDaysOfCourse[i] = courses.minimumWorkingDaysForCourse[i];
    	}
    	
    	for(int day = 0; day < basicInfo.days; day++){
    		// to avoid overcount working days of each course
    		boolean[] dayFulfilled = new boolean[basicInfo.courses];
    		for(int i = 0; i < basicInfo.courses; i++){
    			dayFulfilled[i] = false;
    		}
    		
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int room = 0; room < basicInfo.rooms; room++){
    				int assignedCourse = schedule.assignments[day][period][room];
    				if(assignedCourse == -1)
    					continue;
    				if(dayFulfilled[assignedCourse] == true)
    					continue;
    				dayFulfilled[assignedCourse] = true;
    				minimumWorkingDaysOfCourse[assignedCourse]--;
    			}
    		}
    	}
    	
    	int[][][] secludedLecture = new int[basicInfo.days][basicInfo.periodsPerDay][basicInfo.curricula];
    	
    	//Initialise the value of each slot in secludedLecture, 1 if a curriculum in a timeslot has a secluded lecture
    	for(int day = 0; day < basicInfo.days; day++){
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int curriculum = 0; curriculum < basicInfo.curricula; curriculum++){
    				secludedLecture[day][period][curriculum] = 0;
    			}
    		}
    	}
    	
    	//to calculate the number of secluded lectures of each curriculum
    	for(int day = 0; day < basicInfo.days; day++){
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int curriculum = 0; curriculum < basicInfo.curricula; curriculum++){
    				int count1 = 0; // to calculate X_c,t,r
    				int count2 = 0; // to calculate X_c',t',r'
    				for(int course = 0; course < basicInfo.courses; course++){
    					if (this.curriculum.isCourseInCurriculum[course][curriculum] == true){
    						for(int room = 0; room < basicInfo.rooms; room++){
    	    					if(schedule.assignments[day][period][room] == course)
    	    						count1++;
    	    				}
        				}
    				}
    				int adjacentPeriod1 = period - 1;
    				int adjacentPeriod2 = period + 1;
    				
    				if(adjacentPeriod1 >= 0){
    					for(int course = 0;course < basicInfo.courses; course++){
    						if(this.curriculum.isCourseInCurriculum[course][curriculum] == true){
    							for(int room = 0; room < basicInfo.rooms;room++){
    								if(schedule.assignments[day][adjacentPeriod1][room] == course)
    									count2++;
    							}
    						}
    					}
    				}
    				
    				if(adjacentPeriod2 < basicInfo.periodsPerDay){
    					for(int course = 0;course < basicInfo.courses; course++){
    						if(this.curriculum.isCourseInCurriculum[course][curriculum] == true){
    							for(int room = 0; room < basicInfo.rooms;room++){
    								if(schedule.assignments[day][adjacentPeriod2][room] == course)
    									count2++;	
    							}
    						}
    					}
    				}
    				
    				if(count1 == 1 && count2 == 0)
    					secludedLecture[day][period][curriculum] = 1;
    			}
    		}
    	}
    	
    	//to calculate number of room changes of each courses
    	int[] numberOfRoomChanges = new int[basicInfo.courses];
    	
    	//if the course is always taught in same room, the value is 0
    	//if the course is never allocated, the value is -1
    	for(int course = 0; course < basicInfo.courses; course++){
    		numberOfRoomChanges[course] = -1;
    	}
    	
    	for(int course = 0; course < basicInfo.courses; course++){ 
    		boolean[] roomChanged = new boolean[basicInfo.rooms];
        	
        	for(int room = 0; room < basicInfo.rooms; room++){
        		roomChanged[room] = false;
        	}
        	
    		for(int day = 0; day < basicInfo.days; day++){
    			for(int period = 0; period < basicInfo.periodsPerDay; period++){
        			for(int room = 0; room < basicInfo.rooms; room++){
        				if(schedule.assignments[day][period][room] == course)
        					roomChanged[room] = true;
        			}
    			}
    		}
    		
    		for(int room = 0; room < basicInfo.rooms; room++){
        		if(roomChanged[room] == true)
        			numberOfRoomChanges[course]++;
        	}	
    	}
    	
    	//to calculate the amount of capacity that room is exceeded in a timeslot
    	int leftOverCapacity = 0;
    	
    	for(int day = 0; day < basicInfo.days; day++){
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int room = 0; room < basicInfo.rooms; room++){
    				
    				int course = schedule.assignments[day][period][room];
    				if(course != -1){
    					if(this.rooms.capacityForRoom[room] < this.courses.numberOfStudentsForCourse[course])
        					leftOverCapacity += (this.courses.numberOfStudentsForCourse[course] - this.rooms.capacityForRoom[room]);
        			}
    			}		
    		}
    	}
    	
    	int objective = 0; // calculate the penalties 
    	int unscheduled = 0;
    	int minimumWorkingDays = 0;
    	int curriculumCompactness = 0;
    	int roomStability = 0;
    	
    	for(int i = 0; i < basicInfo.courses; i++){
    		unscheduled += numberOfLecturesOfCourse[i];
    	}
    	
    	for(int course = 0; course < basicInfo.courses; course++){
    		if(minimumWorkingDaysOfCourse[course] > 0)
    		minimumWorkingDays += minimumWorkingDaysOfCourse[course];
    	}
    	
    	for(int day = 0; day < basicInfo.days; day++){
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int curriculum = 0; curriculum < basicInfo.curricula; curriculum++){
    				curriculumCompactness += secludedLecture[day][period][curriculum];
    			}
    		}
    	}
    	
    	for(int course = 0; course < basicInfo.courses; course++){
    		if(numberOfRoomChanges[course] > 0)
    		roomStability += numberOfRoomChanges[course];
    	}

    	objective = 10*unscheduled + 5*minimumWorkingDays + 2*curriculumCompactness + roomStability + leftOverCapacity;
    	
    	return objective;
    }
    
	 	//[day][period][room]
 	protected void DeepClone(Schedule original,Schedule copy) {
 		for(int day=0;day<original.assignments.length;day++) {
 			for(int period =  0;period<original.assignments[day].length;period++) {
 				for(int room = 0;room<original.assignments[day][period].length;room++) {
 					//System.out.println("day = " + day + " period = "+period + " room = "+room);
 					copy.assignments[day][period][room] = original.assignments[day][period][room];
 				}
 			}
 		}
		
	}
 	
 	protected void SwapCourse (int day1,int period1 ,int room1,int day2,int period2 ,int room2 , Schedule Content ) {
 		
 		int temp = Content.assignments[day1][period1][room1]; 
 		Content.assignments[day1][period1][room1] = Content.assignments[day2][period2][room2];
 		Content.assignments[day2][period2][room2] = temp;
 	}

 	//delete and return the course number given schedule and time slots
protected int RemoveCourse (int day,int period ,int room, Schedule Content ) { 
		
 		if(Content.assignments[day][period][room]==-1) //there is no course in given time-room slot
 			return -1;
 		int tmp = Content.assignments[day][period][room];
 		Content.assignments[day][period][room]= -1;
		return tmp;
 	}


protected boolean AddCourse (int courseNo,int day,int period ,int room, Schedule Content ) {
	
		if(Content.assignments[day][period][room]!=-1) //first empty the time-room slot
			return false;
		Content.assignments[day][period][room] = courseNo;
		return true;
	}


    /**
     * Gets the value of the solution if it is altered by assigning a specific course in a given time slot and room.
     * This method return Integer.MAX_VALUE if the room is already occupied.
     * @return The value of the modified solution, or Integer.MAX_VALUE if a constraint is violated.
     */
    protected int valueIfAssigningCourse(Schedule schedule, int day, int room, int period, int courseId) {
        // Room must currently be empty
        if (schedule.assignments[day][period][room] != HillClimberOld.EMPTY_ROOM) {
            return Integer.MAX_VALUE;
        }

        // Assign the course
        assignCourse(schedule, day, period, room, courseId);

        // Validate all constraints
        if (!validateSameLecturerConstraint(schedule) || !validateSameCurriculumConstraint(schedule) || !validateAvailabilityConstraint(schedule) || !validateMaximumScheduleCountConstraint(schedule)) {
            // Revert the change and return a large value
            removeCourse(schedule, day, period, room);
            return Integer.MAX_VALUE;
        }

        // Compute the value of the altered solution
        int value = evaluationFunction(schedule);

        // Revert the change and return the computed value
        removeCourse(schedule, day, period, room);
        return value;
    }

    /**
     * Gets the value of the solution if the given time slot and room is emptied
     * @return The value of the modified solution, or Integer.MAX_VALUE if a constraint is violated
     */
    protected int valueIfRemovingCourse(Schedule schedule, int day, int room, int period) {
        // Room must currently be occupied
        int currentCourse = schedule.assignments[day][period][room];
        if (currentCourse == HillClimberOld.EMPTY_ROOM) {
            return Integer.MAX_VALUE;
        }

        // Empty the room
        removeCourse(schedule, day, period, room);

        // Validate constraints
        if (!validateSameLecturerConstraint(schedule) || !validateSameCurriculumConstraint(schedule) || !validateAvailabilityConstraint(schedule) || !validateMaximumScheduleCountConstraint(schedule)) {
            // Proposed solution is invalid. Revert the change and return a large value.
            assignCourse(schedule, day, period, room, currentCourse);
            return Integer.MAX_VALUE;
        }

        // Compute the value of the altered solution
        int value = evaluationFunction(schedule);

        // Revert the change and return the computed value
        assignCourse(schedule, day, period, room, currentCourse);
        return value;
    }

    /**
     * Gets the value of the solution if the given time slot and room is emptied
     * @return The value of the modified solution, or Integer.MAX_VALUE if a constraint is violated
     */
    protected int valueIfSwappingCourses(Schedule schedule, int day, int room, int period,int day2,int period2,int room2) {
        // Room must currently be occupied
        int currentCourse = schedule.assignments[day][period][room];
        int currentCourse2 = schedule.assignments[day2][period2][room2];
 

        // Swap the rooms
        removeCourse(schedule, day, period, room);
        removeCourse(schedule, day2, period2, room2);
        assignCourse(schedule, day2, period2, room2, currentCourse);
        assignCourse(schedule, day, period, room, currentCourse2);

        // Validate constraints
        if (!validateSameLecturerConstraint(schedule) || !validateSameCurriculumConstraint(schedule) || !validateAvailabilityConstraint(schedule) || !validateMaximumScheduleCountConstraint(schedule)) {
            // Proposed solution is invalid. Revert the change and return a large value.
        	removeCourse(schedule, day, period, room);
            removeCourse(schedule, day2, period2, room2);
            assignCourse(schedule, day, period, room, currentCourse);
            assignCourse(schedule, day2, period2, room2, currentCourse2);
            return Integer.MAX_VALUE;
        }

        // Compute the value of the altered solution
        int value = evaluationFunction(schedule);

        // Revert the change and return the computed value
        removeCourse(schedule, day, period, room);
        removeCourse(schedule, day2, period2, room2);
        assignCourse(schedule, day, period, room, currentCourse);
        assignCourse(schedule, day2, period2, room2, currentCourse2);
        return value;
    }
    
    protected void assignCourse(Schedule schedule, int day, int period, int room, int course) {
        // Make sure the room is empty
        removeCourse(schedule, day, period, room);
        if(course == HillClimberOld.EMPTY_ROOM)
        	return;
        // Perform the assignment and increment the counter
        schedule.assignments[day][period][room] = course;
        courseAssignmentCount[course]++;
        
    }

    protected void removeCourse(Schedule schedule, int day, int period, int room) {
        int assignedCourse = schedule.assignments[day][period][room];
        if (assignedCourse == HillClimberOld.EMPTY_ROOM)
            return;

        courseAssignmentCount[assignedCourse]--;
        schedule.assignments[day][period][room] = HillClimberOld.EMPTY_ROOM;
    }
}
