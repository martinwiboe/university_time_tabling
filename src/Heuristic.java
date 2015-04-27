import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import com.opencsv.CSVWriter;

public abstract class Heuristic {
    public static final int EMPTY_ROOM = -1;

    /**
     * Perform a search with the input schedule as a starting point. The method will return the best schedule
     * found before timeout.
     * This method is abstract and must be implemented according to the chosen heuristic.
     * @param schedule The Schedule to start from
     * @return The most optimal schedule found during search.
     * @throws IOException 
     */
    public abstract Schedule search(Schedule schedule) throws IOException;

    private int timeout = 300;

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
    public BasicInfo basicInfo;
    public Curriculum curriculum;
    public Lecturers lecturers;
    public Courses courses;
    public Unavailability unavailability;
    public Rooms rooms;

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

        System.arraycopy(courses.numberOfLecturesForCourse, 0, numberOfLecturesOfCourse, 0, basicInfo.courses);
    	
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

        System.arraycopy(courses.minimumWorkingDaysForCourse, 0, minimumWorkingDaysOfCourse, 0, basicInfo.courses);
    	
    	for(int day = 0; day < basicInfo.days; day++){
    		// to avoid overcount working days of each course
    		boolean[] dayFulfilled = new boolean[basicInfo.courses];
    		for(int i = 0; i < basicInfo.courses; i++){
    			dayFulfilled[i] = false;
    		}
    		
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int room = 0; room < basicInfo.rooms; room++){
    				int assignedCourse = schedule.assignments[day][period][room];
    				if(assignedCourse == EMPTY_ROOM)
    					continue;
    				if(dayFulfilled[assignedCourse])
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
    					if (this.curriculum.isCourseInCurriculum[course][curriculum]){
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
    						if(this.curriculum.isCourseInCurriculum[course][curriculum]){
    							for(int room = 0; room < basicInfo.rooms;room++){
    								if(schedule.assignments[day][adjacentPeriod1][room] == course)
    									count2++;
    							}
    						}
    					}
    				}
    				
    				if(adjacentPeriod2 < basicInfo.periodsPerDay){
    					for(int course = 0;course < basicInfo.courses; course++){
    						if(this.curriculum.isCourseInCurriculum[course][curriculum]){
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
        		if(roomChanged[room])
        			numberOfRoomChanges[course]++;
        	}
    	}

    	//to calculate the amount of capacity that room is exceeded in a timeslot
    	int capacityExceeding = 0;
    	
    	for(int day = 0; day < basicInfo.days; day++){
    		for(int period = 0; period < basicInfo.periodsPerDay; period++){
    			for(int room = 0; room < basicInfo.rooms; room++){
    				
    				int course = schedule.assignments[day][period][room];
    				if(course != -1){
    					if(this.rooms.capacityForRoom[room] < this.courses.numberOfStudentsForCourse[course])
        					capacityExceeding += (this.courses.numberOfStudentsForCourse[course] - this.rooms.capacityForRoom[room]);
        			}
    			}		
    		}
    	}
    	
    	int objective; // calculate the penalties
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

    	objective = 10*unscheduled + 5*minimumWorkingDays + 2*curriculumCompactness + roomStability + capacityExceeding;

    	return objective;
    }

    /**
     * Gets the value of the solution if it is altered by assigning a specific course in a given time slot and room.
     * This method return Integer.MAX_VALUE if the room is already occupied.
     * @return The value of the modified solution, or Integer.MAX_VALUE if a constraint is violated.
     */
    protected int valueIfAssigningCourse(Schedule schedule, int currentValue, int day, int room, int period, int courseId) {
        // Room must currently be empty
        if (schedule.assignments[day][period][room] != Heuristic.EMPTY_ROOM) {
            return Integer.MAX_VALUE;
        }

        // Ensure that the move is valid
        boolean moveIsValid = deltaState.deltaValidateAllConstraints(day, period, courseId);
        if (!moveIsValid) {
            // Return a large value to indicate that the move is invalid
            return Integer.MAX_VALUE;
        }

        int delta = deltaState.getDeltaWhenAdding(day, period, room, courseId);

        return currentValue + delta;
    }
    /**
     * Gets the value of the solution if the given time slot and room is emptied
     * @return The value of the modified solution, or Integer.MAX_VALUE if a constraint is violated
     */
    protected int valueIfRemovingCourse(Schedule schedule, int currentValue, int day, int room, int period) {
        // Room must currently be occupied
        int currentCourse = schedule.assignments[day][period][room];
        if (currentCourse == Heuristic.EMPTY_ROOM) {
            return Integer.MAX_VALUE;
        }

        // Return the delta value plus the current value
        int delta = deltaState.getDeltaWhenRemoving(day, period, room, currentCourse);
        return currentValue + delta;
    }

    /**
     * Gets the value of the solution if two lectures are swapped
     * @return The value of the modified solution, or Integer.MAX_VALUE if a constraint is violated
     */
    protected int valueIfSwappingCourses(Schedule schedule, int currentValue, int day, int period,int room,int day2,int period2,int room2) {
        // Both rooms must currently be occupied
        int currentCourse = schedule.assignments[day][period][room];
        int currentCourse2 = schedule.assignments[day2][period2][room2];

        if (currentCourse == EMPTY_ROOM || currentCourse2 == EMPTY_ROOM)
            return Integer.MAX_VALUE;

        int totalDelta = 0;

        // First clear both rooms -- this does not require constraint validation
        totalDelta += deltaState.getDeltaWhenRemoving(day, period, room, currentCourse);
        removeCourse(schedule, day, period, room);

        totalDelta += deltaState.getDeltaWhenRemoving(day2, period2, room2, currentCourse2);
        removeCourse(schedule, day2, period2, room2);

        // Then check the delta values if adding the swapped courses
        boolean constraintsSatisfied;
        totalDelta += deltaState.getDeltaWhenAdding(day2, period2, room2, currentCourse);
        constraintsSatisfied = deltaState.deltaValidateAllConstraints(day2, period2, currentCourse);

        if (constraintsSatisfied) {
            // only compute the second delta if the first constraint is satistified
            totalDelta += deltaState.getDeltaWhenAdding(day, period, room, currentCourse2);
            constraintsSatisfied = deltaState.deltaValidateAllConstraints(day, period, currentCourse2);
        }

        // Revert the changes by reassigning the courses. Then return the computed value.
        assignCourse(schedule, day, period, room, currentCourse);
        assignCourse(schedule, day2, period2, room2, currentCourse2);

        if (!constraintsSatisfied) {
            return Integer.MAX_VALUE;
        }

        return currentValue + totalDelta;
    }

    /**
     * Assigns the given course to the given room and time slot.
     * If the room is already occupied, this method does nothing.
     */
    protected void assignCourse(Schedule schedule, int day, int period, int room, int course) {
        // Make sure the room is empty
        removeCourse(schedule, day, period, room);
        if(course == Heuristic.EMPTY_ROOM)
        	return;

        // Perform the assignment and increment the counter
        schedule.assignments[day][period][room] = course;
        deltaState.courseAssignmentCount[course]++;

        int lecturesOnDay = ++deltaState.courseLecturesOnDay[course][day];
        if (lecturesOnDay == 1)
            deltaState.courseWorkingDays[course]++;
        deltaState.lecturesInRoomForCourse[room][course]++;

        for (int q : curriculum.curriculaForCourse.get(course)) {
            deltaState.curriculumAssigned[q][day][period]++;
        }

        deltaState.lecturerBusy[courses.lecturerForCourse[course]][day][period]++;
    }

    /**
     * Empties the given room in the given time slot.
     * If the room is already empty, this method does nothing.
     */
    protected void removeCourse(Schedule schedule, int day, int period, int room) {
        int assignedCourse = schedule.assignments[day][period][room];
        if (assignedCourse == Heuristic.EMPTY_ROOM)
            return;

        // Perform the assignment and decrement the counter
        schedule.assignments[day][period][room] = EMPTY_ROOM;
        deltaState.courseAssignmentCount[assignedCourse]--;

        int lecturesOnDay = --deltaState.courseLecturesOnDay[assignedCourse][day];
        if (lecturesOnDay == 0)
            deltaState.courseWorkingDays[assignedCourse]--;
        deltaState.lecturesInRoomForCourse[room][assignedCourse]--;

        for (int q : curriculum.curriculaForCourse.get(assignedCourse)) {
            deltaState.curriculumAssigned[q][day][period]--;
        }

        deltaState.lecturerBusy[courses.lecturerForCourse[assignedCourse]][day][period]--;
    }

    protected enum Type { REMOVE, ASSIGN, SWAP, NOTHING }
    protected CSVWriter writer = null;
    protected Writer f;

    protected DeltaEvaluationState deltaState = new DeltaEvaluationState();

    protected class DeltaEvaluationState {
        final int UNSCHEDULED_PENALTY = 10;
        final int ROOM_CAPACITY_PENALTY = 1; // per student
        final int MINIMUM_WORKING_DAYS_PENALTY = 5;
        final int CURRICULUM_COMPACTNESS_PENALTY = 2;

        /**
         * Get the change in Unscheduled penalty if a lecture is assigned in the given course.
         */
        private int getUnscheduledPenaltyAfterAdding(int course) {
            // Get the current number of lectures, and the minimum number allowed
            int assignments = courseAssignmentCount[course];
            int minimumAssignments = courses.numberOfLecturesForCourse[course];

            // If we are already at the minimum, adding an extra assignment does not change the solution value
            if (assignments >= minimumAssignments)
                return 0;

            // But in other cases the solution value is reduced by one UNSCHEDULED_PENALTY
            return -UNSCHEDULED_PENALTY;
        }

        /**
         * Get the change in Unscheduled penalty if one lecture of the given course is removed.
         */
        private int getUnscheduledPenaltyAfterRemoving(int course) {
            // Get the current number of lectures, and the minimum number allowed
            int assignments = courseAssignmentCount[course];
            int minimumAssignments = courses.numberOfLecturesForCourse[course];

            // If we are already above the minimum, adding an extra lecture does not change the solution value
            if (assignments > minimumAssignments)
                return 0;

            // But in other cases the solution value is increased by one UNSCHEDULED_PENALTY
            return UNSCHEDULED_PENALTY;
        }

        /**
         * Gets the change in RoomCapacity penalty if one lecture in the given course is scheduled in the given room.
         */
        private int roomCapacityPenaltyAfterAdding(int room, int course) {
            int numberOfStudents = courses.numberOfStudentsForCourse[course];
            int roomCapacity = rooms.capacityForRoom[room];

            return Math.max((numberOfStudents - roomCapacity) * ROOM_CAPACITY_PENALTY, 0);
        }

        /**
         * Gets the change in RoomCapacity penalty if one lecture in the given course is removed from the given room.
         */
        private int roomCapacityPenaltyAfterRemoving(int room, int course) {
            return -roomCapacityPenaltyAfterAdding(room, course);
        }

        /**
         * The total number of times that each course has been scheduled.
         */
        public int[] courseAssignmentCount;

        private int[][] courseLecturesOnDay;

        private int[] courseWorkingDays;

        public int[][] lecturesInRoomForCourse;

        /**
         * Look up whether a lecturer is busy on a give time using [lecturer][day][period]
         */
        public byte[][][] lecturerBusy;

        /**
         * Look up whether a given curriculum is assigned on a day and period.
         * Use indexing [curriculum][day][period]
         */
        private byte[][][] curriculumAssigned;

        public void initialize(Schedule schedule) {
            curriculumAssigned = new byte[basicInfo.curricula][basicInfo.days][basicInfo.periodsPerDay];
            courseAssignmentCount = getCourseAssignmentCount(schedule);
            courseLecturesOnDay = new int[basicInfo.courses][basicInfo.days];
            courseWorkingDays = new int[basicInfo.courses];
            lecturesInRoomForCourse = new int[basicInfo.rooms][basicInfo.courses];
            lecturerBusy = new byte[basicInfo.lecturers][basicInfo.days][basicInfo.periodsPerDay];

            for (int day = 0; day < basicInfo.days; day++) {
                for (int period = 0; period < basicInfo.periodsPerDay; period++) {
                    for (int room = 0; room < basicInfo.rooms; room++) {
                        int course = schedule.assignments[day][period][room];
                        if (course == EMPTY_ROOM)
                            continue;

                        courseLecturesOnDay[course][day]++;
                        lecturesInRoomForCourse[room][course]++;

                        int lecturerId = courses.lecturerForCourse[course];
                        lecturerBusy[lecturerId][day][period]++;
                        assert lecturerBusy[lecturerId][day][period] == 1;

                        for (int q : curriculum.curriculaForCourse.get(course)) {
                            curriculumAssigned[q][day][period] = 1;
                        }
                    }
                }
            }

            for (int course = 0; course < basicInfo.courses; course++) {
                for (int day = 0; day < basicInfo.days; day++) {
                    if (courseLecturesOnDay[course][day] > 0)
                        courseWorkingDays[course]++;
                }
            }
        }

        /**
         * Gets the change in MinimumWorkingDays penalty by scheduling one lecture in the given course on the given day.
         */
        public int minWorkingDaysPenaltyAfterAdding(int day, int course) {
            // What is the number of course lectures scheduled on this day?
            int lecturesScheduled = courseLecturesOnDay[course][day];

            // If the course is already scheduled on this day, there is no change
            // So the delta is 0
            if (lecturesScheduled > 0)
                return 0;

            int minimumWorkingDays = courses.minimumWorkingDaysForCourse[course];
            int currentWorkingDays = courseWorkingDays[course];

            // If we are already at a sufficient number of working days, adding one does not reduce solution value
            if (minimumWorkingDays <= currentWorkingDays)
                return 0;

            // Decreasing the working days deficit will reduce the penalty
            return -MINIMUM_WORKING_DAYS_PENALTY;
        }

        public int minWorkingDaysPenaltyAfterRemoving(int day, int course) {
            // What is the number of course lectures scheduled on this day?
            int lecturesScheduled = courseLecturesOnDay[course][day];

            // If the course is already scheduled at least twice on this day, there is no change
            // So the delta is 0
            if (lecturesScheduled > 1)
                return 0;

            int minimumWorkingDays = courses.minimumWorkingDaysForCourse[course];
            int currentWorkingDays = courseWorkingDays[course];

            // If we are already at a sufficient number of working days, removing one does not increase solution value
            if (minimumWorkingDays < currentWorkingDays)
                return 0;

            // Increasing the working days deficit will increase the penalty
            return MINIMUM_WORKING_DAYS_PENALTY;
        }

        public int roomStabilityPenaltyAfterAdding(int room, int course) {
            // If this is the first lecture for the course, or the room is already in use by the course,
            // there is no change -- return 0
            if (courseAssignmentCount[course] == 0 || lecturesInRoomForCourse[room][course] > 0)
                return 0;

            // If we get to this point, the change means an extra room in use
            return 1;
        }

        public int roomStabilityPenaltyAfterRemoving(int room, int course) {
            // If this is the first lecture for the course, or the room is already in use by the course
            // for several lectures, there is no change -- return 0
            if (courseAssignmentCount[course] == 1 || lecturesInRoomForCourse[room][course] > 1)
                return 0;

            // If we get to this point, the change means an extra room in use
            return -1;
        }

        public int curriculumCompactnessPenaltyAfterAdding(int day, int period, int course) {
            LinkedList<Integer> curriculaForCourse = curriculum.curriculaForCourse.get(course);

            int ownSeclusionPenalty = 0;
            int otherSeclusionPenalty = 0;

            for (int curriculum : curriculaForCourse) {
                // Determine whether the added course is secluded with respect to this curriculum
                boolean hasNeighbor = false;
                boolean earlierNeighorIsSecluded = false;
                boolean hasNeighborEarlier = false;
                boolean hasNeighborLater = false;
                boolean laterNeighborIsSecluded = false;
                if (period > 0) {
                    hasNeighborEarlier = curriculumAssigned[curriculum][day][period - 1] > 0;

                    if (hasNeighborEarlier) {
                        // We have an earlier neighbor -- compute whether that neighbor already had a neighbor

                        earlierNeighorIsSecluded = period < 2 || curriculumAssigned[curriculum][day][period - 2] == 0;
                    }
                    hasNeighbor = hasNeighborEarlier;
                }

                if (period + 1 < basicInfo.periodsPerDay) {
                    hasNeighborLater = curriculumAssigned[curriculum][day][period + 1] > 0;

                    if (hasNeighborLater) {
                        // We have a later neighbor -- compute whether that neighbor was already secluded
                        laterNeighborIsSecluded = period > basicInfo.periodsPerDay - 3 || curriculumAssigned[curriculum][day][period + 2] == 0;
                    }

                    hasNeighbor = hasNeighbor || hasNeighborLater;
                }

                if (!hasNeighbor)
                    ownSeclusionPenalty += CURRICULUM_COMPACTNESS_PENALTY;

                if (hasNeighborEarlier && earlierNeighorIsSecluded)
                    otherSeclusionPenalty -= CURRICULUM_COMPACTNESS_PENALTY;

                if (hasNeighborLater && laterNeighborIsSecluded)
                    otherSeclusionPenalty -= CURRICULUM_COMPACTNESS_PENALTY;
            }

            return ownSeclusionPenalty + otherSeclusionPenalty;
        }

        public int curriculumCompactnessPenaltyAfterRemoving(int day, int period, int course) {
            LinkedList<Integer> curriculaForCourse = curriculum.curriculaForCourse.get(course);

            int ownSeclusionPenalty = 0;
            int otherSeclusionPenalty = 0;

            for (int curriculum : curriculaForCourse) {
                // Determine whether the added course is secluded with respect to this curriculum
                boolean hasNeighbor = false;
                boolean earlierNeighorIsSecluded = false;
                boolean hasNeighborEarlier = false;
                boolean hasNeighborLater = false;
                boolean laterNeighborIsSecluded = false;
                if (period > 0) {
                    hasNeighborEarlier = curriculumAssigned[curriculum][day][period - 1] > 0;

                    if (hasNeighborEarlier) {
                        // We have an earlier neighbor -- compute whether that neighbor already had a neighbor

                        earlierNeighorIsSecluded = period < 2 || curriculumAssigned[curriculum][day][period - 2] == 0;
                    }
                    hasNeighbor = hasNeighborEarlier;
                }

                if (period + 1 < basicInfo.periodsPerDay) {
                    hasNeighborLater = curriculumAssigned[curriculum][day][period + 1] > 0;

                    if (hasNeighborLater) {
                        // We have a later neighbor -- compute whether that neighbor was already secluded
                        laterNeighborIsSecluded = period > basicInfo.periodsPerDay - 3 || curriculumAssigned[curriculum][day][period + 2] == 0;
                    }

                    hasNeighbor = hasNeighbor || hasNeighborLater;
                }

                if (!hasNeighbor)
                    ownSeclusionPenalty -= CURRICULUM_COMPACTNESS_PENALTY;

                if (hasNeighborEarlier && earlierNeighorIsSecluded)
                    otherSeclusionPenalty += CURRICULUM_COMPACTNESS_PENALTY;

                if (hasNeighborLater && laterNeighborIsSecluded)
                    otherSeclusionPenalty += CURRICULUM_COMPACTNESS_PENALTY;
            }

            return ownSeclusionPenalty + otherSeclusionPenalty;
        }

        public int getDeltaWhenAdding(int day, int period, int room, int course) {
            int penaltyDelta = 0;

            // Unscheduled
            penaltyDelta += getUnscheduledPenaltyAfterAdding(course);

            // RoomCapacity
            penaltyDelta += roomCapacityPenaltyAfterAdding(room, course);

            // MinimumWorkingDays
            penaltyDelta += minWorkingDaysPenaltyAfterAdding(day, course);

            // CurriculumCompactness
            penaltyDelta += curriculumCompactnessPenaltyAfterAdding(day, period, course);

            // RoomStability
            penaltyDelta += roomStabilityPenaltyAfterAdding(room, course);

            return penaltyDelta;
        }


        public int getDeltaWhenRemoving(int day, int period, int room, int course) {
            int penaltyDelta = 0;

            // Unscheduled
            penaltyDelta += getUnscheduledPenaltyAfterRemoving(course);

            // RoomCapacity
            penaltyDelta += roomCapacityPenaltyAfterRemoving(room, course);

            // MinimumWorkingDays
            penaltyDelta += minWorkingDaysPenaltyAfterRemoving(day, course);

            // CurriculumCompactness
            penaltyDelta += curriculumCompactnessPenaltyAfterRemoving(day, period, course);

            // RoomStability
            penaltyDelta += roomStabilityPenaltyAfterRemoving(room, course);

            return penaltyDelta;
        }

        /**
         * Checks that a given course can be added in the given time slot without violating the SameCurriculum constraint.
         * @return true if the constraint is satisfied
         */
        public boolean deltaValidateSameCurriculumConstraint(int day, int period, int course) {
            LinkedList<Integer> curricula = curriculum.curriculaForCourse.get(course);
            for (int curriculum : curricula) {
                if (curriculumAssigned[curriculum][day][period] > 0)
                    return false;
            }
            return true;
        }

        /**
         * Checks that a given course can be added in the given time slot without violating the SameLecturer constraint.
         * @return true if the constraint is satisfied
         */
        public boolean deltaValidateSameLecturerConstraint(int day, int period, int course) {
            int lecturer = courses.lecturerForCourse[course];
            return lecturerBusy[lecturer][day][period] == 0;
        }

        /**
         * Checks that a given course can be added in the given time slot without violating the Unavailability constraint.
         * @return true if the constraint is satisfied
         */
        public boolean deltaValidateUnavailabilityConstraint(int day, int period, int course) {
            return !unavailability.courseUnavailable[day][period][course];
        }

        /**
         * Checks that a lecture can be added for a given course without violating the MaximimumLectures constraint
         * @return true if the constraint is not violated by the current schedule
         */
        public boolean deltaValidateMaximumScheduleCountConstraint(int course) {
            return courseAssignmentCount[course] < courses.numberOfLecturesForCourse[course];
        }

        /**
         * Checks that all constraints are satisfied if the given course is assigned to the given time slot.
         * @return true if the assignment does not violate constraint, false otherwise.
         */
        public boolean deltaValidateAllConstraints(int day, int period, int course) {
            boolean allConstraintsSatisfied;

            allConstraintsSatisfied = deltaValidateMaximumScheduleCountConstraint(course);
            allConstraintsSatisfied = allConstraintsSatisfied && deltaValidateUnavailabilityConstraint(day, period, course);
            allConstraintsSatisfied = allConstraintsSatisfied && deltaValidateSameLecturerConstraint(day, period, course);
            allConstraintsSatisfied = allConstraintsSatisfied && deltaValidateSameCurriculumConstraint(day, period, course);

            return allConstraintsSatisfied;
        }
    }
}
