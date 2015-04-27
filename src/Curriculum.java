import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
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

    /**
     * Contains a list of curricula for each course.
     */
    ArrayList<LinkedList<Integer>> curriculaForCourse;

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

            curriculaForCourse = new ArrayList<LinkedList<Integer>>(numberOfCourses);
            for (int i = 0; i < numberOfCourses; i++)
                curriculaForCourse.add(new LinkedList<Integer>());

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
                curriculaForCourse.get(courseId).add(curriculumId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
