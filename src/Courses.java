import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Courses {
    public int[] lecturerForCourse;
    public int[] numberOfLecturesForCourse;
    public int[] minimumWorkingDaysForCourse;
    public int[] numberOfStudentsForCourse;


    public void loadFromFile(String file, int numberOfCourses) throws FileNotFoundException {
        lecturerForCourse = new int[numberOfCourses];
        numberOfLecturesForCourse = new int[numberOfCourses];
        minimumWorkingDaysForCourse = new int[numberOfCourses];
        numberOfStudentsForCourse = new int[numberOfCourses];

        Pattern linePattern = Pattern.compile("C(\\d+) L(\\d+) (\\d+) (\\d+) (\\d+)");

        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(file)));
            // Skip the first line
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                // Read tokens from the file in the order
                // Course Lecturer Number_of_lectures Minimum_working_days Number_of_students
                Matcher match = linePattern.matcher(scanner.nextLine());

                if (!match.matches())
                    continue;

                int courseId = Integer.parseInt(match.group(1));
                lecturerForCourse[courseId] = Integer.parseInt(match.group(2));
                numberOfLecturesForCourse[courseId] = Integer.parseInt(match.group(3));
                minimumWorkingDaysForCourse[courseId] = Integer.parseInt(match.group(4));
                numberOfStudentsForCourse[courseId] = Integer.parseInt(match.group(5));
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
    }
}
