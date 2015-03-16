import sun.misc.Regexp;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin on 16-03-2015.
 */
public class Curriculum {
    boolean isCourseInCurriculum[][];

    public void loadFromFile(String curriculaFile, String relationFile, int numberOfCourses) {
        // Load the list of curricula
        try {
            Reader r = new FileReader(curriculaFile);
            BufferedReader in = new BufferedReader(r);
            Pattern p = Pattern.compile("Q(\\d{4}) (\\d)");
            String latestLine;
            int curriculaCount = 0;
            while ((latestLine = in.readLine()) != null) {
                Matcher m = p.matcher(latestLine);
                if (!m.matches())
                    continue;
                curriculaCount = 1 + Integer.parseInt(m.group(1));
            }

            isCourseInCurriculum = new boolean[numberOfCourses][curriculaCount];
            in.close();
            r.close();

            Pattern relationPattern = Pattern.compile("Q(\\d{4}) C(\\d{4})");
            r = new FileReader(relationFile);
            in = new BufferedReader(r);
            while ((latestLine = in.readLine()) != null) {
                Matcher m = relationPattern.matcher(latestLine);
                if (!m.matches())
                    continue;

                int curriculumId = Integer.parseInt(m.group(1));
                int courseId = Integer.parseInt(m.group(2));
                isCourseInCurriculum[courseId][curriculumId] = true;
            }

            System.out.println("Max curriculum: " + ((Integer)curriculaCount).toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
