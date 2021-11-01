package task_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class Searcher {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Searcher searcher = new Searcher();
		searcher.index(new File("C:\\Users\\janni\\Desktop\\IRTM\\assignments\\twitter.csv"));

		System.out.println(searcher.query("capitol"));
		System.out.println(searcher.query("trump"));
		System.out.println(searcher.query("capitol", "trump"));
		// System.out.println(searcher.postingsLists.get(16861));
		System.gc();
	}

	LinkedList<DictEntry> dictonary = new LinkedList<DictEntry>();
	LinkedList<LinkedList<Long>> postingsLists = new LinkedList<LinkedList<Long>>();

	public void index(File file) throws FileNotFoundException {
		int n = 0;

		Scanner scanner = new Scanner(file);
		LinkedList<TempEntry> tempList = new LinkedList<TempEntry>();
		while (scanner.hasNext() && (n < 50)) {
			System.out.println(n++);
			String line = scanner.nextLine();
			String[] columns = line.split("	"); // special whitespace
			long id = Long.parseLong(columns[0]);
			columns[3] = normalize(columns[3]);
			String[] tokens = columns[3].split(" ");
			// some tweets are multiple times in the input csv. If we detect such a double
			// we dont add it a second time.
			if (Collections.binarySearch(tempList, new TempEntry(id, tokens[0])) >= 0) {
				continue;
			}
			for (String token : tokens) {
				if (token.strip().length() == 0) {
					continue;
				}
				int position = Collections.binarySearch(tempList, new TempEntry(id, token));
				if (position >= 0) {
					tempList.add(position, new TempEntry(id, token));
				} else {
					tempList.add(-position - 1, new TempEntry(id, token));
				}
			}
		}
		scanner.close();
		Iterator<TempEntry> tempIterator = tempList.iterator();

		//tempList.forEach(e -> System.out.println(e.term + " " + e.id));

		TempEntry prevEntry = tempIterator.next();
		this.add(prevEntry.term, prevEntry.id);

		n = 0;
		System.out.println();
		while (tempIterator.hasNext()) {
			// System.out.println(n++);
			TempEntry entry = tempIterator.next();

			if (!entry.equals(prevEntry)) {
				this.add(entry.term, entry.id);
			}
			prevEntry = entry;
		}

		for (DictEntry entry : dictonary) {
			System.out.println(entry.term + " " + entry.frequency + " " + entry.postingListPos);
		}

//		for (LinkedList<Long> entry : postingsLists) {
//			if (entry.size() > 1) {
//				entry.forEach(e -> {
//					System.out.print(e + " ");
//				});
//				System.out.println();
//			}
//		}

	}

	public String normalize(String text) {
		return text.replace("[NEWLINE]", " ").replace("[TAB]", " ").replace(".", " ").replace(",", " ")
				.replace(";", " ").replace("’", "'").replace("\"", " ").replace("”", " ").replace("!", " ")
				.replace(":", " ").replace("“", " ").toLowerCase().strip();
	}

	public void add(String term, long id) {
		int position = searchDictonary(term);
		if (position >= 0) {
			dictonary.get(position).frequency++;
			addToPosting(id, position);
		} else {
			dictonary.add(new DictEntry(term, 1, makePostingsList(id)));
		}
	}

	private void addToPosting(long id, int postingListPos) {
		postingsLists.get(postingListPos).add(-Collections.binarySearch(postingsLists.get(postingListPos), id) - 1, id);
	}

	private int makePostingsList(long id) {
		postingsLists.add(new LinkedList<Long>());
		postingsLists.getLast().add(id);
		// TODO Auto-generated method stub
		return postingsLists.size() - 1;
	}

	public class TempEntry implements Comparable<TempEntry> {
		public final String term;
		public final long id;

		public TempEntry(long id, String term) {
			this.term = term;
			this.id = id;

		}

		@Override
		public boolean equals(Object object) {
			if (!object.getClass().equals(TempEntry.class)) {
				return false;
			}
			return this.term.equals(((TempEntry) object).term) && (this.id == (((TempEntry) object).id));
		}

		@Override
		public int compareTo(TempEntry entry) {
			// TODO Auto-generated method stub
			int compareTerms = this.term.compareTo(entry.term);
			if (compareTerms == 0) {
				return Long.compare(this.id, entry.id);
			} else {
				return compareTerms;
			}
		}
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
		public boolean equals(Object object) {
			if (!object.getClass().equals(DictEntry.class)) {
				return false;
			}
			return this.term.equals(((DictEntry) object).term);
		}

		@Override
		public int compareTo(DictEntry entry) {
			return this.term.compareTo(entry.term);
		}

	}

	public int searchDictonary(String searchTerm) {
		return Collections.binarySearch(dictonary, new DictEntry(searchTerm, 0, 0));
	}

	public LinkedList<Long> query(String term) {
		// System.out.println(normalizedTerm);
		int position = searchDictonary(normalize(term));
		// System.out.println(position);
		if (position >= 0) {
			return postingsLists.get(position);
		}
		return new LinkedList<Long>();
	}

	public LinkedList<Long> query(String term1, String term2) {
		Iterator<Long> term1Iterator = query(term1).iterator();
		Iterator<Long> term2Iterator = query(term2).iterator();
		LinkedList<Long> intersection = new LinkedList<Long>();

		if (!term1Iterator.hasNext() || !term2Iterator.hasNext()) {
			return intersection;
		}

		long posting1 = term1Iterator.next();
		long posting2 = term2Iterator.next();

		while (true) {
			if (posting1 == posting2) {
				intersection.add(posting1);
			}
			if (posting1 <= posting2) {
				if (term1Iterator.hasNext()) {
					posting1 = term1Iterator.next();
				} else {
					break;
				}
			}
			if (posting1 >= posting2) {
				if (term2Iterator.hasNext()) {
					posting2 = term2Iterator.next();
				} else {
					break;
				}
			}
		}

		return intersection;
	}

}
