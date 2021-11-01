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
		System.out.println(searcher.query("side effects Malaria", "COVID vaccines")); // no results
		System.out.println(searcher.query("side effect Malaria", "COVID vaccine")); // no results
		System.out.println(searcher.query("side effects Malaria", "COVID vaccine")); // results: [1395486226216628225,
																						// 1414233129129238532,
																						// 1419334582206410759]

		// 1395486226216628225 @lisaebasa Lisa ✍ Content Writer These Covid vaccine side
		// effects are no joke. I'm here feeling like I have serious malaria
		// 1414233129129238532 @10DY3 Rey Covid vaccine side effects are not cute. Ni
		// mvela malaria malaria 🥴
		// 1419334582206410759 @adamselzer Adam Selzer | אדם “Eric Clapton said he had
		// side effects from the vaccine; I’ll just take my chances with Covid.” - some
		// guy on my facebook[NEWLINE][NEWLINE]“That’s like to say if you got a cold,
		// take a shot of malaria.” - Bob Dylan, 1962

		System.out.println(searcher.query("side effect Malaria", "COVID vaccines")); // no results

		// "show me tweets of people who talk about the side effects of malaria and
		// COVID vaccines" as in "side effects of malaria" and/or "side effects of COVID
		// vaccines"
		System.out.println(searcher.query("side effects", "Malaria")); // [1367971503573266434, 1369013217893232641,
																		// 1369714948578938880, 1369721990261714947,
																		// 1378019116808871940, 1379866604758986756,
																		// 1379881379522080773, 1386096414888022022,
																		// 1389623222509965319, 1390558543892340736,
																		// 1395486226216628225, 1397279306720718849,
																		// 1397279672623419396, 1399832966541496322,
																		// 1400817309644734464, 1401564123960250369,
																		// 1402005689589960708, 1412836299254415364,
																		// 1414233129129238532, 1419334582206410759,
																		// 1422297273573285890, 1425137761103851524,
																		// 1425228681803304960, 1429372699969720320,
																		// 1431327734660308996, 1432091029252059138,
																		// 1437533029409464323, 1440771791778029568]

		System.out.println(searcher.query("side effect", "Malaria")); // [1348965245646204928, 1353800682965446656, 1374093998139305985, 1375210663493128205, 1379881379522080773, 1408115547066224650, 1423777060841996291]
		System.out.println(searcher.query("side effects", "COVID vaccines")); //[1345519179525464065, 1356390570105610241, 1359009146398408705, 1362456146389385221, 1364724700593721351, 1371534745809547264, 1372772796346998784, 1373720332146778113, 1375446942256271367, 1377703250946760707, 1379903590941134850, 1382514072223031306, 1383026939603091456, 1386468881082970112, 1388279455693643783, 1390811852729290752, 1391542129957249028, 1396138553512501258, 1397506903064956929, 1401890664380461056, 1403864293322506240, 1405551171670994947, 1405613950700457989, 1407848810802462723, 1408210345420738563, 1408500961665064976, 1409659378110660608, 1411471789482418177, 1412922374006136833, 1414733987017285636, 1416137465740644355, 1416182247892393987, 1417845198512607234, 1417972041089757192, 1418402997521031172, 1419133218675216389, 1422965727925919749, 1423082761825443841, 1423423589261340672, 1423432518003396615, 1425826157832728587, 1427186264349024257, 1427418107866345475, 1428143461144928256, 1436489048185573403, 1438250839332823043, 1440374772555149323, 1442263171440332800, 1443001148739362819]
		System.out.println(searcher.query("side effect", "COVID vaccines")); //
		System.out.println(searcher.query("side effects", "COVID vaccines")); //
		System.out.println(searcher.query("side effect", "COVID vaccines")); //

		// "show me tweets of people who talk about the side effects of malaria and
		// COVID vaccines" as in "side effects of malaria vaccines" and/or "side effects
		// of COVID vaccines"
		System.out.println(searcher.query("side effects", "Malaria vaccines")); //
		System.out.println(searcher.query("side effect", "Malaria vaccines")); //
		System.out.println(searcher.query("side effects", "COVID vaccine")); //
		System.out.println(searcher.query("side effect", "COVID vaccine")); //

		System.out.println(searcher.query("Malaria", "COVID").subList(0, 10));
		System.out.println(searcher.query("Malaria").subList(0, 10));
		System.out.println(searcher.query("COVID").subList(0, 10));
	}

	HashMap<String, DictEntry> dictonary = new HashMap<String, DictEntry>();
	ArrayList<ArrayList<Long>> postingsLists = new ArrayList<ArrayList<Long>>();

	public void index(File file) throws FileNotFoundException {
		int n = 0;

		Scanner scanner = new Scanner(file);
		long time = System.nanoTime();

		// if you want to search only the first n terms
		while (scanner.hasNext() && (n < 30000000)) {
			n++;
			if (n % 10000 == 0) {
				System.out.println(n);
			}
			String line = scanner.nextLine();
			String[] columns = line.split("	"); // special whitespace
			// this check is here because of line 2114543, 4115522, 4357319, 4577422,
			// 5520503, 6437018, 6437019, 6512362, 7185729 in the input file which are
			// incorrectly
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

	public void add(String token, long id) {

		if (!dictonary.containsKey(token)) { // 0.0563824 whole loop
			dictonary.put(token, new DictEntry(1, makePostingsList(id)));
			return;
		}

		int postingListPos = dictonary.get(token).postingListPos; // 0.0667442
		ArrayList<Long> postings = postingsLists.get(postingListPos); // 0.1953765
		// !postings.contains(id)
		// 8.453592 whole loop with !postings.contains(id), 1.2163975 with
		// (Collections.binarySearch(postings, id) < 0)

		int postingPosition = Collections.binarySearch(postings, id);
		if (postingPosition < 0) { // 8.453592 whole loop with
			dictonary.get(token).frequency++; // 0.0759479
			postings.add(-postingPosition - 1, id);
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
