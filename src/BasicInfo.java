import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class BasicInfo {
    public int courses, rooms, days, periodsPerDay, curricula, constraints, lecturers;

    public void loadFromFile(String file) throws FileNotFoundException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(file)));
            // Skip the first line
            scanner.nextLine();

            // Read tokens from the file in the order
            // Courses Rooms Days Periods_per_day Curricula Constraints Lecturers
            courses = scanner.nextInt(10);
            rooms = scanner.nextInt(10);
            days = scanner.nextInt(10);
            periodsPerDay = scanner.nextInt(10);
            curricula = scanner.nextInt(10);
            constraints = scanner.nextInt(10);
            lecturers = scanner.nextInt(10);

        } finally {
            if (scanner != null)
                scanner.close();
        }
    }
}
