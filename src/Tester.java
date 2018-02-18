import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tester {
	private static Map<Double, Double> timeStatistics = new HashMap<>();
	private static Map<Double, List<Pattern>> patternsForSupport = new HashMap<>();

	public static void main(String[] args) {
		try {
			System.setOut(new PrintStream(new FileOutputStream("output.txt")));
			List<List<String>> database = readData("data/input.txt");

			int maxSupport = 9, minSupport = 1;
			for (int i = maxSupport; i >= minSupport; i--) {
				double minSupportPercent = i / 10.0;
				getFrequentPatterns(minSupportPercent, database);
			}

			System.out.println("Support vs RunTime Statistics");
			System.out.println("---------------------------------------");
			System.out.println("Support\t\tTime\t\tNo. of Patterns");
			System.out.println("---------------------------------------");
			for (int i = maxSupport; i >= minSupport; i--) {
				if (i <= 7) {
					double minSupportPercent = i / 10.0;
					System.out.printf("%.1f\t\t\t%.5fs\t\t%d\n", minSupportPercent,
							timeStatistics.get(minSupportPercent), patternsForSupport.get(minSupportPercent).size());
				}
			}
			System.out.println("---------------------------------------");
			for (int i = maxSupport; i >= minSupport; i--) {
				double minSupportPercent = i / 10.0;
				List<Pattern> patterns = patternsForSupport.get(minSupportPercent);
				int minSupportCount = (int) (minSupportPercent * database.size());
				if (i <= 7) {
					System.out.println("\nSupport = " + minSupportPercent + "% or count = " + minSupportCount
							+ ". Patterns found  = " + patterns.size());
					System.out.println("-----------------------------------");
					for (Pattern freqPattern : patterns)
						System.out.println(freqPattern);
				}
			}
		} catch (IOException e) {
			System.err.println("IOException");
		}
	}

	private static void getFrequentPatterns(double minSupportPercent, List<List<String>> database) {
		long elapsedTime = 0;

		long startTime = System.nanoTime();

		int minSupportCount = (int) (minSupportPercent * database.size());
		// System.out.println("Support: " + minSupportCount);

		// System.out.println("Finding frequent Itemsets...");
		List<Pattern> patterns = new DHPApriori(database, minSupportCount).getAllFrequentItemsets();
		elapsedTime += System.nanoTime() - startTime;

		timeStatistics.put(minSupportPercent, elapsedTime / 1E9);
		patternsForSupport.put(minSupportPercent, patterns);
	}

	private static List<List<String>> readData(String fileName) throws IOException {
		List<List<String>> db = new ArrayList<List<String>>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			String line = br.readLine();
			while (line != null) {
				db.add(Arrays.asList(line.split(" ")));
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		return db;
	}
}
