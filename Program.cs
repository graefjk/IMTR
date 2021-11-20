using System.Linq;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.IO;
using System.Collections.Generic;
using System;
using System.Threading.Tasks;

namespace Program
{
    class Program
    {
        static bool running = true;
        static Dictionary<string, PostingsData> index;
        static Dictionary<string, List<string>> bigramIndex;
        static List<List<int>> postingsIndex; //could be implemented to be on a drive, by Serializing and Deserializing it every time for example

        static Dictionary<int, Tuple<string, string>> docs;
        static void Main(string[] args)
        {

            string term1 = Console.ReadLine().Trim().ToLower().Replace(" ", "");
            string term2 = "covid";
            Console.WriteLine(term1);
            foreach (string s in term1.Split(" ").Concat(term2.Split(" ")).ToArray()) Console.WriteLine(s);


            //Basic Console interface loop
            while (running)
            {
                Console.Write("\nChoose Option with corresponding number: \n\t1: Read Twitter file.\n\t2: Query Data.\n\t3: Create Query File.\n\t4: Exit.\n");
                var input = Console.ReadLine();
                var option = 0;
                try
                {
                    option = int.Parse(input);
                }
                catch { }
                switch (option)
                {
                    case 1:
                        if (Console.ReadLine() == "y")
                        {
                            indexFile("../../../twitter.csv");
                        }
                        else
                        {
                            indexFile("twitter.csv"); //Read File into Memory
                            buildBiWordIndex();
                        }
                        break;
                    case 2: //single use of query with two terms
                        Console.WriteLine("Input 2 terms to search for:");
                        var in1 = CleanInput(Console.ReadLine().Trim().ToLower().Replace(" ", "")); //query term 1
                        var in2 = CleanInput(Console.ReadLine().Trim().ToLower().Replace(" ", "")); //query term 2
                        Console.WriteLine(query(in1, in2)); //write all the tweets that contained both terms
                        break;
                    case 3: //output different versions of the query into a file.
                        using (StreamWriter writer = new StreamWriter("output.txt"))
                        {
                            writer.Write(query("side effects Malaria", "COVID vaccine"));
                        }
                        break;
                    case 4:
                        running = false;
                        continue;
                    default: continue;
                    case 5:
                        wildCardQuery("brit*").ForEach(i => { Console.WriteLine(docs[i].Item2 + "\t" + docs[i].Item1.Replace("\n", " ") + "\n"); });
                        break;
                    case 6:
                        query("brit*").ForEach(i => { Console.WriteLine(docs[i].Item2 + "\t" + docs[i].Item1.Replace("\n", " ") + "\n"); });
                        continue;
                }
            }
        }

        static void indexFile(string path)
        {
            var sw = new Stopwatch();
            sw.Start();
            int newDocId = -1;
            postingsIndex = new List<List<int>>();
            index = new Dictionary<string, PostingsData>();
            docs = new Dictionary<int, Tuple<string, string>>();

            List<Exception> errs = new List<Exception>();
            using (StreamReader sr = new StreamReader(path))
            {
                do
                { //loop through all lines in File
                    try
                    {
                        // get the values in te first few "columns"
                        var line = sr.ReadLine().Trim();
                        if (line.Length == 0) continue;
                        newDocId++;
                        var splitLine = line.Split("\t");
                        if (splitLine.Length < 4) continue;
                        var id = splitLine[0];

                        //actual data is in the last field
                        var data = splitLine[3];
                        docs[newDocId] = new Tuple<string, string>(data, id); //save data and id in separate dict. to get later

                        List<string> typeList = new List<string>(); //List of types that are in the current Line

                        //go over raw Data and get out normalized types
                        foreach (string currentToken in data.Split(' '))
                        {
                            var currentType = CleanInput(currentToken.Replace("[NEWLINE]", "")).Trim().ToLower();
                            //check if type has already been found inside document
                            if (!typeList.Contains(currentType))
                            {
                                //if not, add the entry
                                typeList.Add(currentType);
                            }
                            //duplicates get ignored, because frequenzy isn't a required field
                        }
                        //add all types to the global dictionary
                        foreach (string typ in typeList)
                        {
                            //check if type is already present in dictionary
                            if (index.ContainsKey(typ))
                            {
                                //if so update entry
                                var postingEntry = index[typ];
                                postingsIndex[postingEntry.pointer].Add(newDocId); //use pointer in dictionary to get the postingsList
                                postingEntry.count++;
                                index[typ] = postingEntry; //update entry in index, only count needs to be updates, as pointer never changes

                            }
                            else
                            {
                                //if not add new entry
                                postingsIndex.Add(new List<int> { newDocId });
                                index.Add(typ, new PostingsData(1, postingsIndex.Count));
                            }
                        }
                        if (newDocId % 10000 == 0)// if you write everytime you loop Console.write slows down the programm
                            Console.Write("\rLine: " + newDocId + "\tSize: " + index.Count);
                    }
                    catch (Exception e)
                    {
                        errs.Add(e);
                        continue;
                    }
                } while (!sr.EndOfStream);

            }

            sw.Stop();
            Console.WriteLine("\nElapsed time: " + sw.Elapsed.ToString());
            Console.WriteLine("Errors occured: " + errs.Count);
        }

        public static List<int> query(string term)
        { //query single term
            if (term.Contains('*')) { Console.WriteLine("LOLOL"); return wildCardQuery(term.Trim().ToLower()); }
            term = CleanInput(term.Trim().ToLower());
            if (!index.ContainsKey(term)) return new List<int>();
            return postingsIndex[index[term].pointer]; //simple indexing in dictionary
        }

        private static List<int> wildCardQuery(string term)
        {
            //splitting the searchTerm in multible subterms arrounf the * 
            string[] subTerms = term.Split('*');
            subTerms[0] = " " + subTerms[0];
            subTerms[subTerms.Length - 1] = subTerms[subTerms.Length - 1] + " ";
            List<List<string>> termList = new List<List<string>>();

            foreach (string subTerm in subTerms)
            {
                for (int i = 0; i < subTerm.Length - 1; i++)
                {
                    termList.Add(bigramIndex[subTerm.Substring(i, 2)]);
                }
            }
            //merging all the termLists together
            List<string> intersectList = termList[0];
            intersectList.Sort();
            for (int i = 1; i < termList.Count; i++)
            {
                termList[i].Sort();
                intersectList = intersect2(intersectList, termList[i]);
            }
            subTerms[0] = subTerms[0].Trim();
            subTerms[subTerms.Length - 1] = subTerms[subTerms.Length - 1].Trim();

            //postfiltering
            List<string> tempList = intersectList;
            foreach (string result in tempList)
            {
                foreach (string subTerm in subTerms)
                {
                    if (!result.Contains(subTerm)) intersectList.Remove(result);
                }
            }

            //getting the postingIndexes of the result terms
            List<int> resultList = new List<int>();
            foreach (string result in intersectList) { 
                resultList.AddRange(postingsIndex[index[result].pointer]); 
            }

            resultList.Sort();
            return resultList;
        }

        public static List<int> query(string[] terms)
        { //returns the list of documents that contain terms
            List<int> ret = new List<int>();
            if (terms.Length == 0) return ret;
            ret = query(terms[0]);
            for (int i = 1; i < terms.Length; i++)
            {
                ret = intersect(ret, query(terms[i]));
            }
            return ret;
        }

        public static string query(string term1, string term2)
        { //query and of two terms
            //get postings of both terms
            var allTerms = term1.Split(" ").Concat(term2.Split(" ")).ToArray(); //put all terms into one array to use other query function
            var foundDocs = query(allTerms);
            string ret = "";
            foreach (int i in foundDocs)
            {
                ret += docs[i].Item2 + "\t" + docs[i].Item1.Replace("\n", " ") + "\n";
            }
            return ret;

        }

        private static List<int> intersect(List<int> list1, List<int> list2) //intersect two arrays, returning the commonalities
        {
            var returnList = new List<int>();
            if (list1.Count == 0 || list2.Count == 0) return returnList;

            // iterate over both lists according to lecture
            var iter1 = list1.GetEnumerator();
            var iter2 = list2.GetEnumerator();
            iter1.MoveNext();
            iter2.MoveNext();
            var cont = true;
            do
            {
                //if values match, a document with both has been found, add to result and continue
                if (iter1.Current == iter2.Current)
                {
                    returnList.Add(iter1.Current);
                    cont = iter1.MoveNext() && iter2.MoveNext();
                    continue;
                }
                //If they dont match, advance the lesser list
                var idx1 = iter1.Current;
                var idx2 = iter2.Current;
                if (idx1 < idx2)
                {
                    cont = iter1.MoveNext();
                }
                else if (idx1 > idx2)
                {
                    cont = iter2.MoveNext();
                }
                else
                {
                    throw new Exception("Comparison Failed.");
                }
            } while (cont);

            return returnList;
        }

        private static List<string> intersect2(List<string> list1, List<string> list2) //intersect two arrays, returning the commonalities
        {
            var returnList = new List<string>();
            if (list1.Count == 0 || list2.Count == 0) return returnList;

            // iterate over both lists according to lecture
            var iter1 = list1.GetEnumerator();
            var iter2 = list2.GetEnumerator();
            iter1.MoveNext();
            iter2.MoveNext();
            var cont = true;
            do
            {
                var idx1 = iter1.Current;
                var idx2 = iter2.Current;
                //if values match, a document with both has been found, add to result and continue
                switch (idx1.CompareTo(idx2))
                {
                    case 0:
                        returnList.Add(idx1);
                        cont = iter1.MoveNext() && iter2.MoveNext();
                        break;
                    case -1:
                        cont = iter1.MoveNext();
                        break;
                    case 1:
                        cont = iter2.MoveNext();
                        break;
                    default: throw new Exception("Comparison Failed.");
                }
            } while (cont);

            return returnList;
        }

        static string CleanInput(string strIn) //Gotten from mocrosoft docs: https://docs.microsoft.com/de-de/dotnet/standard/base-types/how-to-strip-invalid-characters-from-a-string
        {
            // Replace invalid characters with empty strings.
            try
            {
                return Regex.Replace(strIn, @"[^\w#@-]", "",
                                     RegexOptions.None, TimeSpan.FromSeconds(1.5));
            }
            // If we timeout when replacing invalid characters,
            // we should return Empty.
            catch (RegexMatchTimeoutException)
            {
                return String.Empty;
            }
        }

        struct PostingsData
        { //helper structure that contains values neccessary in dict
            public int count { get; set; }
            public int pointer { get; }
            public PostingsData(int count, int pointer)
            {
                this.count = count;
                this.pointer = pointer;
            }
        }


        static void buildBiWordIndex()
        {
            Console.WriteLine("index size: " + index.Count);
            bigramIndex = new Dictionary<string, List<string>>();
            int n = 0;
            foreach (KeyValuePair<string, PostingsData> entry in index)
            {
                if (n % 100000 == 0)
                    Console.Write("\rindex: " + n);
                n++;
                string term = entry.Key;
                string paddedTerm = " " + term + " "; // using a whitespace as a special character because we know that no term contains a whitespace
                for (int i = 0; i < paddedTerm.Length - 1; i++)
                {
                    string bigram = paddedTerm.Substring(i, 2);
                    if (bigramIndex.ContainsKey(bigram))
                    {
                        bigramIndex[bigram].Add(term);
                    }
                    else
                    {
                        bigramIndex.Add(bigram, new List<string> { term });
                    }
                }
            }
            Console.Write("\rindex: " + n);
        }

    }
}
