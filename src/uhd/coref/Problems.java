package uhd.coref;

import java.util.List;

import org.eclipse.swt.graphics.Point;

public class Problems {
	
	static Lists list;
	static Concepts concepts;
	static Doc doc;
	static String type;
	
	Problems(Lists listIn, Concepts cons, Doc document) {
		list = listIn;
		concepts = cons;
		doc = document;
	}
	
	public ConceptPairs check(int con, String t) {
		ConceptPairs back = new ConceptPairs();
		
		type = t;
		
		back.chain = 0;
		back.source = con;
		back.dest = -1;
		back.type = type;
		back.rule = 0;
		
		if(!concepts.type(con).contentEquals(type))
			return back;
		
		back.dest = exactMatch(con);
		back.rule = 21;
		
		if(back.dest != -1) {
		//	System.out.println(concepts.mention(con) + "->" + concepts.mention(back.dest));
		//	if(Database2.checkType(concepts.mention(back.source), concepts.mention(back.dest), back.type))
				return back;
		//	else
		//		back.dest = -1;
		}
		
	//	back.dest = looseMatch(con);
	//	back.rule = 37;
	//	if(back.dest > -1) 
	//		return back;
		
		back.dest = acronyms(con);
		back.rule = 22;
		if(back.dest > -1) 
			return back;
		
		back.dest = rule1(con);
		back.rule = 24;
		if(back.dest > -1) 
			return back;
	
		back.dest = matchCaps(con);
		if(back.dest > -1)
			return back;
		
		return back;
	}
	
	public ConceptPairs check1(int con, String t) {
		ConceptPairs back = new ConceptPairs();
		
		type = t;
		
		back.chain = 0;
		back.source = con;
		back.dest = -1;
		back.type = type;
		back.rule = 0;
	
		if(concepts.mention(con).contentEquals("which")) {
			if(con > 0)
		//	if(concepts.type(con-1).contentEquals(type)) {
				back.dest = People.rule2(con);
				back.rule = 27;
				if(back.dest > -1)
					return back;
		//	}
		} 
		
		if(concepts.mention(con).contentEquals("that")) {
			if(People.getWordDistance(con-1, con) == 1) {
				back.dest = con-1;
				return back;
			}
		}
		
		if(!concepts.type(con).contentEquals(type))
			return back;
		
		back.dest = rule1(con);
		back.rule = 24;
		if(back.dest > -1) 
			return back;

		back.dest = checkIsReverse(con);
		back.rule = 25;
		if(back.dest > -1)
			return back;
	
		back.dest = checkIsFoward(con);
		back.rule = 26;
		if(back.dest > -1)
			return back;
		
	//	back.dest = checkProcedure(con);
	//	back.rule = 27;
	//	if(back.dest > -1)
	//		return back;
		
		
		return back;
	}
	
	static int checkProcedure(int i) {
		int back = -1;
		
		if(concepts.startWord(i) > 0) {
			if(doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("for")) {
				for(int c = i+1; c < concepts.length(); c++) {
					if(concepts.mention(c).contains("procedure")||concepts.mention(c).contains("surgery"))
					if(concepts.type(i).contentEquals(concepts.type(c))) {
						System.out.println(concepts.mention(i) + "->" + concepts.mention(c));
						return c;
					}
				}
			}
		}
		
		return back;
	}
	
	static int checkIsReverse(int i) {
		
		if(i > 0) {
			if(concepts.type(i-1).contentEquals(type)) {
			//	System.out.println(doc.substr(concepts, i-1, i).trim());
				if(list.findExact(list.is_phrases, doc.substr(concepts, i-1, i).trim())) {
					if(!(list.isAdj(concepts.mention(i))||list.isAdj(concepts.mention(i-1))))
						return i-1;
				}
			}
		}
		
		return -1;
	}
	
	static int checkIsFoward(int i) {
		if(i < concepts.length()-1) {
			if(concepts.type(i+1).contentEquals(type)) {
		//		System.out.println(doc.substr(concepts, i-1, i).trim());
				if(list.findExact(list.is_phrases, doc.substr(concepts, i, i + 1).trim())) {
					if(!(list.isAdj(concepts.mention(i))||list.isAdj(concepts.mention(i+1))))
						return i + 1;
				}
			}
		}
		
		return -1;
	}
	
	static int acronyms(int i) {
		int back = -1;
		
		Point loc = doc.getIndex(concepts, i);
		
		if(loc.x >= 0 && loc.y >= 0) {
		
		String[] test = doc.toString().substring(loc.x, loc.y).split(" ");
		
		for(int c = 0; c < test.length; c++) {
			if(test[c].length() > 1)
			if(People.isUpperCase(test[c])) {
		//		if(test[c].length() < 3) {
			//	   return -1;//	back = checkAcro2length(test[c]);
	//			} 		
					back = checkAcronym(test[c], i);
		//		}
			}
		}
		
		if(back != -1)
			if(concepts.startLine(back) != concepts.startLine(i)) 
				if(!doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("no")&&!doc.word(concepts.startLine(back), concepts.startWord(back)-1).contentEquals("no"))
					return back;
		
		}
		
		return -1;
	}
	
	static int checkAcronym(String a, int i) {
		int back = -1;
		int d = 0, e = 0;
		String[] phrase;
		Point loc;
		boolean flag = false;
		
		String test = a;
		
		for(int c = 0; c < concepts.length(); c++) {
			
			if(concepts.type(c).contentEquals(type)) {
				loc = doc.getIndex(concepts, c);
				phrase = doc.toString().substring(loc.x, loc.y).split(" ");
				for(d = 0; d < phrase.length; d++) {
					if(People.isUpperCase(phrase[d])&&phrase[d].contentEquals(test)) {
						return c;
					}
					if(e >= test.length()) 
						if(concepts.startLine(i)!=concepts.startLine(c))
						return c;
					if(!phrase[d].isEmpty())
					if(!((Character.toLowerCase(phrase[d].charAt(0)) >= 'a')&&(Character.toLowerCase(phrase[d].charAt(0)) <= 'z')))
					if((Character.toLowerCase(phrase[d].charAt(0)) == Character.toLowerCase(test.charAt(e)))&&!list.common(phrase[d])) {
						e++;
						flag = true;
					} else if(flag) {
						flag = false;
						e = 0;
					}
				}
				if(e == test.length()) {
					if(concepts.startLine(i)!=concepts.startLine(c))
						return c;
				}
			}
		}
		return back;
	}
	
	static int matchCaps(int i) {
		int back = -1;
		int c = 0, d = 0, e = 0;
		
		Point loc = doc.getIndex(concepts, i);
		
		String[] name1;
		String[] name2;
		
		if(loc.x >= 0 && loc.y >= 0) {
		
		if(People.isUpperCase(doc.toString().substring(loc.x, loc.y))) {
	//		System.out.println(concepts.mention(i));
			name1 = concepts.mention(i).split(" ");
			for(c = 0; c < concepts.length(); c++) {
				if(doc.getIndex(concepts, c).x >= 0 && doc.getIndex(concepts, c).y >= 0)
				if(People.isUpperCase(doc.toString().substring(doc.getIndex(concepts, c).x, doc.getIndex(concepts, c).y))) {
					name2 = concepts.mention(c).split(" ");
					for(d = 0; d < name1.length; d++)
						for(e = 0; e < name2.length; e++) {
							if(name1[d].contentEquals(name2[e]))
								return c;
						}
				}
			}
		}
		
		}
		return back;
	}
	
	static int rule1(int i) {  // Acronyms for problem mentions refer to the problem mention
		
		int back = -1;
		
		if(!concepts.type(i).contentEquals(type))
			return back;
		
		String buf = concepts.mention(i);
		
		String [] words	= buf.split(" ");
		
		if(words.length < 2) {
			return back;
		}
		
		buf = "";
		for(int c = 0; c < words.length; c++) {
			if(!words[c].isEmpty())
			if((words[c].charAt(0) >= 'a')&&(words[c].charAt(0) <= 'z')&&!list.common(words[c]))
				if(words[c].length() > 1)
					buf += words[c].charAt(0);
		}
		
		String ss = "";
		String [] test;
		
		int sw = 0, ew = 0;
		
		for(int c = 2; c <= buf.length(); c++) {
			for(int d = 0; c+d <= buf.length(); d++) {
				ss = buf.substring(d, c+d);
				for(int e = 0; e < concepts.length(); e++) {
					sw = doc.getIndex(concepts, e).x;
					ew = doc.getIndex(concepts, e).y;
					if(e != i && sw > -1 && ew > -1) {
						test = doc.toString().substring(sw, ew).split(" ");
						for(int f = 0; f < test.length; f++) {
							if(ss.contentEquals(test[f].toLowerCase())&&People.isUpperCase(test[f])) {
								if(concepts.type(i).contentEquals(concepts.type(e))) {
								//	if(concepts.startLine(e) != concepts.startLine(i))
									//	if(!doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("no")&&!doc.word(concepts.startLine(back), concepts.startWord(back)-1).contentEquals("no"))
										if(checkLines(i, e))
							//				if(Database2.match(concepts.mention(i), concepts.mention(c)))
												return e;
								}
							}
						}
					}
				}
			}
		}
		
		return -1;
	}
	
	
	
	static int looseMatch(int i) {
		int back = -1;
		String [] split = concepts.mention(i).split(" ");

		if(split.length > 2) {
			String lWord = split[split.length-1];
			 
			for(int c = 0; c < concepts.length(); c++) {
				if(concepts.type(c).contentEquals(type)) {
					split = concepts.mention(c).split(" ");
					if(split.length > 2) 
					if(split[split.length-1].contentEquals(lWord))
						return c;
				}
			}
		
		}
		
		return back;
	}
	
	static int exactMatch(int i) {
		int back = -1;
		int d = 0, e = 0, count = 0;
		
		boolean umls = false;
		
	//	boolean cncr = false;
		
	//	if(concepts.mention(i).contains("carcinoma")||concepts.mention(i).contains("cancer"))//||concepts.mention(i).contains("tumor"))
	//		cncr = true;
		
		String[] phrase1 = People.trimCommon(concepts.mention(i)).replace('-', ' ').split(" ");
		String[] phrase2;
		
		
		for(int c = i+1; c < concepts.length(); c++) {
			if((i != c)&&(concepts.type(c).contentEquals(concepts.type(i)))) {
				if(concepts.mention(i).contentEquals(concepts.mention(c)))
					if(checkLines(i, c))
		//				if(!(concepts.startLine(i) == concepts.startLine(c)))
							return c;
				
		//		if(cncr)
		//			if(concepts.mention(c).contains("carcinoma")||concepts.mention(c).contains("cancer"))//||concepts.mention(c).contains("tumor"))
		//				if(checkLines(i, c))
		//				return c;
				
			}
		}
		
		for(int c = i+1; c < concepts.length(); c++) {
			if((i != c)&&(concepts.type(c).contentEquals(concepts.type(i)))) {
				
				phrase2 = People.trimCommon(concepts.mention(c)).replace('-', ' ').split(" ");
				for(d = 0; d < phrase1.length; d++)
					for(e = 0; e < phrase2.length; e++) {
						if(wordMatch(phrase1[d], phrase2[e])) {
							count++;
						}
					}
				if(((count == phrase1.length)||(count == phrase2.length))) {
			//		System.out.println(concepts.mention(i) + " " + phrase1.length + "->" + concepts.mention(c) + " " + phrase2.length);
					if(!phrase1[0].isEmpty() && !phrase2[0].isEmpty())
						if(checkLines(i, c))
								return c;
				} else if(count > 0) {
					umls = Database2.match(concepts.mention(c), concepts.mention(i));
					
					if(umls)
						if(checkLines(i, c))
							return c;
				}
				count = 0;
			}
		}
		
		return back;
	}
	
	static boolean checkLines(int i, int c) {
		
		if(!concepts.type(i).contentEquals(concepts.type(c)))
				return false;

		
		if(People.isInteger(doc.word(concepts.startLine(i), concepts.startWord(i)+1))) {
			if(People.isInteger(doc.word(concepts.startLine(c), concepts.startWord(c)+1))) {
				if(doc.word(concepts.startLine(c), concepts.startWord(c)+1).contentEquals(doc.word(concepts.startLine(i), concepts.startWord(i)+1))) {
					return true;
				} else {
					return false;
				}
			} 
		} 
		
		if(concepts.type(i).contentEquals("treatment"))
			return true;
		
		
		if(concepts.startWord(i) == 0)
			return true;
		if(concepts.startWord(c) == 0)
			return true;
		
		if(doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("is")) {
			if(doc.word(concepts.startLine(i), concepts.startWord(i)-2).contentEquals(doc.word(concepts.startLine(c), concepts.startWord(c)-2))) {
				return true;
			} else {
				return false;
			}
		}
		if(doc.word(concepts.startLine(c), concepts.startWord(c)-1).contentEquals("is")){
			if(doc.word(concepts.startLine(i), concepts.startWord(i)-2).contentEquals(doc.word(concepts.startLine(c), concepts.startWord(c)-2))) {
				return true;
			} else {
				return false;
			}
		}
		
		if(doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("are"))
			return false;
		if(doc.word(concepts.startLine(c), concepts.startWord(c)-1).contentEquals("are"))
			return false;
		
		if(doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("no"))
			if(!doc.word(concepts.startLine(c), concepts.startWord(c)-1).contentEquals("no"))
				return false;
		
		if(!doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("no"))
			if(doc.word(concepts.startLine(c), concepts.startWord(c)-1).contentEquals("no"))
				return false;
		
		return true;
	}

	static boolean wordMatch(String w1, String w2) {
		
		if(list.isSyn(w1, w2))
			return true;
		
		if(w1.contains("carcinoma")||w1.contains("cancer"))
			if(w2.contains("carcinoma")||w2.contains("cancer"))
				return true;
		
		if(w1.length()<4 || w2.length()<4)
			return w1.contentEquals(w2);
		
		if(w1.length() >= (1.5*w2.length()))
			return false;
		
		if(w2.length() >= (1.5*w1.length()))
			return false;
		
		int length; 
		if(w1.length() > w2.length())
			length = (w2.length() * 8)/10;
		else
			length = (w1.length() * 8)/10;
		
		for(int c = 0; c < length; c++)
			if(w1.charAt(c) != w2.charAt(c))
				return false;
		
	//	if(!w1.endsWith("ing")&&w2.endsWith("ing"))
	//		return false;
	//	if(w1.endsWith("ing")&&!w2.endsWith("ing"))
	//		return false;
		
		
		return true;
	}
	
}
