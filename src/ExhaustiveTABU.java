public class ExhaustiveTABU extends TABUHeuristic {
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
                        if (newValue < result.scheduleValueAfterApplying) {
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
                    } else {
                        for (int course = 0; course < basicInfo.courses; course++) {
                            int newValue = valueIfAssigningCourse(currentSchedule, currentScheduleValue, day, period, room, course);
                            if (newValue < result.scheduleValueAfterApplying) {
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
