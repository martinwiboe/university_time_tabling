import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin on 16-03-2015.
 */
public class Curriculum {
    /**
     * Look up whether there is a relation between [course][curriculum]
     */
    boolean isCourseInCurriculum[][];

    public void loadFromFile(String curriculaFile, String relationFile, int numberOfCourses) {
        // Load the list of curricula
        try {
            // Parse the curricula file
            // Each line has the curriculum number and course count
            Reader reader = new FileReader(curriculaFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            Pattern p = Pattern.compile("Q(\\d{4}) (\\d)");
            String latestLine;
            int curriculaCount = 0;
            while ((latestLine = bufferedReader.readLine()) != null) {
                Matcher m = p.matcher(latestLine);
                if (!m.matches())
                    continue;

                // Index of the curriculum we just parsed
                int curriculumIndex = Integer.parseInt(m.group(1));

                // The number of curricula is always equal to highest index + 1
                curriculaCount = 1 + curriculumIndex;
            }
            bufferedReader.close();
            reader.close();

            // Store assignments as an array of booleans
            isCourseInCurriculum = new boolean[numberOfCourses][curriculaCount];

            // Parse the relation file
            // Each line has the index of a curriculum and a course
            Pattern relationPattern = Pattern.compile("Q(\\d{4}) C(\\d{4})");
            reader = new FileReader(relationFile);
            bufferedReader = new BufferedReader(reader);
            while ((latestLine = bufferedReader.readLine()) != null) {
                Matcher m = relationPattern.matcher(latestLine);
                if (!m.matches())
                    continue;

                // Indices we just read from the file
                int curriculumId = Integer.parseInt(m.group(1));
                int courseId = Integer.parseInt(m.group(2));
                isCourseInCurriculum[courseId][curriculumId] = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
