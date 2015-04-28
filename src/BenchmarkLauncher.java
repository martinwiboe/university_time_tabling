import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.Writer;

public class BenchmarkLauncher implements Runnable {
    public int iteration = 0;
    public String[] arguments;
    public CSVWriter writer;

    @Override
    public void run() {
        try {
            // write results to a CSV file
            //Writer fi = new FileWriter("output_" + iteration + ".csv");
            //CSVWriter w = new CSVWriter(fi, ',', CSVWriter.NO_QUOTE_CHARACTER);

            UniversityTimeTabling t = new UniversityTimeTabling();
            t.enableBenchmarking = true;
            t.writer = writer;
            t.startWithParameters(arguments);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
