import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rooms {
    /**
     * Look up whether a course is unavailable on [day][period][course]
     */
    public int[] capacityForRoom;


    public void loadFromFile(String file, int numberOfRooms) throws FileNotFoundException {
        capacityForRoom = new int[numberOfRooms];

        Pattern linePattern = Pattern.compile("R(\\d+) (\\d+)");

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

                int room = Integer.parseInt(match.group(1));
                int capacity = Integer.parseInt(match.group(2));
                capacityForRoom[room] = capacity;
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
    }
}
