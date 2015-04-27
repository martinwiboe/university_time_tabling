import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by Burak on 08-04-2015.
 */
public class HillClimber extends Heuristic {

    protected Schedule schedule; //current schedule
    //protected Schedule currentSchedule; //the copy of the current schedule where changes are made, that are not certain to be saved
    protected int IterationCount = 0;
    protected int currentValue;
    private Random Rand = new XORShiftRandom();
    private Map<Integer, Integer> unScheduledCourses;

    @Override
    public Schedule search(Schedule schedule) {
        startCountdown();
        currentValue = evaluationFunction(schedule); // value of the current solution
        deltaState.courseAssignmentCount = getCourseAssignmentCount(schedule);
        unScheduledCourses = new HashMap<Integer, Integer>();
        for (int courseNo = 0; courseNo < this.basicInfo.courses; courseNo++) { //checked the unscheduled course and the number of unscheduled lectures
            int numberOfLecturesUnAssigned = this.deltaState.courseAssignmentCount[courseNo] - this.courses.numberOfLecturesForCourse[courseNo];
            if (numberOfLecturesUnAssigned != 0)
                unScheduledCourses.put(courseNo, Math.abs(numberOfLecturesUnAssigned));
        }

        boolean done = false;

        System.out.println("Start");
        while (timeoutReached() == false) { //TODO: we need to deal with the d
            done = true; //Runs while there are still changes being made
            //System.out.println("Iteration Count = " + IterationCount);
            this.IterationCount++; //Adds to the iteration count
            if (this.IterationCount % 1000 == 0)
                System.err.println("Iteration Count  = " + this.IterationCount);
            int currentBestValue = currentValue;

            for (int day = 0; day < this.basicInfo.days; day++) { //run thorough all the days

                for (int period = 0; period < this.basicInfo.periodsPerDay; period++) { //all the periods

                    for (int room = 0; room < this.basicInfo.rooms; room++) { //all the rooms

                        //	System.out.println("day = "+ day +" period = " + period + " room = " + room + " course  = " + schedule.assignments[day][period][room]);
                        for (Map.Entry<Integer, Integer> entry : unScheduledCourses.entrySet()) {
                            int unScheduledCourseNo = entry.getKey();
                            int unScheduledCourseValue = entry.getValue();
                            if (schedule.assignments[day][period][room] == Heuristic.EMPTY_ROOM) {// room is empty
                                int valueIfThisCourseIsAssigned = valueIfAssigningCourse(schedule, currentValue, day, room, period, unScheduledCourseNo);
                                if (valueIfThisCourseIsAssigned <= currentValue) {
                                    assignCourse(schedule, day, period, room, unScheduledCourseNo);
                                    unScheduledCourseValue--;
                                    if (unScheduledCourseValue == 0) {
                                        unScheduledCourses.remove(unScheduledCourseNo);
                                    }
                                }

                            } else { //room is not epmty first we need to check the

                            }


                            // remove the course in current time slot and then add the unscheduled course
                            removeCourse(schedule, day, period, room);
                            assignCourse(schedule, day, period, room, unScheduledCourseNo);
                        }

                    int valueIfThisCourseIsAssigned, valueIfThisCourseIsRemoved, valueIfThisCoursesAreSwapped;
                    int day2, period2, room2;
                    day2 = Rand.nextInt(this.basicInfo.days);
                    room2 = Rand.nextInt(this.basicInfo.rooms);
                    period2 = Rand.nextInt(this.basicInfo.periodsPerDay);
                    valueIfThisCourseIsRemoved = valueIfRemovingCourse(schedule, currentValue, day, room, period);
                    int courseId = Rand.nextInt(this.basicInfo.courses);
                    valueIfThisCourseIsAssigned = valueIfAssigningCourse(schedule, currentValue, day, room, period, courseId);
                    valueIfThisCoursesAreSwapped = valueIfSwappingCourses(schedule, day, period, room, day2, period2, room2);
                    Type change;
                    if (valueIfThisCourseIsRemoved <= valueIfThisCourseIsAssigned) {
                        if (valueIfThisCourseIsRemoved <= valueIfThisCoursesAreSwapped) {
                            if (valueIfThisCourseIsRemoved != Integer.MAX_VALUE)
                                change = Type.REMOVE;
                            else
                                change = Type.NOTHING;
                        } else {
                            change = Type.SWAP;
                        }
                    } else {
                        if (valueIfThisCourseIsAssigned < valueIfThisCoursesAreSwapped)
                            change = Type.ASSIGN;
                        else
                            change = Type.SWAP;
                    }

                    int bestCourse1;
                    int bestCourse2;
                    int deltaval;
                    switch (change) {
                        case REMOVE: {
                            deltaval = valueIfThisCourseIsRemoved - currentValue;
                            if (deltaval < 0) {
                                currentValue += deltaval;
                                removeCourse(schedule, day, period, room);
                            }
                            break;
                        }
                        case ASSIGN: {
                            deltaval = valueIfThisCourseIsAssigned - currentValue;
                            if (deltaval < 0) {
                                currentValue += deltaval;
                                assignCourse(schedule, day, period, room, courseId);
                            }
                            break;
                        }
                        case SWAP: {
                            deltaval = valueIfThisCoursesAreSwapped - currentValue;
                            if (deltaval < 0) {
                                currentValue = valueIfThisCoursesAreSwapped;
                                bestCourse1 = schedule.assignments[day][period][room];//Remembers the person for the taboolist
                                bestCourse2 = schedule.assignments[day2][period2][room2]; //Remembers the person for the taboolist
                                removeCourse(schedule, day2, period2, room2);
                                removeCourse(schedule, day, period, room);
                                assignCourse(schedule, day2, period2, room2, bestCourse1);
                                assignCourse(schedule, day, period, room, bestCourse2);
                            }
                            break;
                        }

                        default:
                            break;
                    }
                }

            }


        }
        if (currentBestValue < currentValue) {
            done = false;
            currentValue = currentBestValue;
        }

    }

    return schedule;
}
}
