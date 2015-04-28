import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * Created by Martin on 16-03-2015.
 */
public class UniversityTimeTabling {

    /**
     * This method is called when the program is started from the command line
     * @param args Command-line arguments
     */
    public static void main(String[] args) throws Exception {
        // If we have less than 8 args, we are running in normal "non-benchmark" mode
        // Simply solve the given problem and exit
        if (args.length <= 8) {
            // Simply solve the problem without benchmarking
            new UniversityTimeTabling().startWithParameters(args);
            return;
        }

        // Check that "benchmark" is present
        if (!args[0].equals("benchmark")) {
            print("Too many arguments -- use parameter benchmark <run_count> to benchmark");
            return;
        }

        // solve the problem several times - and output intermediate results
        int iterationCount = Integer.parseInt(args[1]);

        // "cut off" the first two parameters
        String[] arguments = new String[args.length - 1];
        Arrays.asList(args).subList(2, args.length).toArray(arguments);

        Writer fi = new FileWriter("output.csv");
        CSVWriter w = new CSVWriter(fi, ',', CSVWriter.NO_QUOTE_CHARACTER);


        // SET THIS TO THE AMOUNT OF LOGICAL PROCESSORS IN YOUR MACHINE
        int threadCount = Runtime.getRuntime().availableProcessors();
        System.out.println("Running " + threadCount + " simultaneous benchmarks");
        int i = 0;
        Thread[] threads = new Thread[threadCount];
        while (i < iterationCount)
        {
            int j = 0;
            while (j < threadCount && i < iterationCount) {
                BenchmarkLauncher b = new BenchmarkLauncher();
                b.arguments = arguments;
                b.iteration = i;
                b.writer = w;

                Thread t = new Thread(b);
                threads[j] = t;
                t.start();
                j++;
                i++;
            }

            for (int k = 0; k < j; k++)
                threads[k].join();
        }

        w.flush();
        fi.close();
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
        Heuristic heuristic = new ExhaustiveTABU(); // StochasticTABU(20);
        heuristic.setTimeout(timeout);
        print("Using heuristic " + heuristic.getClass().getSimpleName());
        print("Running for " + timeout + " seconds");

        // Load basic info about the problem
        debug("Loading basic info...");
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.loadFromFile(basicFile);
        heuristic.basicInfo = basicInfo;

        // Load the curriculum<->course mappings
        debug("Loading curricula...");
        heuristic.curriculum = new Curriculum();
        heuristic.curriculum.loadFromFile(curriculaFile, relationFile, basicInfo.courses);

        // Load the list of lecturers
        debug("Loading lecturers...");
        heuristic.lecturers = new Lecturers();
        heuristic.lecturers.loadFromFile(lecturersFile);

        // Load the list of courses
        debug("Loading courses...");
        heuristic.courses = new Courses();
        heuristic.courses.loadFromFile(coursesFile, basicInfo.courses);

        // Load unavailability times
        debug("Loading unavailability...");
        heuristic.unavailability = new Unavailability();
        heuristic.unavailability.loadFromFile(unavailabilityFile, basicInfo.days, basicInfo.periodsPerDay, basicInfo.courses);

        // Load rooms capacity
        debug("Loading rooms...");
        heuristic.rooms = new Rooms();
        heuristic.rooms.loadFromFile(roomsFile, basicInfo.rooms);

        debug("Done loading problem definition");

        debug("Generating initial solution...");
        Schedule initialSchedule = heuristic.getRandomInitialSolution();
        debug("Starting search...");

        heuristic.deltaState.initialize(initialSchedule);
        Schedule solution = heuristic.search(initialSchedule);

        int objectiveValue;
        debug("Calculating objective value");
        objectiveValue = heuristic.evaluationFunction(solution);

        print("Found a solution in " + heuristic.iterationCount + " iterations");
        print("The value is " + objectiveValue);

        if (!enableBenchmarking) {
            print(solution.toString());
        }

        if (enableBenchmarking && writer != null) {
            synchronized (writer) {
                // TODO write results to a CSV file
                String[] result = new String[]{"" + objectiveValue, heuristic.getClass().getSimpleName(), heuristic.iterationCount + ""};
                writer.writeNext(result);
            }
        }
    }

    /**
     * Indicates whether to include detailed logging in the output.
     */
    public static boolean enableDebugOutput = false;

    /**
     * Print a debug message, if debug output is enabled.
     * @param message The message to print
     */
    private static void debug(String message) {
        if (enableDebugOutput)
            System.out.println(message);
    }

    /**
     * Print an output message.
     * @param message The message to print
     */
    private static void print(String message) {
        System.out.println(message);
    }
}

