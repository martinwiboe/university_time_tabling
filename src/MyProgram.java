/**
 * Created by Martin on 16-03-2015.
 */
public class MyProgram {
    public static void main(String[] args) {
        System.out.println("Hello :)");

        Curriculum c = new Curriculum();
        String baseDir = "tests\\Test01\\";

        c.loadFromFile(baseDir + "curricula.utt", baseDir + "relation.utt", 30);
        System.out.println("Hey");
    }
}
