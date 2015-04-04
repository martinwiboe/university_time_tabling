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

        // Load basic info about the problem
        print("Loading basic info...");
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.loadFromFile(basicFile);

        // Load the curriculum<->course mappings
        print("Loading curricula...");
        Curriculum c = new Curriculum();
        c.loadFromFile(curriculaFile, relationFile, basicInfo.courses);

        // Load the list of lecturers
        print("Loading lecturers...");
        Lecturers l = new Lecturers();
        l.loadFromFile(lecturersFile);

        // Load the list of courses
        print("Loading courses...");
        Courses courses = new Courses();
        courses.loadFromFile(coursesFile, basicInfo.courses);

        // Load unavailability times
        print("Loading unavailability...");
        Unavailability unavailability = new Unavailability();
        unavailability.loadFromFile(unavailabilityFile, basicInfo.days, basicInfo.periodsPerDay, basicInfo.courses);

        // Load rooms capacity
        print("Loading rooms...");
        Rooms rooms = new Rooms();
        rooms.loadFromFile(roomsFile, basicInfo.rooms);

        print("Done loading problem definition");
    }

    private static void print(String message) {
        System.out.println(message);
    }
}

