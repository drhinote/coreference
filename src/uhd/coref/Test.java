package uhd.coref;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {

	static Lists list;
	static Concepts concepts;
	static Doc doc;
	
	Test(Lists listIn, Concepts cons, Doc document) {
		list = listIn;
		concepts = cons;
		doc = document;
	}

	
	public ConceptPairs check(int con) {
		ConceptPairs back = new ConceptPairs();
		String mention = concepts.mention(con);
		back.chain = 0;
		back.source = con;
		back.type = "test";
		back.rule = 0;
		back.dest = -1;
		if(mention.contentEquals("which") || mention.contentEquals("these") || mention.contentEquals("this") ){//|| mention.contentEquals("that")) {
			back.rule = 2;
			back.dest = rule2(con, mention);
			back.type = "coref " + concepts.type(back.dest);
			if(back.dest > -1)
				return back;
		}
		
		
		if (concepts.type(con).contentEquals("test") ) {
			back.type = "test";
			back.dest = looseMatch(con);
			if (back.dest > -1) {
				back.rule = 1;
				return back;
			}
		}
		
		return back;
	}
	
	public ConceptPairs check2(int con) {
		ConceptPairs back = new ConceptPairs();
		
		back.chain = 0;
		back.source = con;
		back.type = "test";
		back.rule = 0;
		back.dest = -1;
		
		if (concepts.type(con).contentEquals("test") ) {
			
			back.dest = pronounMatch(con);
			if (back.dest > -1) {
				back.rule = 6;
				return back;
			}
			
		}
		
		return back;
	
	}
	
	public ConceptPairs check3(int con) {
		ConceptPairs back = new ConceptPairs();
		
		back.chain = 0;
		back.source = con;
		back.type = "test";
		back.rule = 0;
		back.dest = -1;
		
		if (concepts.type(con).contentEquals("test") ) {
			
			back.dest = studyMatch(con);
			if (back.dest > -1) {
				back.rule = 7;
				return back;
			}
			
			
			back.dest = Problems.exactMatch(con);
			if (back.dest > -1)
				if(filter(con, back.dest)) {
				    back.rule = 88;
			//	    System.out.println(concepts.mention(con) + " " + concepts.mention(back.dest));
				    return back;
				}
			
			back.dest = -1;
		}
		
		return back;
	
	}
	
	static boolean filter(int i, int c) {
		String[] l1 = doc.line(concepts.startLine(i)).split(" ");
		String[] l2 = doc.line(concepts.startLine(c)).split(" ");
		
		int w1 = l1.length;
		int w2 = l2.length;
		
		if(concepts.startLine(i) == concepts.startLine(i+1))
			w1 = concepts.startWord(i+1);
		if(concepts.startLine(c) == concepts.startLine(c+1))
			w2 = concepts.startWord(c+1);
				
		int d = 0, e = 0;
		
		for(d = concepts.endWord(i); d < w1; d++)	{
			if(isDouble(l1[d])) {
				for(e = concepts.endWord(c) + 1; e < w2; e++)	{
					if(isDouble(l2[e])) {
						if(l1[d].contentEquals(l2[e])) {
							
							return true;
						}
					}
				}		
			}
		}
		
		return false;
		
	}
	
	static boolean isDouble(String word) {	
		
		try {
			Double.parseDouble(word);
		} catch (Exception e) {
			return false;
		}
	
		return true;
	}
	
	static int exactMatch(int i) {
		int back = -1;
		String imention = trimCommonWords(concepts.mention(i));
		String cmention;
		
		for(int c = 0; c < concepts.length(); c++) {
			cmention = trimCommonWords(concepts.mention(c));
			if(imention.contentEquals(cmention) && concepts.type(c).contentEquals("test") ) {
				return c;
			}
		}
		
		return back;
	}

	static int tenseMatch (int i, String [] table) {
		int back = -1;
		
		for (int c = 0; c < concepts.length(); c++ ) {
		
			if (concepts.type(c).contentEquals("test") && c!=i) {
				
				if (list.find(table, doc.line(concepts.startLine(i) ) ) && list.find(table, doc.line(concepts.startLine(c) ) ) ) {
					
					if (trimCommonWords(concepts.mention(i) ).contentEquals(trimCommonWords(concepts.mention(c) ) ) )
						return c;
					else if (wordMatch(trimCommonWords(concepts.mention(i) ), trimCommonWords(concepts.mention(c) ) ) ) 
						return c;
				}
			}
		}
	
		return back;
	}
	
	//641
	static int looseMatch (int i) {
		int back = -1;
		String itrim = trimCommonWords(concepts.mention(i) );
		String ctrim;
		for (int c = 0; c<concepts.length(); c++) {
			
			if (concepts.type(c).contentEquals("test") && c!=i) {
				ctrim = trimCommonWords(concepts.mention(c) );
				if ( (itrim != "") && (ctrim != "") ) {
					//System.out.println("0 "+itrim+ " " + ctrim);
					if (itrim.contentEquals(ctrim) || wordMatch(itrim, ctrim) ) {// && testMatch(itrim, ctrim)) {
						//System.out.println("0 "+itrim+ " " + ctrim);
						if ( (numberMatch(i, c) || (!hasNumber(i) && !hasNumber(c) ) ) && isSameDate(i, c) ) {
							//System.out.println("1 "+itrim + " " + ctrim);
							if (isSameDirection(i, c) ) {
								//System.out.println("1 "+itrim + " " + ctrim);
								return c;
							}
						}
						if (isOnAdmission(i) && isOnAdmission(c) && isSameDate(i, c) && (!hasNumber(i) && !hasNumber(c) ) ) {
							//System.out.println("2 "+itrim + " " + ctrim);
							if (isSameDirection(i, c) ) {
								//System.out.println("2 "+itrim + " " + ctrim);
								return c;
							}
						}
					}
				}
			}
		}
		return back;
	}
	
	static int pastMatch (int i) {
		int back = -1;
		
		for (int c = 0; c<concepts.length(); c++) {
			
			if (concepts.type(c).contentEquals("test") && c!=i) {
				
				if (pastTense(doc.line(concepts.startLine(i) ) ) && pastTense(doc.line(concepts.startLine(c) ) ) ) {
					if (trimCommonWords(concepts.mention(i) ).contentEquals(trimCommonWords(concepts.mention(c) ) ) )
						return c;
					else if (wordMatch(trimCommonWords(concepts.mention(i) ), trimCommonWords(concepts.mention(c) ) ) ) 
						return c;
				}
			}	
		}
		
		return back;
	}
	
	static boolean pastTense (String line) {
		boolean back = false;
		
		String [] buf = line.split(" ");
		for (int i = 0; i < buf.length; i++) {
			if (buf[i].endsWith("ed") && !buf[i].startsWith("un") || buf[i].contentEquals("was") ) {
				back = true;
		//		if(back)
		//			System.out.println(back);
				return (back);
			}
		}
		
		return false;
	}
	
	static boolean numberMatch (int i, int c) {
		
		int bound = 5;
		double num1 = Double.MIN_VALUE;
		double num2 = Double.MIN_VALUE;
		String [] remove = {"/", ","};
		String nextword1 = "";
		String nextword2 = "";
		if (getHeading(i).contains("laboratory") )
			bound = 3;
		if (getHeading(i).contains("hospital course") )
			bound = 5;
		//String [] line = doc.line(concepts.startLine(i) ).split(" "); 
		for (int a = 1; a<bound; a++) {
			nextword1 = doc.word(concepts.startLine(i), concepts.endWord(i) + a );
			nextword2 = doc.word(concepts.startLine(c), concepts.endWord(c) + a );
			for (String r : remove) {
				nextword1 = nextword1.replace(r, "");
				nextword2 = nextword2.replace(r, "");
			}
			if ( isNum(nextword1) )//&& !findDate(doc.line(concepts.startLine(i) ) ) )
				num1 = Double.parseDouble(nextword1);
			if ( isNum(nextword2) )//&& !findDate(doc.line(concepts.startLine(c) ) ) )
				num2 = Double.parseDouble(nextword2);
			if (num1 != Double.MIN_VALUE && num2 != Double.MIN_VALUE)
				if (num1 == num2)
					return true;
		}
		
		return false;
	}
	
	static String getHeading(int con) {
		int d = 0;
		String test = "";
		
			for(d = concepts.endLine(con); d > 0; d--) {
				test = doc.line(d);
				if(test.endsWith(" :") )
					return test;
			}
		
		return test;
	}
	
	static boolean isNum( String input )  
	{  
		
	   try  
	   {  
	      Double.parseDouble( input );  
	      return true;  
	   }  
	   catch( Exception e)  
	   {  
	      return false;  
	   }  
	}
	
	static boolean isOnAdmission(int i) {
		if (getHeading(i).contains("laboratory") )
			return true;
		for (int a = -1; a<2; a++) {
			if (doc.line(concepts.startLine(i) + a).contains("admission") )
				return true;
		}
		return false;
	}
	
	static boolean hasNumber(int i) {
		int bound = 6;
		String [] remove = {"/", ",", "%"};
		String nextword = "";
		String line = doc.line(concepts.startLine(i) );
		
		if (getHeading(i).contains("laboratory") )
			bound = 3;
		if (getHeading(i).contains("hospital course") )
			bound = 6;
		 
		for (int a = 1; a<bound; a++) {
			nextword = doc.word(concepts.startLine(i), concepts.endWord(i) + a );
			for (String r : remove) {
				nextword = nextword.replace(r, "");
			}
			if ( isNum(nextword) && !findDate(line) ) {
				//System.out.println(line);
				return true;
			}
		}
		
		return false;
	}
	
	static int studyMatch (int i) {
		int back = -1;
		
		if (concepts.type(i+1).contentEquals("test") ) {
			if (trimCommonWords(concepts.mention(i+1) ).contentEquals("study") || trimCommonWords(concepts.mention(i+1) ).contentEquals("exam") )
				return (i+1);
			
		}
		
		return back;
	}
	
	static int pronounMatch (int i) {
		int back = -1;
		String line = doc.line(concepts.startLine(i) + 1);
		String [] pronouns = {"this"}; /*,"it", "that"};*/ 
		
		for (String p : pronouns) {
			if (line.startsWith(p) || line.endsWith(p) || line.endsWith(p+" .") || line.endsWith(p+".") ) {
				//System.out.println(line);
				if (isOneTest(concepts.startLine(i) ) ) {
					//System.out.println(line + " " + concepts.mention(concepts.getNum(concepts.startLine(i) + 1, p)));
					return concepts.getNum(concepts.startLine(i) + 1, p);
				}
			}
		}
		
		if (concepts.getNum(concepts.startLine(i) + 1, "above") != -1 ) {
			//System.out.println(concepts.mention(concepts.getNum(concepts.startLine(i) + 1, "above") ) );
			return concepts.getNum(concepts.startLine(i) + 1, "above");
		}
		return back;
	}
	
	static boolean isOneTest(int i) {
		int count = 0;
		
		for (int c = 0; c<concepts.length(); c++) {
			
			if ( (concepts.startLine(c)==i) && (concepts.type(c).contentEquals("test") ) ) 
				count = count + 1;
			if (count>1) 
				return false;
			if (concepts.startLine(c)>i) 
				break;
		}
		return true;
	}
	
/*static int sideMatch (int i) {
		
		int back = -1;
		for (int c = 0; c<concepts.length(); c++) {
			if (concepts.type(c).contentEquals("test") && c!=i) {
				
				if (isSameDirection(doc.line(concepts.startLine(i) ), doc.line(concepts.startLine(c) ) ) ){
					if (trimCommonWords(concepts.mention(i) ).contentEquals(trimCommonWords(concepts.mention(c) ) ) )
						return c;
					if (wordMatch(trimCommonWords(concepts.mention(i) ), trimCommonWords(concepts.mention(c) ) ) ) 
						return c;
				}
			}
		}
		return back;
	}*/
	
	static boolean isSameDirection (int i, int c) {
		String prevword = "";
		String prevword2 = "";
		for (int a = -1; (concepts.startWord(i) + a)>=0 ; a--) {
			prevword = doc.word(concepts.startLine(i), concepts.startWord(i) + a );
			if (prevword.contentEquals("right") || prevword.contentEquals("left") ) {
				for (int b = -1; (concepts.startWord(c) + b)>=0 ; b--) {
					prevword2 = doc.word(concepts.startLine(c), concepts.startWord(c) + b);
					if (prevword2.contentEquals("right") || prevword2.contentEquals("left") )
						if (prevword.contentEquals(prevword2) )
							return true;
						else
							return false;
				}
				//System.out.println(prevword + " " + doc.word(concepts.startLine(c), 0) );
				return false;
			}
		}
		
		return true;
	}
			
	static boolean matchBodyPart(String con1, String con2) {
		
		String [] buf1;
		String [] buf2;
		
		buf1 = con1.split(" ");
		buf2 = con2.split(" ");
		if (!list.find(list.body_parts, con1) || !list.find(list.body_parts, con2) ) 
			return true;
		
		for (int a = 0; a<buf1.length; a++) {
			
			if (list.find(list.body_parts, buf1[a]) ) {
				
				for (int b = 0; b<buf2.length; b++) {
					
					if (buf1[a].contentEquals(buf2[b]) ) {					
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	static boolean looseStringMatch(String con1, String con2) {
	
		double fraction_matched;
		int count = 0;
		String [] buf1;
		String [] buf2;
		
		buf1 = con1.split(" ");
		buf2 = con2.split(" ");
		
		if(buf1.length > buf2.length)
			fraction_matched = (buf2.length * 6.0)/10;
		else
			fraction_matched = (buf1.length * 6.0)/10;
		//System.out.println(fraction_matched);
		for (int a = 0; a<buf1.length; a++) {
			
			for (int b = 0; b<buf2.length; b++) {
				
				if (wordMatch(buf1[a], buf2[b]) ) {					
					//System.out.println(count+" "+fraction_matched);
					count++;
				}
			
				if (count>=fraction_matched) {
					//System.out.println(count+" "+fraction_matched);
					//System.out.println(con1+" "+con2+" true");
					return true;
				}
			}
		}
		//System.out.println(con1+" "+con2+" false");
		return false;
	}
	
	static boolean testMatch(String con1, String con2) {
		
		String [] buf1;
		String [] buf2;
		
		buf1 = con1.split(" ");
		buf2 = con2.split(" ");
		if (buf1[buf1.length-1].contentEquals(buf2[buf2.length-1]) ) {
			//System.out.println(buf1[buf1.length-1]+" "+buf2[buf2.length-1]);
			return true;
		}
		
		return false;
	}
	
	static String trimCommonWords(String in) {
		String[] split = in.split(" ");
		String back = "";
			
		for(int c = 0; c < split.length; c++) {
			if(!list.common(split[c]) && !list.isDoctor(split[c]) && !list.find(list.body_parts, split[c]) )
				back += split[c] + " ";
		}
		
		return back.trim();
	}

	static boolean wordMatch(String w1, String w2) {
		//System.out.println(w1+" "+w2);
		if(w1.length()<4 || w2.length()<4)
			return w1.contentEquals(w2);
		//System.out.println(w1+" "+w2);
		int length; 
		if(w1.length() > w2.length())
			length = (w2.length() * 8)/10;
		else
			length = (w1.length() * 8)/10;
		
		for(int c = 0; c < length; c++)
			if(w1.charAt(c) != w2.charAt(c))
				return false;
		//System.out.println(w1+" "+w2);
		if(!w1.endsWith("ing")&&w2.endsWith("ing"))
			return false;
		if(w1.endsWith("ing")&&!w2.endsWith("ing"))
			return false;
		
		//System.out.println(w1+" "+w2);
		return true;
	}
	
	static boolean findDate(String date) { 
		SimpleDateFormat checker = new SimpleDateFormat();
		String [] formats = {"MMMMMMM dd , yyyy", "MMM/dd/yyyy", "MMM dd , yyyy", "MMM", "MM/dd/yy", "MM/d/yy", "MMM dd, yyyy", "yyyy-MM-dd", "yyyy-M-dd",
				"yyyy-M-d", "yyyy-MM-d", "MM-dd-yy", "yy-MM", "yyyyMMdd"};
		boolean success = false;
		Date result = new Date();
		
		for(int i = 0; i < formats.length; i++) {
			if(success)
				break;
			ParsePosition pp = new ParsePosition(0);
			checker.applyPattern(formats[i]);
			for(int j = 0; j < date.length(); j++) {
				pp.setIndex(j);
				result = checker.parse(date, pp);
				if(result != null) {
					//System.out.println(result);
					success = true;
					break;
				}
			}
		}
	 
		return success;
	 }
	
	static boolean isSameDate(int i, int c) {
	
		String line1 = doc.line(concepts.startLine(i) );
		String line2 = doc.line(concepts.startLine(c) );
		
		//System.out.println(line1+" "+line2);
		if (!findDate(line1) && !findDate(line2) )
			return true;
		//System.out.println(line1+" "+line2);
		if (findDate(line1) && findDate(line2) ) {
			//System.out.println(line1+" "+line2);
			if (getDate(line1).equals(getDate(line2) ) ) {
				//System.out.println(line1+" "+line2);
				return true;
			}
		}
		
		return false;
	}
	
	static Date getDate(String date) { 
		SimpleDateFormat checker = new SimpleDateFormat();
		boolean success = false;
		Date result = new Date();
		String [] formats = {"MMMMMMM dd , yyyy", "MMM/dd/yyyy", "MMM dd , yyyy", "MMM", "MM/dd/yy", "MM/d/yy", "MMM dd, yyyy", "yyyy-MM-dd", "yyyy-M-dd",
				"yyyy-M-d", "yyyy-MM-d", "MM-dd-yy", "yy-MM", "yyyyMMdd"};
		for(int i = 0; i < formats.length; i++) {
			if(success)
				break;
			ParsePosition pp = new ParsePosition(0);
			checker.applyPattern(formats[i]);
			for(int j = 0; j < date.length(); j++) {
				pp.setIndex(j);
				result = checker.parse(date, pp);
				if(result != null) {
					//System.out.println(result);
					success = true;
					break;
				}
			}
		}
	 
		return result;
	 }
	
	static int rule2(int i, String pronoun) {   //  "which" the pronoun refers to the concept mention before it in the same sentence, when not in a question
		
		String buf = concepts.mention(i);
		
		if(concepts.startLine(i) != concepts.startLine(i-1))
			return -1;
		
		int dist = concepts.startWord(i) - concepts.endWord(i-1);
		//System.out.println(concepts.mention(i-1)+" "+concepts.mention(i)+" "+dist);
		if((dist < 1) || (dist > 13))
			return -1;
		
		buf = doc.line(concepts.startLine(i));
		
		if(buf.contains("?")) {
			return -1;
		}
		
		//System.out.println(concepts.mention(i-1)+" "+concepts.mention(i)+" "+dist);
		if(buf.indexOf(pronoun) > 3)
		if(buf.substring(buf.indexOf(pronoun)-3, buf.indexOf(pronoun)-1).contentEquals("at"))
			return -1;
		//System.out.println(i-1);
		//System.out.println(concepts.mention(i-1)+" "+concepts.mention(i)+" "+dist);
		return i - 1;
	}
	

}
