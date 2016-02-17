public class ExhaustiveTABU extends TABUHeuristic {

    public boolean considerSwaps = true;

    @Override
    protected TABUOperationResult bestSolutionInNeighborhood() {
        TABUOperationResult result = new TABUOperationResult();
        result.scheduleValueAfterApplying = Integer.MAX_VALUE;

        for (int day = 0; day < basicInfo.days; day++) {
            for (int period = 0; period < basicInfo.periodsPerDay; period++) {
                for (int room = 0; room < basicInfo.rooms; room++) {
                    int assignedCourse = currentSchedule.assignments[day][period][room];
                    if (assignedCourse != EMPTY_ROOM) {
                        int newValue = valueIfRemovingCourse(currentSchedule, currentScheduleValue, day, period, room);
                        if (newValue <= result.scheduleValueAfterApplying) {
                            TABUOperation operation = new TABUOperation();
                            operation.day = day;
                            operation.period = period;
                            operation.room = room;
                            operation.type = OperationType.Remove;

                            if (!isTaboo(operation)) {
                                result.operation = operation;
                                result.scheduleValueAfterApplying = newValue;
                            }
                        }

                        // Consider swapping with all other courses
                        if (considerSwaps) {
                            for (int day2 = 0; day2 < basicInfo.days; day2++) {
                                for (int period2 = 0; period2 < basicInfo.periodsPerDay; period2++) {
                                    for (int room2 = 0; room2 < basicInfo.rooms; room2++) {
                                        int otherCourse = currentSchedule.assignments[day2][period2][room2];
                                        if (otherCourse == EMPTY_ROOM || otherCourse <= assignedCourse)
                                            continue;

                                        int newValueAfterSwap = valueIfSwappingCourses(currentSchedule, currentScheduleValue, day, period, room, day2, period2, room2);
                                        if (newValueAfterSwap <= result.scheduleValueAfterApplying) {
                                            TABUOperation operation = new TABUOperation();
                                            operation.day = day;
                                            operation.period = period;
                                            operation.room = room;
                                            operation.course = assignedCourse;
                                            operation.otherDay = day2;
                                            operation.otherPeriod = period2;
                                            operation.otherRoom = room2;
                                            operation.otherCourse = otherCourse;
                                            operation.type = OperationType.Swap;

                                            if (!isTaboo(operation)) {
                                                result.operation = operation;
                                                result.scheduleValueAfterApplying = newValueAfterSwap;
                                            }
                                        }

                                    }
                                }
                            }
                        }

                    } else {
                        for (int course = 0; course < basicInfo.courses; course++) {
                            int newValue = valueIfAssigningCourse(currentSchedule, currentScheduleValue, day, period, room, course);
                            if (newValue <= result.scheduleValueAfterApplying) {
                                TABUOperation operation = new TABUOperation();
                                operation.day = day;
                                operation.period = period;
                                operation.room = room;
                                operation.course = course;
                                operation.type = OperationType.Assign;

                                if (!isTaboo(operation)) {
                                    result.operation = operation;
                                    result.scheduleValueAfterApplying = newValue;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
