package bingsearchpp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
//Download and add this library to the build path.

public class BingTest {
		
		private static boolean debug = false;
		private static List<String> queryList = new ArrayList<String>();
		private static String[] titleResult;
		private static String[] descriptionResult;
		private static String[] urlResult;
		private static int[] isRelevant;
		private static String accountKey = "";
		private static double targetPrecision;
		private static double currentPrecision;
		private static List<Doc> docs;
		private static List<Doc> relevantDocs;
		private static List<Doc> irrelevantDocs;
		private static List<Term> termList;
		private static double averageTermsInDocument;
		private static List<Term> newQueryWords;
		private static List<Double> tfidfsOfNewWords;
		private static List<String> ignoreList;
		
		//data fields required by Bing:
		private static StringBuffer bingURL;
		private static byte[] accountKeyBytes;
		private static String accountKeyEnc;
		private static URL url;
		private static URLConnection urlConnection;
		private static InputStream inputStream;
		private static String content;
		private static byte[] contentRaw;
		private static Document resultDoc;
		
		public static void main(String[] args) throws IOException {
		
		//Data Fields
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		/***************************************************************************************
		 						SOME GENERAL INFORMATION
							
			(i)	gAQWhdOorE4LJ5ntPJT9mbKSemDzsrDxsN/xcZQA8Fo= --> Account Key
											
		 ***************************************************************************************/

		//Assign the command line input arguments;
		System.out.println("Enter Bing User Key...");
		args = new String[3];
																//ERASED THE SCANNER INITIALIZATION HERE
		args[0] = in.readLine();								//INSTEAD OF THE PREVIOUS SCANNER INPUT, USED BUFFEREFREADER
		System.out.println("Enter Desired Target Precision...");
		args[1] = in.readLine();								//INSTEAD OF THE PREVIOUS SCANNER INPUT, USED BUFFEREFREADER
		System.out.println("Enter the search query...");
		args[2] = in.readLine();								//INSTEAD OF THE PREVIOUS SCANNER INPUT, USED BUFFEREFREADER
		
		if(args.length > 0)
			accountKey = args[0];
		if(args.length > 1)
			targetPrecision = Double.parseDouble(args[1]);
		for(int i = 2; i < args.length; i ++)
			queryList.add(args[i].toLowerCase());
		
/*******************************************************************************************************************************/
		
		do
		{
			//Initialize and Re-initialize
			titleResult = new String[10];
			descriptionResult = new String[10];
			urlResult = new String[10];
			isRelevant = new int[10];
			docs = new ArrayList<Doc>();
			termList = new ArrayList<Term>();
			ignoreList = new ArrayList<String>();
			newQueryWords = new ArrayList<Term>();
			tfidfsOfNewWords = new ArrayList<Double>();						
			String[] ignoreWords = new String[] {"able","about","across","after","all","almost","also","among","even","better","and","any","are","because","been","so","few","but","can","cannot","further","make","makes","many","ahead","could","dear","did","does","either","else","ever","every","for","from","get","got","had","has","have","her","hers","him","his","how","however","into","its","just","least","let","like","likely","may","might","most","must","neither","nor","not","off","often","only","our","own","other","say","says","she","the","rather","said","says","should","since","some","than","that","their","them","then","there","was","who","yet","you","why","these","they","this","twas","wants","were","what","when","where","which","while","whom","will","with","would","your","even"};
			for(int i = 0; i < ignoreWords.length; i ++)
			{
				ignoreList.add(ignoreWords[i]);
			}
			
			
			//formulate the bingURL according to requirement
			bingURL = new StringBuffer("https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web?Query=");
		
			//%27gates%27
			int i;
			bingURL.append("'");
			for ( i = 0; i < queryList.size(); i ++)
			{	
				if(i != 0)
					bingURL.append("+");
				if (!queryList.get(i).equals(""))
				{
					bingURL.append(queryList.get(i));
				}
			}
			bingURL.append("'");
		
			//&$top=10&$format=Atom";
			bingURL.append("&$top=10&$format=Atom");
		
			//The URL for Bing is now created
			
			//BingTest.print(bingURL);
		
/*******************************************************************************************************************************/
		
			//Actual connection between BingSearch++ and Bing
			accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
			accountKeyEnc = new String(accountKeyBytes);

			//Query dispatched to Bing
			url = new URL(bingURL+"");
			urlConnection = url.openConnection();
			urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
		
			//Bing sends 10 results to BingSearch++
			inputStream = (InputStream) urlConnection.getContent();		
			contentRaw = new byte[urlConnection.getContentLength()];
			inputStream.read(contentRaw);
			content = new String(contentRaw);
			
/*******************************************************************************************************************************/
			
			resultDoc = Jsoup.parse(content,"",Parser.xmlParser());
			i = 0;
		
			//System.err.println(resultDoc.toString());
			
			
			BingTest.print("\n##############Search Results##############\n");
		
			// Extract the Title, Description and URL of the 10 searched items
			// from the XML result content, display search results, ask user for relevance
			for ( Element entryElements : resultDoc.select("entry"))
			{	
				Elements e = entryElements.getElementsByTag("content");
				titleResult[i]=new String();
				titleResult[i]=(e.first().children().first().children().get(1).text());
				descriptionResult[i]=new String();
				descriptionResult[i]=(e.first().children().first().children().get(2).text());
				urlResult[i]=new String();
				urlResult[i]=(e.first().children().first().children().get(3).text());
			
				BingTest.print("Title #"+(i+1)+": "+titleResult[i]);
				BingTest.print("Description #"+(i+1)+": "+descriptionResult[i]);
				BingTest.print("URL #"+(i+1)+": "+urlResult[i]+"\n");
				BingTest.print("\nResult relevant ? (Y for Yes, other for No)");
			
				String input ="";
			
				if((input += in.readLine()).equals("y") || input.equals("Y"))
					isRelevant[i] = 1;
				else
					isRelevant[i] = 0;
				i ++;
			}

/*******************************************************************************************************************************/		

			//Check if there are 10 results returned by Bing
			if (titleResult[9] == null)
			{	BingTest.print("Not enough results...program terminated!");
				System.exit(0);
			}

			int count = 0;// Keeps a count of number of relevant documents in the current iteration.
			for (i = 0; i < 10; i ++)
			{
				if ( isRelevant[i] == 1 )
					count++;
			}
			currentPrecision = (double)count / isRelevant.length;
			BingTest.print("Current Precision@10 : "+currentPrecision);
			if (currentPrecision == 0.0 )
			{
				BingTest.print("Precision zero... program terminated!");
				System.exit(0);
			}

			relevantDocs = new ArrayList<Doc>();	//Assign the user-relevant docs.
			irrelevantDocs = new ArrayList<Doc>();	//Assign the user-irrelevant docs.
			
			//Get the Docs... all, relevant and irrelevant, and add all terms to the termList
			//Add all the terms present in a doc to the termList of the doc
			docs = new ArrayList<Doc>();
			for (i = 0; i < 10; i ++)
			{
				Doc newDoc = null;
				if (isRelevant[i] == 1)
				{	
					newDoc = new Doc(titleResult[i] + " " + descriptionResult[i],true);
					relevantDocs.add(newDoc);
				}
				else
				{	
					newDoc = new Doc(titleResult[i] + " " + descriptionResult[i],false);
					irrelevantDocs.add(newDoc);
				}
				docs.add(newDoc);
				//BingTest.log(docs.get(i).getText());
				addToTermList(newDoc);
				//BingTest.log(docs.get(i).isRelevant());
			}
			//Check for document termlists---->> Doc Termlists alright!
			
			//Get Average Doc Length for calculating tfidf
			int sum = 0;
			//System.err.println("#####################################################################"+docs.size()+"-----"+docs.get(docs.size()-1).getId());
			for(i = 0; i < 10; i ++)
			{
				sum += docs.get(i).getDocLength();
			}
			averageTermsInDocument = sum / 10;
			
			//SET ALL OCCURENCES!!
			for(i = 0; i < 10; i ++)
			{
				for(int j = 0; j < termList.size(); j ++)
				{
					for(int k = 0; k < docs.get(i).getTermList().size(); k ++)
					{
						if(docs.get(i).getTermList().get(k).getTerm().equals(termList.get(j).getTerm()))
						{
							//System.err.println("%%%%%%%%%%%%%%%%%%%%%%%"+termList.get(j).getOccurences(i));
							termList.get(j).incrementOccurences(docs.get(i).getId());
						}						
					}
				}
			}
			
			//test occurences FIXED
			/*for(i = 0; i < termList.size(); i ++)
			{
				for(int j = 0; j < 10; j ++)
				{
					BingTest.log("term: "+termList.get(i).getTerm()+" is present "+termList.get(i).getOccurences(j)+" times in "+j);
				}
			}*/
			
			//test termlist
			//for(i = 0; i < termList.size(); i ++)
				//BingTest.log("-----------------"+termList.get(i).getTerm());
			
			//TESTING IF EACH DOC HAS TERMLIST POPULATED
			/*for(i = 0; i < docs.size(); i ++)
			{
				//for(int j = 0; j < docs.get(i).getTermList().size(); j ++)
					//BingTest.log("Document "+i+"-->"+docs.get(i).getTermList().get(j).getTerm());
			}*/
			
			//Find Doc Freq of each term
			for(i = 0; i < termList.size(); i ++)
			{	
				calcDocFreq(termList.get(i));
				//BingTest.log(termList.get(i).getTerm()+" "+termList.get(i).getDocsWithTerm());
			}
			
			//Set TFIDF for each term for each doc
			for(i = 0; i < 10; i ++)
			{
				for (int j = 0; j < termList.size(); j ++)
				{
					termList.get(j).setTfidf(docs.get(i), averageTermsInDocument);
				}
			}
			
			
			//re-initialize the termlists of each doc to update their TFIDFs
			//BingTest.log(termList.get(0).getTfidf(docs.get(0))+" "+termList.get(2).getTfidf(docs.get(0))+" "+termList.get(1).getTfidf(docs.get(0))+" ");
			for(i = 0; i < 10; i ++)
			{
				List<Term> newTermList = new ArrayList<Term>();
				for(int j = 0; j < docs.get(i).getTermList().size(); j ++)
				{
					
					for(int k = 0; k < termList.size(); k ++)
					{
						if(termList.get(k).getTerm().equals(docs.get(i).getTermList().get(j).getTerm()))
						{
							
							newTermList.add(termList.get(k));
						}
					}
				}
				docs.get(i).setTermList(newTermList);
			}
			
			
			//BingTest.log(docs.get(0).getTermList().get(0).getTfidf(docs.get(0))+" "+docs.get(0).getTermList().get(1).getTfidf(docs.get(0))+" "+docs.get(0).getTermList().get(2).getTfidf(docs.get(0))+" "+docs.get(0).getTermList().get(3).getTfidf(docs.get(0)));
			
			//testing
			/*int j=0;
			for(i = 0; i < 10; i ++)//Test TFIDF... FIX TFIDFs////////I THINK ITS FIXED:)
			{
				for (j = 0; j < termList.size(); j ++)
				{
					BingTest.log(termList.get(j).getTerm()+" in doc "+i+"-->"+termList.get(j).getTfidf(docs.get(i)));
				}	
			}*/
			//BingTest.log("SIZE:"+termList.size() +"TOTAL OUTPUT:" +j*i);
			
			
			//TESTING IF RELEVANT DOC/IRRELEVANT DOC TERMLISTS HAVE TFIDF VALUES INITIALIZED OR NOT
			
			
			
			//BingTest.log(docs.get(0).getTermList().get(0).getTfidf(docs.get(0)));
			
			//Make vectors of the docs
			/*for(i = 0; i < 10; i ++)
			{
				//docs.get(i).makeVector();
			}*/
			
			/*for(i = 0; i < 10; i ++)//test vectors
			{
				BingTest.log("Doc "+i+"-->");
				for(j = 0; j < docs.get(i).getVector().length; j ++)
					BingTest.log(docs.get(i).getVector());
				BingTest.log(docs.get(i).getTermList().get(0));//.getTfidf(docs.get(i)));
			}*/
			
			//List<Term> newQueryList = new ArrayList<Term>();
/********************************************************************************************************************************/			
			//OUR KNOWLEDGE BASED STRATEGY
			/*for(i = 0; i < 10; i ++)
			{	//List<Term> relevantList = null;
				if(docs.get(i).isRelevant())
				{
					addNewWords(docs.get(i));///////////////////////////////////////////////////////////////////////////
				}
				//for(j = 0; relevantList != null && j < relevantList.size(); j ++)
				//{
				//	newQueryList.add(relevantList.get(j));
				//}
			}
			

			//for(i = 0; i < newQueryList.size(); i ++)
				//BingTest.log(newQueryList.get(i).getTerm() + "<<-------- " + tfidfsOfNewWords.get(i));
			
			
			sortNewWordList(newQueryWords, tfidfsOfNewWords);
			
			BingTest.log(newQueryWords.size() + " " + tfidfsOfNewWords.size());
			
			//Reomove Infinity valued newWords
			for(i = 0; i < newQueryWords.size(); i ++)
			{	
				if(Double.isInfinite(tfidfsOfNewWords.get(i)))
				{
					BingTest.log(newQueryWords.get(i).getTerm());
					newQueryWords.remove(i);
					tfidfsOfNewWords.remove(i);
					BingTest.log(newQueryWords.size() + " " + tfidfsOfNewWords.size()+ "--");
				}
			}
			
			//Testing Infinity case
			BingTest.log(newQueryWords.size() + " " + tfidfsOfNewWords.size());
			for(i = 0; i < newQueryWords.size(); i ++)
			{
				BingTest.log(newQueryWords.get(i).getTerm() + "<<-------- " + tfidfsOfNewWords.get(i));
			}
			
			//Add first two terms in new query
			int countNewTerms = 0;
			for(i = 0; i < newQueryWords.size() && countNewTerms<2; i ++)
			{
				if(tfidfsOfNewWords.get(i).isInfinite())
				{
					tfidfsOfNewWords.remove(i);
					newQueryWords.remove(i);
					--i;
					continue;
				}
				queryList.add(newQueryWords.get(i).getTerm());
				BingTest.log("Added new Term====>>>>"+ newQueryWords.get(i).getTerm());
				countNewTerms ++;
			}
			*/
/********************************************************************************************************************************/			

			
			
/*******************************************************************************************************************************/
			//Run the IDE-Dec-Hi Algo!! We get the vector of new query 
			
			
			
			double[] modifiedQuery = new double[termList.size()];
			
			//Adding the orig Query tfidfs
			for(i = 0; i < modifiedQuery.length; i ++)
			{
				double x = 0;
				
				for(int j = 0; j < 10; j ++)
				{
					for(int k = 0; k < queryList.size(); k ++)
					{
						if(termList.get(i).getTerm().equalsIgnoreCase(queryList.get(k)))
						{
							x += termList.get(i).getTfidf(docs.get(j));
						}
					}
				}
				modifiedQuery[i] = x;
			}
			
			BingTest.log("Orig:" + Arrays.toString(modifiedQuery));
			
			
			for(i = 0; i < modifiedQuery.length; i ++)
			{
				double x = 0;
				for(int j = 0; j < 10; j ++)
				{
					if(docs.get(j).isRelevant())
					{
						for(int k = 0; k < docs.get(j).getTermList().size(); k ++)
						{
							if(termList.get(i).getTerm().equalsIgnoreCase(docs.get(j).getTermList().get(k).getTerm()))
							{
								x += termList.get(i).getTfidf(docs.get(j));
							}
						}
					}
				}
				modifiedQuery[i] += x;
			}
			
			BingTest.log("Orig + Relevant" + Arrays.toString(modifiedQuery));
			
			for(i = 0; i < modifiedQuery.length; i ++)
			{
				for(int j = 0; j < 10; j ++)
				{
					if(!docs.get(j).isRelevant())
					{
						for(int k = 0; k < docs.get(j).getTermList().size(); k ++)
						{
							if(termList.get(i).getTerm().equalsIgnoreCase(docs.get(j).getTermList().get(k).getTerm()))
							{
								modifiedQuery[i] -= termList.get(i).getTfidf(docs.get(j));
							}
						}
						break;
					}
				}
			}
			
			BingTest.log("Orig + Relevant - 1st Irrelevant" + Arrays.toString(modifiedQuery));
			
			List<Double> finalTfidf = new ArrayList<Double>();
			for(i = 0; i < modifiedQuery.length; i ++)
			{
				finalTfidf.add(modifiedQuery[i]);
			}
			
//SORTING
			for (i = 1; i < termList.size(); i++)
			{
				int j = i;
				Term B = termList.get(i);
				Double TD = finalTfidf.get(i);
				Double D = modifiedQuery[i];
				while ((j > 0) && (modifiedQuery[j-1] < D))
				{
					termList.set(j, termList.get(j-1));
					finalTfidf.set(j, finalTfidf.get(j-1));
					modifiedQuery[j] = modifiedQuery[j-1];
					j--;
				}
				termList.set(j, B);
				finalTfidf.set(j, TD);
				modifiedQuery[j] = D;
			}
			
			
			
			
			BingTest.log("SORTED ==> Orig + Relevant - 1st Irrelevant" + Arrays.toString(modifiedQuery));
			for(i = 0; i < 5; i ++)
			{
				BingTest.log("SORTED ==> Orig + Relevant - 1st Irrelevant" + termList.get(i).getTerm());
			}
			//Add first two terms in new query
			int countNewTerms = 0;
			for(i = 0; i < termList.size() && countNewTerms<2; i ++)
			{
				
				if(finalTfidf.get(i).isInfinite())
				{
					finalTfidf.remove(i);
					termList.remove(i);
					--i;
					continue;
				}
			
				if(queryList.contains(termList.get(i).getTerm()))
				{
					finalTfidf.remove(i);
					termList.remove(i);
					--i;
					continue;
				}
				
				if(ignoreList.contains(termList.get(i).getTerm()))
				{
					finalTfidf.remove(i);
					termList.remove(i);
					--i;
					continue;
				}
				
				/*if(termList.get(i).getTerm().length() <= 3)
				{
					finalTfidf.remove(i);
					termList.remove(i);
					--i;
					continue;
				}*/
				queryList.add(termList.get(i).getTerm());
				BingTest.print("Added new Term====>>>>"+ termList.get(i).getTerm());
				countNewTerms ++;
			}
			
			
			
			
			
			BingTest.log("Orig + Relevant - 1st Irrelevant" + Arrays.toString(modifiedQuery));
			
			//AAGE KA DEKHTE HAI...
			
			//RESET THE COUNTER FOR DOCS!!!!!!!
			//docs.get(0).resetCount();
			Doc.resetCount();
			BingTest.log(currentPrecision);

		}while(currentPrecision<targetPrecision);
		
		BingTest.print("Target Precision achieved.");
	}
		
		private static void addNewWords(Doc doc)
		{
			//Add new Terms which have the highest TFIDF values from this doc
			List<Term> sortedList = doc.getSortedList();
			double bestValue = sortedList.get(0).getTfidf(doc);
			for(int j = 0; j < sortedList.size(); j ++)
			{
				if(sortedList.get(j).getTfidf(doc) == bestValue)
				{
					if(!newQueryWords.contains(sortedList.get(j)))
					{	newQueryWords.add(sortedList.get(j));
						tfidfsOfNewWords.add(bestValue);
						//BingTest.log(sortedList.get(j).getTerm()+" *added* with tf-idf-->" + tfidfsOfNewWords.get(j));
					}
				}
			}
			//Remove all terms from new words list which have high tfidf values in IRRELEVANT docs.==>>DONE ALREDY
			//sort the resultant list by tfidf
			//newQueryWords = sortNewWordList(newQueryWords, tfidfsOfNewWords);
		}
		
		private static List<Term> sortNewWordList(List<Term> list, List<Double> tfidfList)
		{
			for (int i = 1; i < list.size(); i++)
			{
				int j = i;
				Term B = list.get(i);
				Double D = tfidfList.get(i);
				while ((j > 0) && (tfidfList.get(j-1) < D))
				{
					list.set(j, list.get(j-1));
					tfidfList.set(j, tfidfList.get(j-1));
					j--;
				}
				list.set(j, B);
				tfidfList.set(j, D);
			}
			return list;
		}

		private static void log(Object errCheck) {
			if(debug)
				System.out.println(errCheck.toString());
		}
		
		private static void print(Object o)
		{
			try{
				  // Create file 
				  FileWriter fstream = new FileWriter("out.txt");
				  BufferedWriter out = new BufferedWriter(fstream);
				  out.write(o.toString());
				  //Close the output stream
				  out.close();
				}	catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
				  }
			System.out.println(o);
		}
		
		private static void addToTermList(Doc doc)
		{
			String[] words = doc.getText().split(" ");
			String newWord = "";
			List<Term> docTermList = new ArrayList<Term>();
			boolean flag = true;
			for ( int i = 0; i < words.length; i ++)
			{
				flag = true;
				StringBuffer sb = new StringBuffer("");
				for (int j = 0; j < words[i].length(); j ++)
				{
					if( Character.isLetterOrDigit(words[i].charAt(j)) )
					{
						sb.append(words[i].charAt(j));
					}
				}
				
				newWord = sb.toString();
				
				for (int k = 0; k < termList.size(); k ++)
				{					
					if ( termList.get(k).getTerm().equalsIgnoreCase(newWord))
					{
						//termList.get(k).incrementOccurences(doc.getId()); NOT HERE!!!!!!!!!!!
							//BingTest.log("Repeating"+ termList.get(k).getTerm() +" "+ termList.get(k).getOccurences(doc));
						flag = false;
						docTermList.add(new Term(newWord));
					}
				}
				if (flag)
				{
					termList.add(new Term(newWord.toLowerCase()));
					docTermList.add(new Term(newWord));
					//BingTest.log(newWord);
				}
			}
			doc.setTermList(docTermList);
		}
		
		private static void calcDocFreq(Term term)
		{
			for ( int i = 0; i < docs.size(); i ++)
			{
				if(docs.get(i).getText().contains(term.getTerm()))
				{
					term.incrementDocsWithTerm();
					term.setPresentInDoc(i);
				}
			}
		}
}

//TODO: Test is the termList needs to be re initialized after evry search query.
//TODO: more weight for initial results
//TODO: just 1st irrelevant taken... try all irrelevant for efficiency