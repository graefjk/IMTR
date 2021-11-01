package task_1;

import java.io.File;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Scanner;

public class Searcher {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Searcher searcher = new Searcher();
		searcher.index(new File("twitter.csv"));
		System.out.println(searcher.query("side effects Malaria", "COVID vaccines"));
	}

	HashMap<String, DictEntry> dictonary = new HashMap<String, DictEntry>();
	ArrayList<ArrayList<Long>> postingsLists = new ArrayList<ArrayList<Long>>();

	public void index(File file) throws FileNotFoundException {
		int n = 0;

		Scanner scanner = new Scanner(file);
		long time = System.nanoTime();

		while (scanner.hasNext() && (n < 30000000)) {
			n++;
			if (n % 10000 == 0) {
				System.out.println(n);
			}
			String line = scanner.nextLine();
			String[] columns = line.split("	"); // special whitespace
			// this check is here because of line 2114543, 4115522, 4357319, 4577422,
			// 5520503, 6437018, 6437019, 6512362 in the input file which are incorrectly
			// formated
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

		time1Sum += System.nanoTime() - time1;

		time2 = System.nanoTime();
		if (!dictonary.containsKey(token)) { // 0.0563824 whole loop
			time3 = System.nanoTime();
			dictonary.put(token, new DictEntry(1, makePostingsList(id)));
			time3Sum += System.nanoTime() - time3;
			return;
		}
		time2Sum += System.nanoTime() - time2;

		time4 = System.nanoTime();
		int postingListPos = dictonary.get(token).postingListPos; // 0.0667442
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
			dictonary.get(token).frequency++; // 0.0759479
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

	public class DictEntry {
		public int frequency;
		public int postingListPos;

		public DictEntry(int frequency, int postingListPos) {
			this.frequency = frequency;
			this.postingListPos = postingListPos;
		}

	}

	public ArrayList<Long> query(String term) {
		String[] splitTerm = term.split(" ");
		if (splitTerm.length > 1) {
			return query(Arrays.asList(term.split(" ")));
		}

		// System.out.println(normalizedTerm);
		int position = dictonary.get(normalize(term)).postingListPos;
		// System.out.println(position);
		if (position >= 0) {
			return postingsLists.get(position);
		}
		return new ArrayList<Long>();
	}

	public ArrayList<Long> query(String term1, String term2) {
		ArrayList<String> queryList = new ArrayList<String>();
		queryList.addAll(Arrays.asList(term1.split(" ")));
		queryList.addAll(Arrays.asList(term2.split(" ")));
		System.out.println(queryList);
		return query(queryList);
	}

	public ArrayList<Long> query(List<String> terms) {
		List<String> tempList = terms;
		switch (tempList.size()) {
		case 0:
			return new ArrayList<Long>();
		case 1:
			return query(tempList.remove(0));
		case 2:
			return intersect(query(tempList.remove(0)), query(tempList.remove(0)));
		default:
			String term0 = tempList.remove(0);
			return intersect(query(term0), query(tempList));
		}
	}

	public ArrayList<Long> intersect(ArrayList<Long> list1, ArrayList<Long> list2) {
		Iterator<Long> list1Iterator = list1.iterator();
		Iterator<Long> list2Iterator = list2.iterator();
		ArrayList<Long> intersection = new ArrayList<Long>();

		if (!list1Iterator.hasNext() || !list2Iterator.hasNext()) {
			return intersection;
		}

		long posting1 = list1Iterator.next();
		long posting2 = list2Iterator.next();

		while (true) {
			// System.out.println(posting1 + " " + posting2);

			if (posting1 == posting2) {
				intersection.add(posting1);
				if (list1Iterator.hasNext()) {
					posting1 = list1Iterator.next();
				} else {
					break;
				}
				if (list2Iterator.hasNext()) {
					posting2 = list2Iterator.next();
				} else {
					break;
				}
			}
			if (posting1 < posting2) {
				if (list1Iterator.hasNext()) {
					posting1 = list1Iterator.next();
					continue;
				} else {
					break;
				}
			}
			if (posting1 > posting2) {
				if (list2Iterator.hasNext()) {
					posting2 = list2Iterator.next();
					continue;
				} else {
					break;
				}
			}
		}

		return intersection;

	}

}
