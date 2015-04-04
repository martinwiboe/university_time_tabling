import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Lecturers {
    public int[] lecturers;

    public void loadFromFile(String file) throws FileNotFoundException {
        Scanner scanner = null;
        List idList = new LinkedList<Integer>();

        try {
            scanner = new Scanner(new BufferedReader(new FileReader(file)));
            // Skip the first line
            scanner.nextLine();
            //scanner.useDelimiter("\\s*L");

            // Read tokens from the file
            // Each line contains an id
            System.out.println("Starting scan...");
            String pattern = "L\\d+";
            while (scanner.hasNext(pattern)) {
                String line = scanner.next(pattern);
                int id = Integer.parseInt(line.substring(1));
                idList.add(id);
            }

            lecturers = new int[idList.size()];
            Iterator<Integer> iter = idList.iterator();
            for (int i=0; iter.hasNext(); i++) {
                lecturers[i] = iter.next();
            }

        } finally {
            if (scanner != null)
                scanner.close();
        }
    }
}
