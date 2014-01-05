package uhd.coref;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

public class Lists {
	
	public String[] pastTense;
	public String[] presentTense;
	public String[] futureTense;
	public String[] common;
	public String[] pronoun;
	public String[] headings;
	public String[] past_headings;
	public String[] present_headings;
	public String[] future_headings;
	public String[] hospital_course_procedures;
	public String[] doctor;
	public String[] body_parts;
	public String[] is_phrases;
	public static List<query> queries = new ArrayList<query>(0);
	public List<String> verbs;
	public List<SynWord> adj;
	public static List<SynWord> synonyms;
	
	Lists() {
		
		Database.open();
		
		presentTense = Database.get("present_tense");
		pastTense = Database.get("past_tense");
		futureTense = Database.get("future_tense");
		common = Database.get("common");
		pronoun = Database.get("pronoun");
		headings = Database.get("headings");
		past_headings = Database.get("past_headings");
		present_headings = Database.get("present_headings");
		future_headings = Database.get("future_headings");
		hospital_course_procedures = Database.get("hospital_course_procedures");
		queries = Database.getQueries();
		doctor = Database.get("doctor");
		body_parts = Database.get("body_parts"); 
		is_phrases = Database.get("is_phrases"); 
		synonyms = getSyns();
		//	verbs = getVerbs();
		adj = getAdj();
		
		Database.close();
	}
	
	static List<SynWord> getAdj() {
		List<SynWord> list = new ArrayList<SynWord>(13650);
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader("adj.txt"));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	list.add(new SynWord(line.replace("_", " "),0));
		        }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		      ex.printStackTrace();
		    }
		    
		    return list;
	}
	
	static List<String> getVerbs() {
		List<String> list = new ArrayList<String>(13650);
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader("verbs.txt"));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	list.add(line.replace("_", " "));
		        }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		      ex.printStackTrace();
		    }
		    
		    return list;
	}
	
	static List<SynWord> getSyns() {
		List<SynWord> list = new ArrayList<SynWord>(54000);
		String[] t;

		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader("synonyms.txt"));
		      try {
		        String line = null;
		        
		        
		        		        
		        while(( line = input.readLine()) != null) {
		        	t = line.split("\t");
		        	list.add(new SynWord(t[1], Integer.parseInt(t[0])));
		        }
		        
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		      ex.printStackTrace();
		    }
		    
		    return list;
	}
	
	public boolean isAdj(String s1) {
		
		return bSearch(s1, adj) >= 0;

	}
	
	public boolean isSyn(String s1, String s2) {
		int index1 = 0, index2 = 0;
		
		index1 = bSearch(s1, synonyms);
		index2 = bSearch(s2, synonyms);
		
		if(index1 < 0)
			return false;
		if(index2 < 0)
			return false;
		
		Point l1 = getIndex(index1);
		Point l2 = getIndex(index2);
		
		for(index1 = l1.x; index1 < l1.y; index1++)
			for(index2 = l2.x; index2 < l2.y; index2++)
				if(synonyms.get(index1).group == synonyms.get(index2).group)
					return true;
		
		return false;
	}
	
	static Point getIndex(int index) {
		Point pos = new Point(0, 0);
		String word = synonyms.get(index).word;
		
		pos.x = index;
		pos.y = index;
		
		while(synonyms.get(pos.x).word.contentEquals(word))
			pos.x--;
		pos.x++;
		while(synonyms.get(pos.y).word.contentEquals(word)&&(pos.x < synonyms.size()))
			pos.y++;
		
		return pos;
	}
		
	static int bSearch(String w, List<SynWord> list) {
		int back = -1;
		int size = list.size();
		int middle = size / 2;
    	int high = size;
    	int low = 0;
    	int comp = 0;
    	boolean found = false;
		
    	while(!found) {
    		comp = list.get(middle).word.compareTo(w);
    		if(comp < 0) {
    			if(middle >= (size-1)) {
    				found = true;
    			} else
    			if(list.get(middle+1).word.compareTo(w) < 0) {
    				low = middle+1;
    				middle = ((high-low) / 2) + low;
    			}
    			else {
    				if(list.get(middle+1).word.compareTo(w) == 0)
    					back = middle+1;
    				found = true;
    			}
    		}
    		else if(comp > 0) {
    			if(middle <= 0) {
    				found = true;
    			} else
    			if(list.get(middle-1).word.compareTo(w) > 0) {
    				high = middle-1;
    				middle = high - ((high-low) / 2);
    			} else {
    				if(list.get(middle-1).word.compareTo(w) == 0)
    					back = middle-1;
    				found = true;
    			}
    		}
    		else if(comp == 0) {
    			back = middle;
    			found = true;
    		}
    	}
		return back;
	}
	
	public boolean pronoun(String pn) {
		
		for(int c = 0; c < pronoun.length; c++) {
			if(pronoun[c].contentEquals(pn))
				return true;
		}
	
		return false;	
	}
	
	public boolean find(String[] table, String phrase) {
		
		if(table != null) {
			for(int c = 0; c < table.length; c++) {
				if(phrase.contains(table[c]))
					return true;
			}
		}
		return false;
	}
	
	public boolean findExact(String[] table, String phrase) {
		
		if(table != null) {
			for(int c = 0; c < table.length; c++) {
				if(phrase.contentEquals(table[c]))
					return true;
			}
		}
		return false;
	}
	
	public boolean isInHeading(List<String[]> list, int con, Concepts concepts, Doc doc) {
		int c = 0, d = 0, e = 0;
		
		 
			for(d = concepts.endLine(con); d > 0; d--) {
				for(c = 0; c < list.size(); c++)
					for(e = 0; e < list.get(c).length; e++)
						if(doc.line(d).contentEquals(list.get(c)[e]))
							return true;
				for(e = 0; e < headings.length; e++)
					if(doc.line(d).contentEquals(headings[e]))
						return false;
			}
		
		
		return false;
	}
	
	public boolean common(String com) {
		
		for(int c = 0; c < common.length; c++) {
			if(common[c].trim().contentEquals(com))
				return true;
		}
	
		return false;	
	}
	
	public boolean isDoctor(String name) {
		
		for(int c = 0; c < doctor.length; c++) {
			if(name.contentEquals(doctor[c]))
				return true;
		}
		
		return false;
	}
	

	public boolean isDoctor1(String name) {
		String[] buf = name.split(" ");
		int d = 0;
		
		if(name.contentEquals("i"))
			return true;
		
		if(name.contentEquals("me"))
			return true;
		
		if(name.contentEquals("my"))
			return true;
		
		if(name.contentEquals("we"))
			return true;
		
		if(name.contentEquals("our"))
			return true;
		
		if(name.contentEquals("us"))
			return true;
		
		for(int c = 0; c < doctor.length; c++) {
			for(d = 0; d < buf.length; d++)
				if(buf[d].replace("'", "").contentEquals(doctor[c]))
					return true;
		}
		
	/*
		for(int c = 0; c < buf.length; c++) {
			if(buf[c].endsWith("ologist")||buf[c].endsWith("ology")||buf[c].endsWith("trist")||buf[c].endsWith("try")) {
				Database.add("doctor", buf[c]);
				String[] temp = new String[doctor.length+1];
				for(d = 0; d < doctor.length; d++) {
					temp[d] = doctor[d];
				}
				temp[temp.length-1] = buf[c];
				doctor = temp;
				System.out.println("Added: " + buf[c]);
				return true;
			}
		}
		
		if(!pronoun(name)&&name.length() > 2&&!name.contains("'")&&include(name))
			if(bingMedicalSearch(name)) {
				Database.add("doctor", name);
				String[] temp = new String[doctor.length+1];
				for(d = 0; d < doctor.length; d++) {
					temp[d] = doctor[d];
				}
				temp[temp.length-1] = name;
				doctor = temp;
				System.out.println("Added: " + name);
				return true;
			}
			
			*/
		
		return false;
	}
	
	public void findDoctors(Concepts cons) {

		for(int i = 0; i < cons.length(); i++) { 
		if(cons.type(i).contentEquals("person")||cons.type(i).contentEquals("people")) {
		if(!isDoctor1(cons.mention(i))) {
			
		String name = cons.mention(i);
		String[] buf = name.split(" ");
		
		int d = 0;
		
		for(int c = 0; c < buf.length; c++) {
			if(buf[c].endsWith("ologist")||buf[c].endsWith("ology")||buf[c].endsWith("trist")||buf[c].endsWith("try")) {
				Database.add("doctor", buf[c]);
				String[] temp = new String[doctor.length+1];
				for(d = 0; d < doctor.length; d++) {
					temp[d] = doctor[d];
				}
				temp[temp.length-1] = buf[c];
				doctor = temp;
				System.out.println("Added: " + buf[c]);
			}
		}
		
		if(!pronoun(name)&&name.length() > 2&&!name.contains("'")&&include(name))
			if(!name.contains("name["))
			if(bingMedicalSearch(name)) {
				Database.add("doctor", name);
				String[] temp = new String[doctor.length+1];
				for(d = 0; d < doctor.length; d++) {
					temp[d] = doctor[d];
				}
				temp[temp.length-1] = name;
				doctor = temp;
				System.out.println("Added: " + name);
			}
		}
		}
		}
	}
	
	
	static boolean bingMedicalSearch(String phrase) {
			
		for(int c = 0; c < queries.size(); c++) {
			if(queries.get(c).phrase.contentEquals(phrase))
				return false;
		}
		
		int total = 0;
			
		boolean medical = false;
		
		query back = new query();
		back.phrase = phrase;
			
			try {
				URL google = new URL("http://www.bing.com/search?q=" + phrase.replace(' ', '+') + "&qs=n&sk=&form=QBRE");
				URLConnection yc = google.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
				String line;
				Thread.sleep(2000);
				
				System.out.println("Searched: " + phrase);
				
				
				
				while ((line = in.readLine()) != null) {
					
					if(line.contains("<span class=\"sb_count\" id=\"count\">1-10 of ")) {
						int l = ("<span class=\"sb_count\" id=\"count\">1-10 of ").length();
						int p = line.indexOf("<span class=\"sb_count\" id=\"count\">1-10 of ") + l;
						int end = line.indexOf(" " , p);
						String number = line.substring(p , end).replace(",", "");
						
						if(number.length() < 7) {
							int count = Integer.parseInt(number);
							
							if(count < 5000) {
								while ((line = in.readLine()) != null) {}
								in.close();
								return false;
							}
						}
					}
					
					if(line.contains("medical")) {
						total++;
						medical = true;
					}
					if(line.contains("clinic")) {
						total++;
						medical = true;
					}
					if(line.contains("hospital")) {
						total++;
						medical = true;

					}
					if(line.contains("Dr. ")) {
						total++;
						medical = true;
					}
					if(line.contains("medicine")) {
						total++;
						medical = true;

					}
					if(line.contains(" MD")) {
						total++;
						medical = true;
					}
					if(line.contains("MedlinePlus")) {
						total++;
						medical = true;
					}
					if(line.contains("specialist")) {
						total++;
						medical = true;
					}
					if(line.contains("physician")) {
						total++;
						medical = true;
					}
					if(line.contains("surgery")) {
						total++;
						medical = true;
					}
					if(line.contains("doctor")) {
						total++;
						medical = true;
					}
					if(line.contains("surgical")) {
						total++;
						medical = true;
					}
					if(line.contains("health")) {
						total++;
						medical = true;
					}
					if(line.contains("trauma")) {
						total++;
						medical = true;
					}
				}
				in.close();
				
				
			} catch (Exception e) {
				e.printStackTrace();
				
				 medical = bingMedicalSearch(phrase);
			}
			
			
			Database.addQuery(back);
			queries.add(back);
			
			
			
			return total > 1;
		
	}
	
	static boolean include(String name) {
		if(name.contains("mr."))
			return false;
		if(name.contains("baby"))
			return false;
		if(name.contains("infant"))
			return false;
		if(name.contains("newborn"))
			return false;
		if(name.contains("male"))
			return false;
		if(name.contains("female"))
			return false;
		if(name.contains(" man"))
			return false;
		if(name.contains("woman"))
			return false;
		if(name.contains("ms."))
			return false;
		if(name.contains("patient"))
			return false;
		if(name.contains("year"))
			return false;
		
		return true;
	}
}
