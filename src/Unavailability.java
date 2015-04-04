import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unavailability {
    /**
     * Look up whether a course is unavailable on [day][period][course]
     */
    public boolean[][][] courseUnavailable;


    public void loadFromFile(String file, int numberOfDays, int numberOfPeriods, int numberOfCourses) throws FileNotFoundException {
        courseUnavailable = new boolean[numberOfDays][numberOfPeriods][numberOfCourses];

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

                int courseId = Integer.parseInt(match.group(1));
                int day = Integer.parseInt(match.group(2));
                int period  = Integer.parseInt(match.group(3));
                courseUnavailable[day][period][courseId] = true;
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
    }
}
