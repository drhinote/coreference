package uhd.coref;

import org.eclipse.swt.graphics.Point;


public class People {
	
	static Lists list;
	static Concepts concepts;
	static Doc doc;
	static int subject;
	static boolean female = false;

	
	People(Lists listIn, Concepts cons, Doc document) {
		list = listIn;
		concepts = cons;
		doc = document;
		
		subject = findSubject();
		female = findGender();
	}
	
	public ConceptPairs check(int con) {
		ConceptPairs back = new ConceptPairs();
		
		if(list.pronoun(concepts.mention(con))) {
			back = linkPronoun(con);
		} else {
			back = linkName(con);
		}
		
		return back;
	}
	
	public ConceptPairs check2(int concept) {
		ConceptPairs back = new ConceptPairs();
		
		back.chain = 0;
		back.source = concept;
		back.dest = -1;
		back.type = "coref person";
		back.rule = 0;
		
		String mention = concepts.mention(concept);
		
		if(!(concepts.type(concept).contentEquals("person") || concepts.type(concept).contentEquals("people"))) 
			return back;
		
		if(list.isDoctor1(mention)) {
			back.rule = 6;
			back.dest = linkDoctor(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}
		
		if((concepts.startWord(concept) == 0)&&(!list.isDoctor1(mention))) {
			back.rule = 10;
			back.dest = linkFirstName(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}
		
		back.rule = 13;
		back.dest = linkCapsNames(concept);
		back.type = "coref " + concepts.type(concept);
		
		return back;		
	}

	
	
	static int linkCapsNames(int i) {
		int back = -1;
		int c = 0, d = 0, e = 0;
		
		Point loc = doc.getIndex(concepts, i);
		
		String[] name1;
		String[] name2;
		
		
		
		if(loc.x > -1 && loc.y > -1)
		if(isUpperCase(doc.toString().substring(loc.x, loc.y))) {
	//		System.out.println(concepts.mention(i));
			name1 = concepts.mention(i).split(" ");
			for(c = 0; c < concepts.length(); c++) {
				if(doc.getIndex(concepts, c).x > -1 && doc.getIndex(concepts, c).y > -1)
				if(isUpperCase(doc.toString().substring(doc.getIndex(concepts, c).x, doc.getIndex(concepts, c).y))) {
					name2 = concepts.mention(c).split(" ");
					for(d = 0; d < name1.length; d++)
						for(e = 0; e < name2.length; e++) {
							if(name1[d].contentEquals(name2[e]))
								if(concepts.type(i).contentEquals(concepts.type(c)))
									return c;
						}
				}
			}
		}
		
		name1 = concepts.mention(i).split(" ");
		for(c = 0; c < concepts.length(); c++) {
			name2 = concepts.mention(c).split(" ");
			for(d = 0; d < name1.length; d++)
				for(e = 0; e < name2.length; e++) {
					if(name1[d].length() > 2 && name2[e].length() > 2) {
						if(name1[d].charAt(0) == name1[d].charAt(1))
							if(name1[d].charAt(1) == name1[d].charAt(2))
						if(name2[e].charAt(0) == name2[e].charAt(1))
							if(name2[e].charAt(1) == name2[e].charAt(2))
						if(name2[e].charAt(0) == name1[d].charAt(0)) {
						//	System.out.println(concepts.mention)
							return c;
						}
					}
				}
		}
		
		return back;
	}
	
	static int linkFirstName(int i) {
		int back = -1;
		
		int nameLength = concepts.mention(i).split(" ").length;
		String line = doc.line(concepts.startLine(i));
		int lineLength = line.split(" ").length;

		
		
		if(!(line.contains("you")||line.contains("your")||line.contains("yours")||line.contains("yourself")))
		if((lineLength-nameLength) > 6)
			back = subject;
		
		return back;
	}
	
	static ConceptPairs linkName(int concept) {
		ConceptPairs back = new ConceptPairs();
		
		back.chain = 0;
		back.source = concept;
		back.dest = -1;
		back.type = "coref person";
		back.rule = 0;
		
		String mention = concepts.mention(concept);
		
		if(mention.contentEquals("they")) {
			back.rule = 11;
			back.dest = checkPronounThey(concept);
			back.type = "coref person";
			if(back.dest > -1)
				return back;
		}
		
		if(mention.contentEquals("we")||mention.contentEquals("our")) {
			for(int c = 0; c < concepts.length(); c++)
				if(c!=concept)
				if((concepts.mention(c).contentEquals("we")||concepts.mention(c).contentEquals("our"))) {//&&concepts.type(c).contentEquals("person")) {
					back.dest = c;
					back.type = "coref person";
					return back;
				}
			return back;
		}
		
		if(mention.contentEquals("i")||mention.contentEquals("me")||mention.contentEquals("myself")) {
			for(int c = 0; c < concepts.length(); c++)
				if(c!=concept)
				if((concepts.mention(c).contentEquals("i")||(concepts.mention(c).contentEquals("me")||mention.contentEquals("myself")))&&(concepts.type(c).contentEquals("person")||concepts.type(c).contentEquals("person"))) {
					back.dest = c;
					back.type = "coref person";
					return back;
				}
			return back;
		}
		
		if(mention.contains("patient")||mention.contains("the pt")||mention.contentEquals("pt")) {
			
			back.rule = 3;
			back.dest = rule3(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}
		
		if(mention.contains(" male")||mention.contains(" female")||mention.contains(" man")||mention.contains(" woman")) {
			back.rule = 3;
			back.dest = rule3(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}
		
		if(concepts.type(concept).contentEquals("person") || concepts.type(concept).contentEquals("people")) {
			 back.rule = 7;
			 back.dest = exactNameMatch(concept);
			 back.type = "coref " + concepts.type(concept);
			 if(back.dest > -1)
					return back; 
		}

		if((concepts.type(concept).contentEquals("person")|| concepts.type(concept).contentEquals("people"))&&(concepts.type(concept + 1).contentEquals("person")|| concepts.type(concept + 1).contentEquals("people"))&&doc.substr(concepts, concept, concept + 1).contentEquals("is")) {
			 back.rule = 12;
			 back.dest = concept + 1;
			 back.type = "coref " + concepts.type(concept);
			 if(back.dest > -1)
					return back; 
		}
		
		return back;
	}
	
	
	static int exactNameMatch(int i) {
		int back = -1;
		int d = 0, e = 0, count = 0;
		boolean matched = false;
		
		
		String[] name = trimCommon(concepts.mention(i)).split(" ");
		String[] test;
		
		
		
		for(int c = 0; c < i; c++) {

			test = trimCommon(concepts.mention(c)).split(" ");
			count = test.length;
			if((concepts.type(c).contentEquals("person")||concepts.type(c).contentEquals("people"))&&(test.length==name.length)) {
				for(d = 0; d < name.length; d++) {
					for(e = 0; e < test.length; e++) {
						if(!name[d].isEmpty() && !test[e].isEmpty()) {
						
							if(name[d].contentEquals("mother")&&test[e].contentEquals("mom"))
								return c;
							if(name[d].contentEquals("mom")&&test[e].contentEquals("mother"))
								return c;
							if(name[d].contentEquals("father")&&test[e].contentEquals("dad"))
								return c;
							if(name[d].contentEquals("dad")&&test[e].contentEquals("father"))
								return c;
							
						if(name[d].contentEquals(test[e])||initial(name[d], test[e])) {
								count--;
								matched = true;
						} 
						}
					}
					if(!matched)
						count++;
					matched = false;
				}
				if((count < 1)&&concepts.type(i).contentEquals(concepts.type(c))) {
					if(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))
					if(!list.isDoctor1(concepts.mention(c))) {
						back = c;
						return back;
					} else {
						if(!doc.word(concepts.startLine(c), concepts.startWord(c)+1).contentEquals(":")) {
							back = c;
							return back;
						}
					}
				}
			}
		}
		
		return back;
	}
	
	static boolean initial(String s1, String s2) {
		if(s1.isEmpty())
			return false;
		
		if (s2.isEmpty())
			return false;
		
		if(!((s1.replace(".", "").length() == 1)||(s2.replace(".", "").length() == 1)))
			return false;

		if(s1.charAt(0) != s2.charAt(0))
			return false;
		
		return true;
	}
	
	static int linkDoctor(int i) {
		int back = -1;
		
		if(i < concepts.length()-1)
		if(concepts.type(i+1).contentEquals("person")||concepts.type(i+1).contentEquals("people")|| concepts.type(i+2).contentEquals("person")||concepts.type(i+2).contentEquals("people")) {
			back = checkNextDoctor(i);
			if(back > -1)
				return back;
		}
		
		if(i > 0)
		if(concepts.type(i-1).contentEquals("person")||concepts.type(i-1).contentEquals("people")|| concepts.type(i-2).contentEquals("person")||concepts.type(i-2).contentEquals("people")) {
			back = checkPrevDoctor(i);
			if(back > -1)
				return back;
		}
		
		back = checkDoctorLooseMatch(i);
		
		return back;
	}
	
	static int checkDoctorLooseMatch(int i) {
		int back = -1;
		
		String[] name1 = trimCommonWords(concepts.mention(i)).split(" ");
		String[] name2;
		
	//	System.out.println(concepts.mention(i));
		
		if(name1.length < 1)
			return back;
		
		if((name1.length == 1)&&(name1[0].contentEquals("")))
			return back;
		
		int d = 0, e = 0;
		
		for(int c = 0; c < concepts.length(); c++) {
		//	System.out.println(concepts.mention(i) + "->" + concepts.mention(c));
			if(c != i)
			if((concepts.type(c).contentEquals("person")|| concepts.type(c).contentEquals("people")))//&&list.isDoctor1(concepts.mention(c)))
			if(isName(concepts.mention(c))) {
		//		System.out.println(concepts.mention(i) + "->" + concepts.mention(c));
				
				name2 = trimCommonWords(concepts.mention(c)).split(" ");
				for(d = 0; d < name1.length; d++)
					for(e = 0; e < name2.length; e++) {
						if(name1[d].contentEquals(name2[e])&&(name1[d].length() > 3))
							if(name1.length == 1 || name2.length == 1)
								return c;
					}
			}
		}
		
	//	if(name1.length == 1)
		
		return back;
	}
	
	
	static int checkPrevDoctor(int i) {
		int back = -1;
		int prev = i - 1;
		
		if(concepts.startWord(i) == concepts.startWord(prev))
			prev--;
		
		if(prev > -1) {
		int dist = getWordDistance(i, prev);
		
		if((dist > 2)||(dist < 1))
			return back;
		
		if(doc.substr(concepts, prev, i).trim().contentEquals("in")||doc.substr(concepts, prev, i).trim().contentEquals("and"))
			return back;
		
		if(concepts.startWord(prev) > 0)
		if(!doc.word(concepts.startLine(prev), concepts.startWord(prev)-1).contentEquals("with"))
			return back;
		
		//System.out.println(dist + " " + concepts.mention(i) + "->" + concepts.mention(prev));
		
		if((trimCommonWords(concepts.mention(i)).length() < 1)&&(trimCommonWords(concepts.mention(prev)).length() > 0))
			back = prev;
		
		if(!(concepts.type(back).contentEquals("person")|| concepts.type(back).contentEquals("people")))
			back = -1;
		}
		
		return back;
	}
	
	static int checkNextDoctor(int i) {
		int back = -1;
		int next = i + 1;
		
		if(concepts.startWord(i) == concepts.startWord(next))
			if(concepts.startLine(i) == concepts.startLine(next))
			  next++;
		
		int dist = getWordDistance(i, next);
		
	//	System.out.println(dist + " " + concepts.mention(i) + "->" + concepts.mention(next));
		
		if((dist > 3)||(dist < 1))
			return back;
		
		if(doc.substr(concepts, i, next).trim().contentEquals("in")||doc.substr(concepts, i, next).trim().contentEquals("and"))
			return back;
		
		if((trimCommonWords(concepts.mention(i)).length() < 1)&&(trimCommonWords(concepts.mention(next)).length() > 0))
			back = next;
		
		if(!(concepts.type(back).contentEquals("person")|| concepts.type(back).contentEquals("people")))
			back = -1;
		
		return back;
	}
	
	static int rule3(int i) {  // The patient is always the subject of the document in the medical records "the patient" "patient" "this" and "pt"
		
		if(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))
		if(!list.isDoctor1(concepts.mention(i))) {
			return subject;
		}
		
		return -1;
	}	
	
	
	
	




	static ConceptPairs linkPronoun(int concept) {
		ConceptPairs back = new ConceptPairs();
		
		back.chain = 0;
		back.source = concept;
		back.dest = -1;
		back.type = "coref person";
		back.rule = 0;
		
		String mention = concepts.mention(concept);
		
		if(mention.contentEquals("which")) {
			back.rule = 2;
			back.dest = rule2(concept);
			back.type = "coref " + concepts.type(back.dest);
			if(back.dest > -1)
				return back;
		}
		
		if(mention.contentEquals("this")) {
			back.rule = 8;
			back.dest = checkPronounThis(concept);
			back.type = "coref " + concepts.type(back.dest);
			if(back.dest > -1)
				return back;
		}
		
		if(mention.contentEquals("who")) {
			back.rule = 9;
			back.dest = checkPronounWho(concept);
			back.type = "coref " + concepts.type(back.dest);
			if(back.dest > -1)
				return back;
		}
		

		
	//	if(mention.contentEquals("that")) {
	//		back.dest = concept-1;
	//		return back;
	//	}
		
		if(!(concepts.type(concept).contentEquals("person")||concepts.type(concept).contentEquals("people")))
			return back;
		
/*		if(female && (gender(concept)==1)) {
			back.rule = 35;
			back.dest = connectDoctorPronoun(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}
		
		if(!female && (gender(concept)==2)) {
			back.rule = 36;
			back.dest = connectDoctorPronoun(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}  */
		
		if(concepts.startWord(concept) == 0) {
			back.rule = 5;
			back.dest = findPrevNamePrevLine(concept);
			back.type = "coref " + concepts.type(concept);
			if(back.dest > -1)
				return back;
		}
			
		
		back.rule = 4;
		back.dest = findPrevNameByLine(concept);
		back.type = "coref " + concepts.type(concept);
		
		
		return back;
	}
	
	static int checkPronounThey(int i) {
		int back = -1;
		int pos = i;
		
		int count = 0;
		
	//	if(concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people"))
	//		if(!list.pronoun(concepts.mention(i-1)))
	//			return i-1;
		
		pos--;
		while((pos > 0)&&(count < 6)) {
			if(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))
			if(list.isDoctor1(concepts.mention(pos)))
				return pos;
			pos--;
			count++;
		}
		
		return back;
	}
	
	
	static int checkPronounWho(int i) {
		int back = -1;
		
		if(i < 2)
			return back;
		
		if((concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people"))&&!list.pronoun(concepts.mention(i-1)))
			return i-1;
		
		if((concepts.type(i-2).contentEquals("person")|| concepts.type(i-2).contentEquals("people"))&&!list.pronoun(concepts.mention(i-2)))
			return i-2;
		
		return back;
	}
	
	static int checkPronounThis(int i) {
		int back = -1;
		
		if(concepts.startWord(i) == 0) {
			if(doc.word(concepts.startLine(i), concepts.startWord(i) + 1).contentEquals("is"))
				if(doc.word(concepts.startLine(i), concepts.startWord(i) + 2).contentEquals("a")||doc.word(concepts.startLine(i), concepts.startWord(i) + 2).contentEquals("an")) {
					String buf = doc.line(concepts.startLine(i));
					if(buf.contains("female") || buf.contains("male") || buf.contains("man") || buf.contains("woman")) {
						return subject;
					}
				}
		}
		
		return back;
	}
	
	static int findPrevNamePrevLine(int i) {
		int back = -1;
		
		
		String line = doc.line(concepts.startLine(i));
		
		if(!(line.contains("you")||line.contains("your")||line.contains("yours")||line.contains("yourself")))
			if((female && (gender(i)==2))||(!female && (gender(i)==1)))
				back = subject;
		
		return back;
	}
	
	static int findPrevNameByLine(int i) {
		int back = -1;
		int nameCount = 0;
		
		if((female&&(gender(i)==1))||(!female&&(gender(i)==2))) {
			back = findPrevDoctor(i);
			return back;
		}
			
			
		if(i != concepts.length()-1)   // if concept is part of a larger mention, see if its a doctor
			if(i != 0) {
				String test = "";
				if(concepts.startWord(i) == concepts.startWord(i+1))
					test = concepts.mention(i+1);
				if(concepts.startWord(i) == concepts.startWord(i-1))
					test = concepts.mention(i-1);
				if(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))
				if(!test.isEmpty()) {
					if(list.isDoctor1(test.substring(test.indexOf(' ') + 1)))
						return subject;
				}
			}
			
		if(concepts.mention(i).contentEquals("you"))
			return subject;
		if(concepts.mention(i).contentEquals("your"))
			return subject;
		if(concepts.mention(i).contentEquals("yours"))
			return subject;
		if(concepts.mention(i).contentEquals("yourself"))
			return subject;
		
		if(concepts.mention(i).contentEquals("i")||concepts.mention(i).contentEquals("me")||concepts.mention(i).contentEquals("my")||concepts.mention(i).contentEquals("our")||concepts.mention(i).contentEquals("we")) {
			for(int c = 0; c < concepts.length(); c++) {
				if(concepts.mention(c).contentEquals("attending"))
					return c;
			}
			return -1;
		}  
				
		for(int c = 1; c < concepts.length()-1; c++) {
			if(concepts.startLine(i) == concepts.startLine(c)) {
				if(!list.pronoun(concepts.mention(c))&&(concepts.startWord(i) > concepts.startWord(c))&&(concepts.type(c).contentEquals("person")|| concepts.type(c).contentEquals("people"))) {
					if(!list.isDoctor1(concepts.mention(c))) {
						back = c;
					} else {
						back = checkDoctorPronoun(i);
					}
					nameCount++;
				}
			}
		}
		
		if(nameCount > 1)
			back = checkLineMultipleNames(i);
		if(nameCount < 1)
			back = checkLineNoNames(i);
		
		return back;
	}
	
	static int findPrevDoctor(int i) {
		
		while(i > 0) {
			if(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))
			if(list.isDoctor1(concepts.mention(i))) {
				return i;
			}
			i--;
		}
		
		return -1;
	}
	
	static int checkLineNoNames(int i) {
		int back = -1;
			
		String line = doc.line(concepts.startLine(i));
		
		if(!(line.contains("you")||line.contains("your")||line.contains("yours")))
	//		if((!female && (gender(i) == 1))||(female && gender(i) == 2))
				back = subject;
		
		
		return back;
		
		
	}
	
	static int checkLineMultipleNames(int i) {  
		int back = -1;
		
		if(concepts.mention(i).contentEquals("him")&&!female) {
			if(!doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("in")) {
				return subject;
			}
			if(concepts.startLine(i) == concepts.startLine(i+1))
				if(concepts.startWord(i) == concepts.startWord(i+1))
					if(!(concepts.type(i+1).contentEquals("person")|| concepts.type(i+1).contentEquals("people")))
						return subject;
			if(concepts.startLine(i) == concepts.startLine(i-1))
				if(concepts.startWord(i) == concepts.startWord(i-1))
					if(!(concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people")))
						return subject;
		} else {
			back = connectDoctorPronoun(i);
		}
		
		if((concepts.mention(i).contentEquals("her")||concepts.mention(i).contentEquals("hers"))&&female) {
			if(!doc.word(concepts.startLine(i), concepts.startWord(i)-1).contentEquals("in")) {
				return subject;
			}
			if(concepts.startLine(i) == concepts.startLine(i+1))
				if(concepts.startWord(i) == concepts.startWord(i+1))
					if(!(concepts.type(i+1).contentEquals("person")|| concepts.type(i+1).contentEquals("people")))
						return subject;
			if(concepts.startLine(i) == concepts.startLine(i-1))
				if(concepts.startWord(i) == concepts.startWord(i-1))
					if(!(concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people")))
						return subject;
		} else {
			back = connectDoctorPronoun(i);
		}
		
		if((concepts.mention(i).contentEquals("he")||concepts.mention(i).contentEquals("his"))&&!female) {
			if(!list.pronoun(concepts.mention(i-1))&&!list.pronoun(concepts.mention(i+1))) {
				return subject;
			}
			if(concepts.startLine(i) == concepts.startLine(i+1))
				if(concepts.startWord(i) == concepts.startWord(i+1))
					if(!(concepts.type(i+1).contentEquals("person")|| concepts.type(i+1).contentEquals("people")))
						return subject;
			if(concepts.startLine(i) == concepts.startLine(i-1))
				if(concepts.startWord(i) == concepts.startWord(i-1))
					if(!(concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people")))
						return subject;
		} else {
			back = connectDoctorPronoun(i);
		}
		
		if(concepts.mention(i).contentEquals("she")&&female) {
			if(!list.pronoun(concepts.mention(i-1))&&!list.pronoun(concepts.mention(i+1))) {
				return subject;
			}
			if(concepts.startLine(i) == concepts.startLine(i+1))
				if(concepts.startWord(i) == concepts.startWord(i+1))
					if(!(concepts.type(i+1).contentEquals("person")|| concepts.type(i+1).contentEquals("people")))
						return subject;
			if(concepts.startLine(i) == concepts.startLine(i-1))
				if(concepts.startWord(i) == concepts.startWord(i-1))
					if(!(concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people")))
						return subject;
		} else {
			back = connectDoctorPronoun(i);
		}
		
		return back;
	}
	
	static int checkDoctorPronoun(int i) {
		int back = -1;
		
		if(doc.word(concepts.endLine(i), concepts.endWord(i)+1).contentEquals("was")||doc.word(concepts.endLine(i), concepts.endWord(i)+1).contentEquals("is")) {
			return subject;
		}
		
		if(concepts.startLine(i) == concepts.startLine(i+1))
			if(concepts.startWord(i) == concepts.startWord(i+1))
				if(!(concepts.type(i+1).contentEquals("person")|| concepts.type(i+1).contentEquals("people")))
					return subject;
		if(concepts.startLine(i) == concepts.startLine(i-1))
			if(concepts.startWord(i) == concepts.startWord(i-1))
				if(!(concepts.type(i-1).contentEquals("person")|| concepts.type(i-1).contentEquals("people")))
					return subject;
		
		int prev = i-1;
		
		if(!concepts.type(prev).contentEquals("person")||!concepts.type(prev).contentEquals("people"))
			prev--;
		
		if(i > 0)
		if((concepts.type(prev).contentEquals("person")||concepts.type(prev).contentEquals("people")) && list.isDoctor1(concepts.mention(prev))) {
			if(doc.substr(concepts, prev, i).contains("in"))
				if(getWordDistance(prev, i) < 5)
					return prev;
		}
		
		for(int c = 0; c < concepts.length(); c++) {
			if(concepts.startLine(c) == concepts.startLine(i))
				if(concepts.type(i).contentEquals("person")||concepts.type(i).contentEquals("people"))
				if(list.isDoctor1(concepts.mention(c)))
					if(!trimCommonWords(concepts.mention(c)).isEmpty())
						return back;
		}
		
		if(!female)
		if(concepts.mention(i).contentEquals("he")||concepts.mention(i).contentEquals("him")||concepts.mention(i).contentEquals("his")) {
			return subject;
		} else {
			back = connectDoctorPronoun(i);
		}
		
		if(female)
		if(concepts.mention(i).contentEquals("her")||concepts.mention(i).contentEquals("hers")||concepts.mention(i).contentEquals("she")) {
				return subject;
		} else {
			back = connectDoctorPronoun(i);
		}
		
		return back;
	}
	
	static int connectDoctorPronoun(int i) {
		int line = concepts.startLine(i);
		int gen = gender(i);
		
		for(int c = 0; c < concepts.length(); c++) {
			if((concepts.startLine(c) == line) && isName(concepts.mention(c)) && (concepts.type(c).contentEquals("person")||concepts.type(c).contentEquals("people"))) { 
				if(female && (gen==1))
					return c;
				if(!female && (gen==2))
					return c;
			}
		}
		
		return -1;
	}
	
	
	static int gender(int i) {
		if(concepts.mention(i).contentEquals("he")||concepts.mention(i).contentEquals("him")||concepts.mention(i).contentEquals("his")||concepts.mention(i).contentEquals("himself")) {
			return 1;
		}
		if(concepts.mention(i).contentEquals("her")||concepts.mention(i).contentEquals("hers")||concepts.mention(i).contentEquals("she")||concepts.mention(i).contentEquals("herself")) {
			return 2;
		}
		
		return 0;
	}
	
	
	static int rule2(int i) {   //  "which" the pronoun refers to the concept mention before it in the same sentence, when not in a question
		
		String buf = concepts.mention(i);
		
		if(concepts.startLine(i) != concepts.startLine(i-1))
			return -1;
		
		int dist = concepts.startWord(i) - concepts.endWord(i-1);
		if((dist < 1) || (dist > 4))
			return -1;
		
		buf = doc.line(concepts.startLine(i));
		
		if(buf.contains("?")) {
			return -1;
		}
		
		if(buf.substring(buf.indexOf("which")-3, buf.indexOf("which")-1).contentEquals("at"))
			return -1;
		
		if(concepts.type(i-1).contentEquals("person")||concepts.type(i-1).contentEquals("people"))
			return -1;
		
		return i - 1;
	}

	
	
	
	
	static boolean isUpperCase(String word) {
		
		for(int c = 0; c < word.length(); c++) {
			if(!(((word.charAt(c) <= 'Z')&&(word.charAt(c) >= 'A'))||word.charAt(c) == ' '))
				return false;
		}
		
		return true;
	}
	
	static int getWordDistance(int a, int b) {
		int dist = -1;
		
		
		if(concepts.startLine(a) == concepts.startLine(b))
			if(concepts.startWord(a) == concepts.startWord(b))
				return 0;
		
		int temp = 0;
		if(a > b) {
			temp = a;
			a = b;
			b = temp;
		}
		
		int sl = concepts.endLine(a);
		int sw = concepts.endWord(a);
		int dl = concepts.startLine(b);
		int dw = concepts.startWord(b);
			
		if(sl == dl) {
			dist = dw - sw;
		}
		else {
			int length = doc.line(sl).split(" ").length;
			dist = length - sw;
			sl++;
			int count = 0;
			while(sl != dl) {
				length = doc.line(sl).split(" ").length;
				dist += length;
				sl++;
				count++;
				if(count > 5)
					break;
			}
			dist +=	dw;		
		}
						
		return dist;
	}
	
	static boolean isName(String in) {
		
		if(trimCommonWords(in).length() > 0)
			return true;
		
		return false;		
	}
	
	static String trimCommonWords(String in) {
		String[] split = in.split(" ");
		String back = "";
			
		for(int c = 0; c < split.length; c++) {
			if(!list.common(split[c]) && !list.isDoctor(split[c])) // && !((split[c].length() == 2) && (split[c].charAt(1) == '.')))
					back += split[c] + " ";
		}
		
		return back.trim();
	}
	
	static String trimCommon(String in) {
		String[] split = in.split(" ");
		String back = "";
			
		for(int c = 0; c < split.length; c++) {
			if(!list.common(split[c]) && !((split[c].length() == 2) && (split[c].charAt(1) == '.')))
				back += split[c] + " ";
		}
		
		return back.trim();
	}
	
	static int findSubject() {  
		String buf[];
		boolean flag = true;
		
		for(int c = 0; c < concepts.length(); c++) {
			if(concepts.type(c).contentEquals("person")||concepts.type(c).contentEquals("people"))
				if(!(concepts.mention(c).contains("patient")||concepts.mention(c).contentEquals("pt")||concepts.mention(c).contains(" pt")))
				if(!list.isDoctor1(concepts.mention(c)))
					if(!(concepts.endLine(c) > doc.body.length-2))
					if(!list.pronoun(concepts.mention(c))) {
						buf = doc.line(concepts.startLine(c)).split(" ");
						for(int d = 0; d < buf.length; d++) {
							if(isInteger(buf[d])&&buf[d].length() > 3) 
								flag = false;
						}
						if(flag) {
		//					System.out.println(concepts.mention(c) + " " + RunDirectory.ffile + " " + doc.body.length);
							return c;
						}
					}
			flag = true;
		}
		
		for(int c = 0; c < concepts.length(); c++) {
			if(concepts.mention(c).contains("patient")||concepts.mention(c).contentEquals("pt")||concepts.mention(c).contains(" pt"))
				return c;
		}
		
		return -1;
	}
	
	static boolean findGender() {
		int m = 0, f = 0;
		
		
		for(int i = 0; i < concepts.length(); i++) {
			if(gender(i) == 1)
				m++;
			if(gender(i) == 2)
				f++;
		}
		
		
		return f >= m;
		
	}
	
	static boolean isInteger( String input )  
	{  
	   try  
	   {  
	      Integer.parseInt( input );  
	      return true;  
	   }  
	   catch( Exception e)  
	   {  
	      return false;  
	   }  
	}  

}
