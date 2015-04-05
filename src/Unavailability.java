import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unavailability {
    /**
     * Look up whether a course is unavailable on [day][period][course]
     */
    public boolean[][][] courseUnavailable;
    public List<UnavailabilityConstraint> constraints;

    public void loadFromFile(String file, int numberOfDays, int numberOfPeriods, int numberOfCourses) throws FileNotFoundException {
        courseUnavailable = new boolean[numberOfDays][numberOfPeriods][numberOfCourses];
        constraints = new LinkedList<UnavailabilityConstraint>();

        Pattern linePattern = Pattern.compile("C(\\d+) (\\d+) (\\d+)");

        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(file)));
            // Skip the first line
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                // Read tokens from the file in the order
                // Course Day Period
                Matcher match = linePattern.matcher(scanner.nextLine());

                if (!match.matches())
                    continue;
                UnavailabilityConstraint constraint = new UnavailabilityConstraint();
                constraint.course = Integer.parseInt(match.group(1));
                constraint.day = Integer.parseInt(match.group(2));
                constraint.period  = Integer.parseInt(match.group(3));
                constraints.add(constraint);

                courseUnavailable[constraint.day][constraint.period][constraint.course] = true;
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
    }
}


