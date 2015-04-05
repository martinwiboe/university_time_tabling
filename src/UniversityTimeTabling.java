/**
 * Created by Martin on 16-03-2015.
 */
public class UniversityTimeTabling {
    public static void main(String[] args) throws Exception {
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

        print("Found a solution!");

        print(solution.toString());
    }

    private static void print(String message) {
        System.out.println(message);
    }
}

