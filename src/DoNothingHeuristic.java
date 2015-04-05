/**
 * Created by Martin on 05-04-2015.
 */
public class DoNothingHeuristic extends Heuristic {
    @Override
    public Schedule search(Schedule schedule) {
        startCountdown();

        while(timeoutReached() == false) {
            // Do nothing.
        }

        return schedule;
    }
}
