package task_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Searcher {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Searcher searcher = new Searcher();
		searcher.index(new File("twitter.csv"));

		// System.out.println(searcher.query("capitol"));
		// System.out.println(searcher.query("trump"));
		// System.out.println(searcher.query("capitol", "trump"));
		// System.out.println(searcher.postingsLists.get(16861));
		// System.gc();
		searcher.query("side effects of malaria","COVID vaccines").forEach(e -> System.out.println(e));
	}

	// ArrayList 16.2037631
	// Vector 18.219501
	// Stack 18.2218297

	ArrayList<DictEntry> dictonary = new ArrayList<DictEntry>();
	ArrayList<ArrayList<Long>> postingsLists = new ArrayList<ArrayList<Long>>();

	public void index(File file) throws FileNotFoundException {
		int n = 0;

		Scanner scanner = new Scanner(file);
		long time = System.nanoTime();

		while (scanner.hasNext() && (n < 10000)) {
			n++;
			if (n % 10000 == 0) {
				System.out.println(n);
			}
			String line = scanner.nextLine();
			String[] columns = line.split("	"); // special whitespace
			// this check is here because of line 2114543, 4115522, 4357319, 4577422,
			// 5520503, 6437018, 6437019, 6512362 in the input file which are incorrectly formated
			if (columns.length < 4) {
				System.out.println(columns.length + " " + n);
				continue;
			}
			long id = Long.parseLong(columns[0]);
			String[] tokens = normalize(columns[3]).split(" ");

			
			// some tweets are multiple times in the input csv. If we detect such a double
			// we dont add it a second time.

			for (String token : tokens) {
				if (token.strip().length() != 0) {
					this.add(token, id); // this takes by far the most time;
				}

			}

		}
		System.out.println();
		System.out.println((System.nanoTime() - time) / (double) 1000000000);

		System.out.println(1 + " " + time1Sum / (double) 1000000000);
		System.out.println(2 + " " + time2Sum / (double) 1000000000);
		System.out.println(3 + " " + time3Sum / (double) 1000000000);
		System.out.println(4 + " " + time4Sum / (double) 1000000000);
		System.out.println(5 + " " + time5Sum / (double) 1000000000);
		System.out.println(6 + " " + time6Sum / (double) 1000000000);
		System.out.println(7 + " " + time7Sum / (double) 1000000000);
		System.out.println(8 + " " + time8Sum / (double) 1000000000);

		time = System.nanoTime();
		scanner.close();

//		for (DictEntry entry : dictonary) {
//			System.out.println(entry.term + " " + entry.frequency + " " + entry.postingListPos);
//		}

	}

	public String normalize(String text) {
		return text.replace("[NEWLINE]", " ").replace("[TAB]", " ").replace(".", " ").replace(",", " ")
				.replace(";", " ").replace("’", "'").replace("\"", " ").replace("”", " ").replace("!", " ")
				.replace(":", " ").replace("“", " ").toLowerCase().strip();
	}

	long time1 = System.nanoTime();
	long time2 = System.nanoTime();
	long time3 = System.nanoTime();
	long time4 = System.nanoTime();
	long time5 = System.nanoTime();
	long time6 = System.nanoTime();
	long time7 = System.nanoTime();
	long time8 = System.nanoTime();

	long time1Sum = 0;
	long time2Sum = 0;
	long time3Sum = 0;
	long time4Sum = 0;
	long time5Sum = 0;
	long time6Sum = 0;
	long time7Sum = 0;
	long time8Sum = 0;

	public void add(String token, long id) {

		time1 = System.nanoTime();
		int position = searchDictonary(token); // 1.6210686
		time1Sum += System.nanoTime() - time1;

		time2 = System.nanoTime();
		if (position < 0) { // 0.0563824 whole loop
			time3 = System.nanoTime();
			dictonary.add(-position - 1, new DictEntry(token, 1, makePostingsList(id))); // 0.7777419
			time3Sum += System.nanoTime() - time3;
			return;
		}
		time2Sum += System.nanoTime() - time2;

		time4 = System.nanoTime();
		int postingListPos = dictonary.get(position).postingListPos; // 0.0667442
		time4Sum += System.nanoTime() - time4;

		time5 = System.nanoTime();
		ArrayList<Long> postings = postingsLists.get(postingListPos); // 0.1953765
		time5Sum += System.nanoTime() - time5;


		
		// !postings.contains(id)
		// 8.453592 whole loop with !postings.contains(id), 1.2163975 with
		// (Collections.binarySearch(postings, id) < 0)

		time6 = System.nanoTime();
		int postingPosition = Collections.binarySearch(postings, id);
		time6Sum += System.nanoTime() - time6;
		
		if (postingPosition < 0) { // 8.453592 whole loop with
			time7 = System.nanoTime();
			dictonary.get(position).frequency++; // 0.0759479
			time7Sum += System.nanoTime() - time7;

			time8 = System.nanoTime();
			postings.add(-postingPosition - 1, id);
			time8Sum += System.nanoTime() - time8;
		}

	}

	@SuppressWarnings("serial")
	private int makePostingsList(long id) {
		postingsLists.add(new ArrayList<Long>() {
			{
				add(id);
			}
		});

		// TODO Auto-generated method stub
		return postingsLists.size() - 1;
	}

	public class DictEntry implements Comparable<DictEntry> {
		public final String term;
		public int frequency;
		public int postingListPos;

		public DictEntry(String term, int frequency, int postingListPos) {
			this.term = term;
			this.frequency = frequency;
			this.postingListPos = postingListPos;
		}

		@Override
		public int compareTo(DictEntry entry) {
			return this.term.compareTo(entry.term);
		}

	}

	public int searchDictonary(String searchTerm) {
		return Collections.binarySearch(dictonary, new DictEntry(searchTerm, 0, 0));
	}

	public ArrayList<Long> query(String term) {
		// System.out.println(normalizedTerm);
		int position = dictonary.get(searchDictonary(normalize(term))).postingListPos;
		// System.out.println(position);
		if (position >= 0) {
			return postingsLists.get(position);
		}
		return new ArrayList<Long>();
	}

	public ArrayList<Long> query(String term1, String term2) {
		Iterator<Long> term1Iterator = query(term1).iterator();
		Iterator<Long> term2Iterator = query(term2).iterator();
		ArrayList<Long> intersection = new ArrayList<Long>();

		if (!term1Iterator.hasNext() || !term2Iterator.hasNext()) {
			return intersection;
		}

		long posting1 = term1Iterator.next();
		long posting2 = term2Iterator.next();

		while (true) {
			// System.out.println(posting1 + " " + posting2);

			if (posting1 == posting2) {
				intersection.add(posting1);
				if (term1Iterator.hasNext()) {
					posting1 = term1Iterator.next();
				} else {
					break;
				}
				if (term2Iterator.hasNext()) {
					posting2 = term2Iterator.next();
				} else {
					break;
				}
			}
			if (posting1 < posting2) {
				if (term1Iterator.hasNext()) {
					posting1 = term1Iterator.next();
					continue;
				} else {
					break;
				}
			}
			if (posting1 > posting2) {
				if (term2Iterator.hasNext()) {
					posting2 = term2Iterator.next();
					continue;
				} else {
					break;
				}
			}
		}

		return intersection;
	}

}
