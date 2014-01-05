package uhd.coref;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class Rules {

	static List<ConceptPairs> pairs;
	static Concepts concepts;
	static Doc doc;
	static People people;
	static Problems problems;
	static Test test;
	
	static ConceptPairs add = new ConceptPairs();
	
	static Lists list;
	
	
	
	static ConceptPairs [] getPairs(Doc txt, Concepts cons, Lists wList) {
		concepts = cons;
		doc = txt;
		pairs = new ArrayList<ConceptPairs>(50);
		
		if(wList == null)
		    list = new Lists();
		else
			list = wList;
		
		people = new People(list, concepts, doc);
		problems = new Problems(list, concepts, doc);
		test = new Test(list, concepts, doc);
		
		//umlsMatch();
		
//		list.findDoctors(concepts);
		
		for(int i = 0; i < concepts.length(); i++) {
			if(!list.isAdj(removeNonChar(concepts.mention(i)))||(concepts.type(i).contentEquals("people")||concepts.type(i).contentEquals("person")))
				startRules(i);
		}
		
		if(pairs.size() > 0) {
			makeChains(); 
			cleanPairs();
		}
		
		ConceptPairs [] back = new ConceptPairs[pairs.size()];
		for(int c = 0; c < pairs.size(); c++) {
			back[c] = pairs.get(c);
		}
		return back;
	}

	
	// ------------  Odie
	// diseaseorsyndrome
	// people
	// procedure
	// none
	// anatomicalsite
	// signorsymptom
	// other
	
	// ------------  I2B2
	// Problem
	// Person
	// Treatment
	// Test
	// Pronoun
	
	static void startRules(int i) {
		
	boolean found = false;

	if(concepts.type(i).contentEquals("person") || concepts.type(i).contentEquals("pronoun") || concepts.type(i).contentEquals("people") || concepts.type(i).contentEquals("none")) {
			add = people.check(i);
			
			if(add.dest != -1) {
				found = true;
				pairs.add(add);
			}
			
			add = people.check2(i);
			
			if(add.dest != -1){
				found = true;
				pairs.add(add);
			}
		} 
	
		found = runProblems("problem", i, found);
				
		found = runType("treatment", i, found);	
		
		found = runType("diseaseorsyndrome", i, found);
			
		found = runType("procedure", i, found);
		
		found = runType("anatomicalsite", i, found);
		
		found = runType("signorsymptom", i, found);
		
		found = runType("other", i, found);
		

		if(concepts.type(i).contentEquals("test") || concepts.type(i).contentEquals("pronoun") ) {
			add = test.check(i);
			
			if(add.dest != -1) {
				if (concepts.type(add.dest).contentEquals("test") || concepts.type(add.dest).contentEquals("pronoun")) {
					pairs.add(add);
					found = true;
				}
			} 	
		
			add = test.check2(i);
			if(add.dest != -1) {
				if (concepts.type(add.dest).contentEquals("test") ) {
					pairs.add(add);
					found = true;
				}
			}
			
			add = test.check3(i);
			if(add.dest != -1) {
				if (concepts.type(add.dest).contentEquals("test") ) {
					pairs.add(add);
					found = true;
				}
			}
		}
		
	//	if(!found) {
	//		if(!(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))) {
	//			gSearch(People.trimCommonWords(concepts.mention(i)));
	//		}
	//	}
	}  
	
	static boolean runProblems(String type, int i, boolean found) {
		
		if(concepts.type(i).contentEquals(type)||concepts.type(i).contentEquals("pronoun")||concepts.type(i).contentEquals("none")) {
			add = problems.check(i, type);
	
		//	String[] tense = null;
			String buf = doc.line(concepts.endLine(i));
				
			if(!list.find(list.futureTense, buf)) {
			
				if(add.dest != -1) {
			
				buf = doc.line(concepts.endLine(add.dest));
					if(!list.find(list.futureTense, buf))
						if(concepts.type(add.dest).contentEquals(type)) {
							pairs.add(add);
							found = true;
						}
				}
		
				add = problems.check1(i, type);
		
				if(add.dest != -1){
					
					buf = doc.line(concepts.endLine(add.dest));
						if(!list.find(list.futureTense, buf))
							if(concepts.type(add.dest).contentEquals(type)) {
								pairs.add(add);
								found = true;
							}
				}
			}
		} 
		
		return found;
	}
	
	
	static String removeNonChar(String phrase) {
		String back = phrase.trim();
		
		int length = phrase.length();
		
		for(int c = 0; c < back.length(); c++) {
			if((back.charAt(c) < 'a' || back.charAt(c) > 'z')&&back.charAt(c) != '-') {
				back = back.replace("" + back.charAt(c), "");
			}
		}
		
		back = back.replace("  ", " ");
		
		return back;
	}
	
	static boolean runType(String type, int i, boolean found) {
		
		if(concepts.type(i).contentEquals(type)||concepts.type(i).contentEquals("pronoun")||concepts.type(i).contentEquals("none")) {
			add = problems.check(i, type);
			
			if(add.dest != -1) {
				if(concepts.type(add.dest).contentEquals(type)) {
					pairs.add(add);
					found = true;
				}
			}
		
			add = problems.check1(i, type);
		
			if(add.dest != -1){
				if(concepts.type(add.dest).contentEquals(type)) {
					pairs.add(add);
					found = true;
				}
			}
		} 
		
		return found;
	}
	
	static void umlsMatch() {
		List<ArrayList<String>> conso = new ArrayList<ArrayList<String>>();
		int c = 0, d = 0, e = 0, f = 0;
		
		for(c = 0; c < concepts.length(); c++) {
			conso.add(Database2.getConcept(concepts.mention(c)));
		}
		
		ConceptPairs back = new ConceptPairs();

		back.chain = 0;
		back.source = 0;
		back.dest = 0;
		back.type = "none";
		back.rule = 99;
		
		
		for(c = 0; c < conso.size(); c++) 
			for(d = 0; d < conso.size(); d++) {
				if(c != d) {
					for(e = 0; e < conso.get(c).size(); e++)
						for(f = 0; f < conso.get(d).size(); f++) {
							if(conso.get(c).get(e).contentEquals(conso.get(d).get(f))) {
								back.source = c;
								back.source = d;
								pairs.add(back);
								break;
							}
						}
				}
			}
	}
	
	
	static int heading(int con) {
		int d = 0;
		String test = "";
		
			for(d = concepts.endLine(con); d > 0; d--) {
				test = doc.line(d);
				if(list.find(list.past_headings,test))
					return 1;
				if(list.find(list.present_headings,test))
					return 2;
				if(list.find(list.future_headings,test))
					return 3;
				if(test.endsWith(" :"))
					return 0;
			}
		
		return 0;
	}
	
	
	//======================================================================================== Rules for coreference
	/*
	static void rule5(int i) {
		
		if(!(concepts.type(i).contentEquals("problem")||concepts.type(i).contentEquals("test")||concepts.type(i).contentEquals("treatment")))
			return;
			
		
		int rule = 5;
		String gSearch = "";
		int a = 0, b = 0;
		
		String[] tense = null;
		String buf1 = doc.line(concepts.endLine(i));
		String[] buf;
		
		if(list.find(list.futureTense, buf1))
			tense = list.futureTense;
		if(list.find(list.pastTense, buf1))
			tense = list.pastTense;
		if(list.find(list.presentTense, buf1))
			tense = list.presentTense;
		
		

				
		buf = concepts.mention(i).split(" ");
		for(a = 0; a < buf.length; a++) 
		if(!list.common(buf[a]))
			gSearch += buf[a] + " ";
				
		if(gSearch.length() > 0) {
			gSearch = gSearch.substring(0, gSearch.length()-1);
				
			query search = gSearch(gSearch);
				
			boolean found = false;
			boolean similar = false;
			List<String> words = new ArrayList<String>();
			
			for(int c = 0; c < concepts.length(); c++) {
				if((c != i)&&(concepts.type(c).contentEquals("problem")||concepts.type(c).contentEquals("test")||concepts.type(c).contentEquals("treatment"))) {
				buf = concepts.mention(c).split(" ");

				for(a = 0; a < buf.length; a++) {
					if(!list.common(buf[a])) {
						found = true;
						words.add(buf[a]);
						if(concepts.mention(i).contains(buf[a])) {
							similar = true;
						}
					}
				}
					
				for(a = 0; a < search.results.length; a++) { 
				//	System.out.println(search[a]);
					for(b = 0; b < words.size(); b++) {
						if(search.results[a].contains(words.get(b))) {
							words.remove(b);
						}
					}
				}
				
				if(found&&similar) {
					//	System.out.println(doc.line(concepts.endLine(c)));
					if(words.isEmpty()) {
						buf1 = doc.line(concepts.endLine(c));
						if(tense == null) {
							List<String[]> out = new ArrayList<String[]>();
							out.add(list.past_headings);
							out.add(list.present_headings);
							out.add(list.future_headings);
							//String[][] out = { medicine_headings, hospital_course_headings };
							if(heading( out , i))
								if(heading(out, c)) {
									if(concepts.type(i).contentEquals(concepts.type(c))) {
										makePairs(i, c, concepts.type(i), 0, rule);
										return;
									}
								}
						}
						
						
						if(list.find(tense, buf1)) {
							if(concepts.type(i).contentEquals(concepts.type(c))) {
								makePairs(i, c, concepts.type(i), 0, rule);
								return;
							}
						}
				}
			}
			found = false;
			similar = false;
			words.clear();
		}}}}    */
	/*
	static void rule8(int i) {   // google search II
		
		query in = new query();
		int rule = 8;
		int threshold = 2000000;
		
		List<String[]> out = new ArrayList<String[]>();
		out.add(list.present_headings);
		
		
		if(!concepts.type(i).contentEquals("treatment"))
			return;
		if(!heading(out, i))
			return;
		
		int a = 0, b = 0, c = 0;
		
		for(c = 0; c < concepts.length(); c++) {
	
			
			if(concepts.type(c).contentEquals("treatment"))
				if(c!=i)
					if(heading(out, c)) {
				
				String[] buf1 = concepts.mention(i).split(" ");
				String gSearch1 = "";
				
				for(a = 0; a < buf1.length; a++) 
				if(!list.common(buf1[a]))
					gSearch1 += buf1[a] + " ";
				
				if(gSearch1.length() > 0) {
					String[] buf2 = concepts.mention(c).split(" ");
					String gSearch2 = "";
					
					for(a = 0; a < buf2.length; a++) 
						if(!list.common(buf2[a]))
							gSearch2 += buf2[a] + " ";
					
				if(gSearch2.length() > 0) {
					gSearch1 = gSearch1 + gSearch2.substring(0, gSearch2.length()-1);
					in = gSearch(gSearch1); 
					for(b = 0; b < in.results.length; b++) {
						if(in.results[b].contentEquals(gSearch1)) {
							if(in.count[b] > threshold) {
								makePairs(i, c, concepts.type(i), 0, rule);
							}
						}
					}
			}}
		}
		
	}
	}   */
	
	
	//======================================================================== 
	
	static String [] getAdjacent ( String [] list, int base, int offset ) { //Check words that surround a string
		
		int left;
		int right;
		String[] adj;
		adj = new String[2];
		left = base - offset;
		right = base + offset;
		
		if (!(left < 0) ) {
			
			adj[0] = list[left];
			
			if (!(right>=list.length)) {
				
				adj[1] = list[right];
			
			}
			
		}
		
		return adj;
	}
	
	
	static boolean isRelated ( String [] list, String related, int base, int offset ) { //Check words that surround a string
		
		int left;
		int right;
		
		left = base - offset;
		right = base + offset;
		
		if (!(left < 0) && !(right>=list.length) ) {
			
			if ( list[left].contains(related) || list[right].contains(related) ) {
				
				return true;
			}
		}
		
		return false;
	}

	static void gSearch4(String phrase) {
		int pos1 = 0;
		int pos2 = 0;

		
		if(phrase.length() > 90)
			return;
		
		try {
			URL google = new URL("http://google.com/complete/search?q=" + phrase.replace(" ", "+") + "&output=toolbar");
			URLConnection yc = google.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				pos1 = line.indexOf("<CompleteSuggestion>");
				while(pos1 != -1) {
					pos1 = line.indexOf("data=", pos1) + 6;
					pos2 = line.indexOf('"', pos1);
					System.out.print("Suggestion: " + line.substring(pos1,pos2) + " ");
					if(line.substring(pos2, pos2 + 20).contains("<num_queries")) {
						pos1 = line.indexOf("int=",pos2) + 5;
						pos2 = line.indexOf('"', pos1);
						System.out.println("Number of results: " + line.substring(pos1, pos2));
					} else {
						System.out.println("Number of results: 0");
					}
					pos1 = line.indexOf("<CompleteSuggestion>", pos2);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}	
	
	static query gSearch(String phrase) {
		
		for(int c = 0; c < list.queries.size(); c++) {
			if(list.queries.get(c).phrase.contentEquals(phrase))
				return list.queries.get(c);
		}
		
		query back = new query();
//		int pos1 = 0;
//		int pos2 = 0;
//		List<String> results = new ArrayList<String>();
//		List<Integer> count = new ArrayList<Integer>();
		
	//	if(phrase.length() > 90)
	//		return back;
		
		try {
		//	URL google = new URL("http://google.com/complete/search?q=" + phrase.replace(" ", "+") + "&output=toolbar");
		//	URL google = new URL("http://www.google.com/#sclient=psy&hl=en&source=hp&q=bed+bound&aq=f&aqi=g5&aql=&oq=&pbx=1&bav=on.2,or.r_gc.r_pw.&fp=64480e3e283e02ba&biw=1252&bih=602");
			String search = "";
			URL google = new URL("http://www.bing.com/search?q=" + search + "&qs=n&sk=&form=QBRE");
			URLConnection yc = google.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
			/*	pos1 = line.indexOf("<CompleteSuggestion>");
				while(pos1 != -1) {
					pos1 = line.indexOf("data=", pos1) + 6;
					pos2 = line.indexOf('"', pos1);
					results.add(line.substring(pos1,pos2));
					System.out.print("Added: " + line.substring(pos1,pos2) + " ");
					if(line.substring(pos2, pos2 + 20).contains("<num_queries")) {
						pos1 = line.indexOf("int=",pos2) + 5;
						pos2 = line.indexOf('"', pos1);
						if(line.substring(pos1, pos2).length() < 9) {
							System.out.println(Integer.parseInt(line.substring(pos1, pos2)));
							count.add(Integer.parseInt(line.substring(pos1, pos2)));
						} else {
							System.out.println("10000000");
							count.add(Integer.parseInt("10000000"));
						}
					} else {
						System.out.println(" 0");
						count.add(0);
					}
					pos1 = line.indexOf("<CompleteSuggestion>", pos2);
				}
				String[] stringOut = new String[results.size()];
				int[] intsOut = new int[count.size()];
				for(int c = 0; c < results.size(); c++) {
					stringOut[c] = results.get(c);
					intsOut[c] = count.get(c);
				}
				back = new query(phrase, stringOut, intsOut);   */ 
				
				System.out.println(line);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	//	Database.addQuery(back);
	//	list.queries.add(back);
		
		return back;
	}	
	
	static void cleanPairs() {
		int c = 0, d = 0, chains = 0;
		List<int[]> chainList = new ArrayList<int[]>();
		boolean flag1 = false, flag2 = false;
		int sl = 0, dl = 0, sw = 0, dw = 0;
		int sel = 0, del = 0, sew = 0, dew = 0;
		List<ConceptPairs> tpairs = new ArrayList<ConceptPairs>();
		int[] newPair = new int[2];
		
		for(c = 0; c < pairs.size(); c++)
			if(pairs.get(c).chain > chains)
				chains = pairs.get(c).chain;
		
		
		for(int chain = 1; chain <= chains; chain++) {
		chainList.clear();
		for(c = 0; c < pairs.size(); c++)
			if(pairs.get(c).chain == chain) {
				flag1 = flag2 = true;
				for(d = 0; d < chainList.size(); d++) {
					if(pairs.get(c).source == chainList.get(d)[0])
						flag1 = false;
					if(pairs.get(c).dest == chainList.get(d)[0])
						flag2 = false;
				}
				if(flag1) {
					newPair[0] = pairs.get(c).source;
					newPair[1] = pairs.get(c).rule;
					chainList.add(newPair);
					newPair = new int[2];
				}
				if(flag2) {
					newPair[0] = pairs.get(c).dest;
					newPair[1] = pairs.get(c).rule;
					chainList.add(newPair);
					newPair = new int[2];
				}
			}
		
		sl = concepts.startLine(chainList.get(0)[0]);
		sw = concepts.startWord(chainList.get(0)[0]);
		sel = concepts.endLine(chainList.get(0)[0]);
		sew = concepts.endWord(chainList.get(0)[0]);
		
		for(d = 1; d < chainList.size(); d++) {
			if((sl > concepts.startLine(chainList.get(d)[0]))||((sl == concepts.startLine(chainList.get(d)[0]))&&(sw > concepts.startWord(chainList.get(d)[0])))) {
				sl = concepts.startLine(chainList.get(d)[0]);
				sw = concepts.startWord(chainList.get(d)[0]);
				sel = concepts.endLine(chainList.get(d)[0]);
				sew = concepts.endWord(chainList.get(d)[0]);
			}	
		}
		
		c = 0;
		int rule = 0;
		
		while(c < chainList.size()-1) {
			
			dl = 10000;
			dw = 10000;
			
			for(d = 0; d < chainList.size(); d++) {
				if(chainList.get(d)[0] != (-1)) {
					if((concepts.startWord(chainList.get(d)[0]) == sw)&&(concepts.startLine(chainList.get(d)[0]) == sl)) {
						newPair[0] = -1;
						chainList.set(d, newPair);
					}
				}
			}
			
			for(d = 0; d < chainList.size(); d++) {
				if(chainList.get(d)[0] != -1) {
					if((dl > concepts.startLine(chainList.get(d)[0]))||((dl == concepts.startLine(chainList.get(d)[0]))&&(dw > concepts.startWord(chainList.get(d)[0])))) {
						dw = concepts.startWord(chainList.get(d)[0]);
						dl = concepts.startLine(chainList.get(d)[0]);
						dew = concepts.endWord(chainList.get(d)[0]);
						del = concepts.endLine(chainList.get(d)[0]);
						rule = chainList.get(d)[1];
					}
				}
			}

			
			if((dl != 10000)&&(dw != 10000)) {
				ConceptPairs np = new ConceptPairs();
				np.chain = chain;
				np.dest = concepts.getNum(dl, dw, del, dew);
				np.source = concepts.getNum(sl, sw, sel, sew);
				np.rule = rule;
				if(concepts.type(np.dest).contentEquals(concepts.type(np.source))) {
						np.type = "coref " + concepts.type(np.dest);
				} else {
					if(!concepts.type(np.source).contentEquals("pronoun")&&!concepts.type(np.source).contentEquals("none"))
						np.type = "coref " + concepts.type(np.source);
					else if(!concepts.type(np.dest).contentEquals("pronoun")&&!concepts.type(np.dest).contentEquals("none"))
						np.type = "coref " + concepts.type(np.dest);
					else
						np.type = "None";
				}
				if(np.source != -1)
	    			  if(np.dest != -1)
	    				  if(!np.type.contentEquals("pronoun")&&!np.type.contentEquals("none"))
	    					//  if(Database2.checkType(concepts.mention(np.source), concepts.mention(np.dest), np.type))
	    					  	  tpairs.add(np);
			}
			
			sl = dl;
			sw = dw;
			sel = del;
			sew = dew;
			
			c++;
		}
		}
		
		pairs.clear();
		pairs.addAll(tpairs);
	}
	
	static void makeChains() {
		int chain = 1;
		int i = 0;
		
		for(i = pairs.size()-1; i >= 0; i--) {
			if(pairs.get(i).chain == 0) {
				Chains(chain, pairs.get(i).source);
				chain++;
			}
		}
	}
	
	static void Chains(int Chain, int source) {
		int i = 0; 
		ConceptPairs np = new ConceptPairs();
		
		for(i = 0; i < pairs.size(); i++) {
			if((pairs.get(i).source == source)&&(pairs.get(i).chain == 0)) {
				np = pairs.get(i);
				np.chain = Chain;
				pairs.set(i, np);
				Chains(Chain, pairs.get(i).dest);
			} 
			if((pairs.get(i).dest == source)&&(pairs.get(i).chain == 0)) {
				np = pairs.get(i);
				np.chain = Chain;
				pairs.set(i, np);
				Chains(Chain, pairs.get(i).source);
			}
		}
	}
}  


