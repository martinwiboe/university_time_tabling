
import java.io.IOException;
import java.util.LinkedList;

public abstract class TABUHeuristic extends Heuristic {
    protected Schedule currentSchedule, bestSchedule;
    protected int currentScheduleValue, bestScheduleValue;

    public enum OperationType {
        Assign,
        Remove,
        Swap
    }

    public int tabooListLength = 20;

    public class TABUOperation {
        public int day = -1;
        public int period = -1;
        public int room = -1;
        public int course = -1;
        public int otherDay = -1;
        public int otherPeriod = -1;
        public int otherRoom = -1;
        public int otherCourse = -1;

        public OperationType type;
    }

    public class TABUOperationResult {
        public TABUOperation operation;
        public int scheduleValueAfterApplying;
    }

    private void applyOperation(Schedule schedule, TABUOperation operation) {
        if (operation == null)
            return;

        switch (operation.type) {
            case Assign:
                assignCourse(schedule, operation.day, operation.period, operation.room, operation.course);
                break;

            case Remove:
                removeCourse(schedule, operation.day, operation.period, operation.room);
                break;

            case Swap:
                removeCourse(schedule, operation.day, operation.period, operation.room);
                removeCourse(schedule, operation.otherDay, operation.otherPeriod, operation.otherRoom);
                assignCourse(schedule, operation.day, operation.period, operation.room, operation.otherCourse);
                assignCourse(schedule, operation.otherDay, operation.otherPeriod, operation.otherRoom, operation.course);
                break;
        }
    }

    @Override
    public Schedule search(Schedule schedule) throws IOException {
        currentSchedule = schedule;
        currentScheduleValue = evaluationFunction(currentSchedule);

        bestSchedule = new Schedule(basicInfo.days, basicInfo.periodsPerDay, basicInfo.rooms);

        cloneArray(schedule.assignments, bestSchedule.assignments);
        bestScheduleValue = currentScheduleValue;

        startCountdown();

        while(!timeoutReached()) {
            iterationCount++;

            // Determine the operation needed to get the best solution in neighborhood
            TABUOperationResult operation = bestSolutionInNeighborhood();

            // Apply that operation to the currentSchedule
            applyOperation(currentSchedule, operation.operation);
            currentScheduleValue = operation.scheduleValueAfterApplying;

            // If we just encountered a better solution than earlier, store it
            if (currentScheduleValue < bestScheduleValue) {
                cloneArray(schedule.assignments, bestSchedule.assignments);
                bestScheduleValue = currentScheduleValue;
            }
        }

        return bestSchedule;
    }

    protected LinkedList<TABUOperation> tabooList = new LinkedList<TABUOperation>();

    private void addTaboo(TABUOperation operation) {
        tabooList.addLast(operation);
        while (tabooList.size() > tabooListLength)
            tabooList.removeFirst();
    }

    protected boolean isTaboo(TABUOperation operation) {
        switch (operation.type) {
            case Assign:
            case Swap:
                for (TABUOperation taboo : tabooList) {
                    if (taboo.course == operation.course || taboo.course == operation.otherCourse || taboo.otherCourse == operation.course || taboo.otherCourse == operation.otherCourse)
                        return true;
                }
                return false;

            case Remove:
            default:
                for (TABUOperation taboo : tabooList) {
                    if (taboo.day == operation.day && taboo.period == operation.period && taboo.room == operation.room)
                        return true;
                }
                return false;
        }
    }

    protected abstract TABUOperationResult bestSolutionInNeighborhood();
}
