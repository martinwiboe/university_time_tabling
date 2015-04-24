import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * Created by Martin on 16-03-2015.
 */
public class UniversityTimeTabling {


    public static void main(String[] args) throws Exception {
        if (args.length == 7 || args.length == 8) {
            // Simply solve the problem without benchmarking
            new UniversityTimeTabling().startWithParameters(args);
        }

        if (args.length > 8 && args[0].equals("benchmark")) {
            // solve the problem several times - and output intermediate results
            int iterationCount = Integer.parseInt(args[1]);

            // write results to a CSV file
            Writer fi = new FileWriter("output.csv");
            CSVWriter w = new CSVWriter(fi, ',', CSVWriter.NO_QUOTE_CHARACTER);

            // "cut off" the first two parameters
            String[] arguments = new String[args.length - 1];
            Arrays.asList(args).subList(2, args.length).toArray(arguments);
            for (int i = 0; i < iterationCount; i++) {
                UniversityTimeTabling t = new UniversityTimeTabling();
                t.enableBenchmarking = true;
                t.writer = w;
                t.startWithParameters(arguments);
            }

            w.flush();
            fi.close();
        }
    }

    public boolean enableBenchmarking = false;
    public CSVWriter writer = null;

    public void startWithParameters(String[] args) throws Exception {
        // Validate input parameter count and length
        if (args == null || args.length < 7) {
            print("Too few arguments -- expecting basic.utt courses.utt lecturers.utt rooms.utt curricula.utt relation.utt unavailability.utt 300");
            System.exit(1);
        }

        // Final argument is the timeout, which is optional and defaults to 300
        int timeout = 300;
        if (args.length >= 8) try {
            timeout = Integer.parseInt(args[7], 10);
        } catch (NumberFormatException nEx) {
        }

        String basicFile = args[0];
        String coursesFile = args[1];
        String lecturersFile = args[2];
        String roomsFile = args[3];
        String curriculaFile = args[4];
        String relationFile = args[5];
        String unavailabilityFile = args[6];

        // Create the heuristic
        Heuristic heuristic = new DoNothingHeuristic();
        heuristic.setTimeout(timeout);
        print("Using heuristic " + heuristic.getClass().getSimpleName());
        print("Running for " + timeout + " seconds");

        // Load basic info about the problem
        print("Loading basic info...");
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.loadFromFile(basicFile);
        heuristic.basicInfo = basicInfo;

        // Load the curriculum<->course mappings
        print("Loading curricula...");
        heuristic.curriculum = new Curriculum();
        heuristic.curriculum.loadFromFile(curriculaFile, relationFile, basicInfo.courses);

        // Load the list of lecturers
        print("Loading lecturers...");
        heuristic.lecturers = new Lecturers();
        heuristic.lecturers.loadFromFile(lecturersFile);

        // Load the list of courses
        print("Loading courses...");
        heuristic.courses = new Courses();
        heuristic.courses.loadFromFile(coursesFile, basicInfo.courses);

        // Load unavailability times
        print("Loading unavailability...");
        heuristic.unavailability = new Unavailability();
        heuristic.unavailability.loadFromFile(unavailabilityFile, basicInfo.days, basicInfo.periodsPerDay, basicInfo.courses);

        // Load rooms capacity
        print("Loading rooms...");
        heuristic.rooms = new Rooms();
        heuristic.rooms.loadFromFile(roomsFile, basicInfo.rooms);

        print("Done loading problem definition");

        print("Generating initial solution...");
        Schedule initialSchedule = heuristic.getRandomInitialSolution();
        print("Starting search...");

        Schedule solution = heuristic.search(initialSchedule);

        int objectiveValue;
        print("Calculating objective value");
        objectiveValue = heuristic.evaluationFunction(initialSchedule);
        print("The value is " + objectiveValue);

        print("Found a solution!");

        if (enableBenchmarking == false) {
            print(solution.toString());
        }

        if (enableBenchmarking && writer != null) {
            // TODO write results to a CSV file
            String[] result = new String[] { "" + objectiveValue, heuristic.getClass().getSimpleName(), heuristic.iterationCount + "" };
            writer.writeNext(result);


        }
    }

    private static void print(String message) {
        System.out.println(message);
    }
}

